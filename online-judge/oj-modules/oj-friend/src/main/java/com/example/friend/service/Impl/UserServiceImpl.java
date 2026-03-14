package com.example.friend.service.Impl;

import cn.hutool.core.util.RandomUtil;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.utils.RegexUtil;
import com.example.common.message.util.MailUtil;
import com.example.common.security.exception.ServiceException;
import com.example.friend.domain.dto.UserDTO;
import com.example.friend.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.xml.transform.Result;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private MailUtil mailUtil;
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    @Value("${mail.verify.expire-time:300}")
    private long expireTime;
    @Value("${mail.verify.frequency:60}")
    private long frequencyTime;
    // Redis key前缀
    private static final String VERIFY_CODE_KEY = "verify:code:";
//    private static final String VERIFY_FREQUENCY_KEY = "verify:frequency:";
    @Override
    public void sendCode(UserDTO userDTO) {
        String email = userDTO.getEmail();
        // 详细的QQ邮箱校验
        RegexUtil.QQEmailCheckResult checkResult = RegexUtil.checkQQEmailDetail(email);
        if (!checkResult.isValid()) {
            log.warn("邮箱格式错误: {}, reason: {}", email, checkResult.getMessage());
            throw new ServiceException(ResultCode.FAILED_USER_EMAIL.getCode(), checkResult.getMessage());
        }
        // 如果有警告，记录日志但继续执行
        if (checkResult.getWarning() != null) {
            log.warn("邮箱格式警告: {}, warning: {}", email, checkResult.getWarning());
        }
        // 频率限制（防止恶意刷验证码） 60内不能重复发送
        if (redisTemplate != null) {
            String frequencyKey = VERIFY_CODE_KEY + email;
//            String codeKey = VERIFY_CODE_KEY + email;
            Boolean hasKey = redisTemplate.hasKey(frequencyKey);
            if (hasKey != null && hasKey) {
                Long ttl = redisTemplate.getExpire(frequencyKey, TimeUnit.SECONDS);
                if (ttl > expireTime-frequencyTime) {
                    String msg = String.format("验证码发送过于频繁，请等待 %d 秒后重试", ttl+frequencyTime-expireTime);
                    log.warn("验证码发送频率限制: {}, 剩余: {}秒", email, ttl);
                    throw new ServiceException(ResultCode.FAILED_TOO_FREQUENT.getCode(), msg);
                }
            }
        }
        // 生成验证码
        String code = RandomUtil.randomNumbers(6);
        log.info("生成验证码: email={}, code={}", email, code);
        // 发送邮件
        try {
            mailUtil.sendVerifyCode(email, expireTime, code);
            log.info("验证码发送成功: {}", email);
        } catch (Exception e) {
            log.error("验证码发送失败: {}, error: {}", email, e.getMessage(), e);
            throw new ServiceException(ResultCode.ERROR.getCode(), "验证码发送失败: " + e.getMessage());
        }
    }
}
