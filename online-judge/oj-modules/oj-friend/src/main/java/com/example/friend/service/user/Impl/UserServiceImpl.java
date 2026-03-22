package com.example.friend.service.user.Impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.constants.Constants;
import com.example.common.core.constants.HttpConstants;
import com.example.common.core.domain.LoginUser;
import com.example.common.core.domain.R;
import com.example.common.core.domain.vo.LoginUserVO;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.enums.UserIdentity;
import com.example.common.core.enums.UserStatus;
import com.example.common.core.utils.RegexUtil;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.common.message.util.MailUtil;
import com.example.common.redis.service.RedisService;
import com.example.common.security.exception.ServiceException;
import com.example.common.security.service.TokenService;
import com.example.friend.domain.user.User;
import com.example.friend.domain.user.dto.UserDTO;
import com.example.friend.domain.user.dto.UserUpdateDTO;
import com.example.friend.domain.user.vo.UserVO;
import com.example.friend.manager.UserCacheManager;
import com.example.friend.mapper.user.UserMapper;
import com.example.friend.service.user.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RefreshScope
public class UserServiceImpl implements IUserService {
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserCacheManager userCacheManager;
    
    @Value("${mail.verify.expire-time:300}")
    private long expireTime;
    @Value("${mail.verify.frequency:60}")
    private long frequencyTime;
    @Value("${mail.verify.times:3}")
    private Integer sendLimit;
    @Value("${jwt.secret}")
    private String secret;
    @Value("${mail.is-send:false}")
    private boolean isSend; //开关

    @Value("${file.oss.downloadUrl}")
    private String downloadUrl;
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
            String frequencyKey = CacheConstants.VERIFY_CODE_KEY + email;
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
        // 次数限制（防止恶意刷验证码） 一天次数50次 计数
        //操作 记录次数当天有效 大于限制->抛出异常
        String codeTimeKey = CacheConstants.CODE_TIME_KEY + email;
        Long sendTimes = redisService.getCacheObject(codeTimeKey, Long.class);
        if(sendTimes != null && sendTimes >= sendLimit){
            throw new ServiceException(ResultCode.SEND_TIME_LIMIT);
        }
        // 生成验证码
        String code = isSend ? RandomUtil.randomNumbers(6) : Constants.DEFAULT_CODE;
        log.info("生成验证码: email={}, code={}", email, code);
        // 发送邮件
        if(isSend){
            try {
                mailUtil.sendVerifyCode(email, expireTime, code);
                log.info("验证码发送成功: {}", email);
            }catch (Exception e) {
                log.error("验证码发送失败: {}, error: {}", email, e.getMessage(), e);
                throw new ServiceException(ResultCode.ERROR.getCode(), "验证码发送失败: " + e.getMessage());
            }
        }
        if (redisTemplate != null) {
            String key = CacheConstants.VERIFY_CODE_KEY + email;
            redisTemplate.opsForValue().set(key, code,expireTime, TimeUnit.SECONDS);
        }
            redisService.increment(codeTimeKey);
            if(null == sendTimes){ //当天第一次
                long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withNano(0));
                redisService.expire(codeTimeKey,seconds,TimeUnit.SECONDS);
            }
    }

    @Override
    public String codeLogin(String email, String code) {
        //验证验证码
        checkCode(email, code);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        //新用户 注册 -> 登录
        if(null == user){
            //注册
            user = new User();
            user.setEmail(email);
            user.setNickName(email.split("@")[0]); // 临时昵称
            user.setCreateTime(LocalDateTime.now());
            user.setCreateBy(0L);
            user.setStatus(UserStatus.NORMAL.getValue()); // 正常状态
            userMapper.insert(user);
        }
        //老用户
        //登录逻辑
        //生成Token
        return tokenService.createToken(user.getUserId(),secret, UserIdentity.ORDINARY.getValue(), user.getNickName(), user.getHeadImage());
    }

    @Override
    public boolean logout(String token) {
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        return tokenService.deleteLoginUser(token, secret);
    }

    @Override
    public R<LoginUserVO> info(String token) {
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        LoginUser loginUser = tokenService.getLoginUser(token, secret);
        if(null == loginUser){
            return R.fail();
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setNickName(loginUser.getNickName());
        if(StrUtil.isNotEmpty(loginUserVO.getHeadImage())) {
            loginUserVO.setHeadImage(downloadUrl + loginUser.getHeadImage());
        }
        return R.ok(loginUserVO);
    }

    @Override
    public UserVO detail() {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if(null == userId){
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        UserVO userVO = userCacheManager.getUserId(userId);
        if(null == userVO){
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if(StrUtil.isNotEmpty(userVO.getHeadImage())) {
            userVO.setHeadImage(downloadUrl + userVO.getHeadImage());
        }
        return userVO;
    }

    @Override
    public int edit(UserUpdateDTO userUpdateDTO) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if(null == userId){
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        User user = userMapper.selectById(userId);
        if(null == user){
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        user.setNickName(userUpdateDTO.getNickName());
        user.setSex(userUpdateDTO.getSex());
        user.setPhone(userUpdateDTO.getPhone());
        user.setEmail(userUpdateDTO.getEmail());
        user.setWechat(userUpdateDTO.getWechat());
        user.setSchoolName(userUpdateDTO.getSchoolName());
        user.setMajorName(userUpdateDTO.getMajorName());
        user.setIntroduce(userUpdateDTO.getIntroduce());
        //更新用户缓存
        userCacheManager.refreshUser(user);
        tokenService.refreshLoginUser(user.getNickName(),user.getHeadImage(),
                ThreadLocalUtil.get(Constants.USER_KEY,String.class));
        return userMapper.updateById(user);
    }

    @Override
    public int updateHeadImage(String headImage) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if(null == userId){
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        User user = userMapper.selectById(userId);
        if(null == user){
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        user.setHeadImage(headImage);
        //更新用户缓存
        userCacheManager.refreshUser(user);
        tokenService.refreshLoginUser(user.getNickName(),user.getHeadImage(),
                ThreadLocalUtil.get(Constants.USER_KEY,String.class));
        return userMapper.updateById(user);
    }

    private void checkCode(String email, String code) {
        String emailCodeKey = CacheConstants.VERIFY_CODE_KEY + email;
        String cacheCode = redisService.getCacheObject(emailCodeKey, String.class);
        //验证码过期
        if(StrUtil.isEmpty(cacheCode)){
            throw new ServiceException(ResultCode.FAILED_INVALID_CODE);
        }
        //验证码错误
        if(!cacheCode.equals(code)){
            throw new ServiceException(ResultCode.FAILED_ERROR_CODE);
        }
        //验证码比对成功 -> 删除验证码
        redisService.deleteObject(emailCodeKey);
    }
}
