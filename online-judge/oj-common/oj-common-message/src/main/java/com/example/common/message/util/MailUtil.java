package com.example.common.message.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮件发送工具类
 */
@Slf4j
@Component
public class MailUtil {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${mail.verify.expire-time:300}")
    private long expireTime;
    
    @Value("${mail.verify.subject:邮箱验证码}")
    private String subject;
    
    @Value("${mail.verify.template:您的验证码是：%s，%d分钟内有效，请勿泄露给他人。}")
    private String template;

    @Value("${mail.from-name:OJ系统}")
    private String fromName;
    
    /**
     * 生成随机验证码
     * @param length 验证码长度
     * @return 验证码
     */
    public String generateCode(int length) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 生成6位验证码
     */
    public String generateCode() {
        return generateCode(6);
    }
    
    /**
     * 发送简单邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    public void sendSimpleMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String from = String.format("%s <%s>", fromName, fromEmail);
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("邮件发送成功 to: {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败 to: {}, error: {}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }
    
    /**
     * 发送验证码邮件
     * @param email 收件邮箱
     * @return 验证码
     */
    public String sendVerifyCode(String email,String code) {
        // 生成验证码
//        String code = generateCode();
        
        // 生成邮件内容
        String content = String.format(template, code, expireTime / 60);
        
        // 发送邮件
        sendSimpleMail(email, subject, content);
        
        // 存储验证码到Redis
//        if (redisTemplate != null) {
//            String key = "verify:code:" + email;
//            redisTemplate.opsForValue().set(key, code, expireTime, TimeUnit.SECONDS);
//        }
        return code;
    }
    
    /**
     * 发送验证码邮件（自定义过期时间）
     * @param email 收件邮箱
     * @param expireSeconds 过期时间（秒）
     * @return 验证码
     */
    public String sendVerifyCode(String email, long expireSeconds,String code) {
        // 生成验证码
//        String code = generateCode();
        
        // 生成邮件内容
        String content = String.format(template, code, expireSeconds / 60);
        
        // 发送邮件
        sendSimpleMail(email, subject, content);
        
        // 存储验证码到Redis
//        if (redisTemplate != null) {
//            String key = "verify:code:" + email;
//            redisTemplate.opsForValue().set(key, code, expireSeconds, TimeUnit.SECONDS);
//        }
        
        return code;
    }
    
    /**
     * 验证验证码
     * @param email 邮箱
     * @param code 验证码
     * @return 是否验证通过
     */
    public boolean verifyCode(String email, String code) {
        if (redisTemplate == null) {
            log.warn("Redis not configured, cannot verify code");
            return false;
        }
        
        String key = "verify:code:" + email;
        String savedCode = redisTemplate.opsForValue().get(key);
        
        if (savedCode != null && savedCode.equals(code)) {
            // 验证成功后删除验证码（防止重复使用）
            redisTemplate.delete(key);
            return true;
        }
        
        return false;
    }
    
    /**
     * 验证验证码（不删除，仅验证）
     * @param email 邮箱
     * @param code 验证码
     * @return 是否验证通过
     */
    public boolean checkCode(String email, String code) {
        if (redisTemplate == null) {
            log.warn("Redis not configured, cannot verify code");
            return false;
        }
        
        String key = "verify:code:" + email;
        String savedCode = redisTemplate.opsForValue().get(key);
        
        return savedCode != null && savedCode.equals(code);
    }
}