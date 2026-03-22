package com.example.common.file.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.ObjectId;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.constants.Constants;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.common.file.config.OSSProperties;
import com.example.common.file.domain.OSSResult;
import com.example.common.redis.service.RedisService;
import com.example.common.security.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RefreshScope
public class OSSService {

    @Autowired
    private OSSProperties prop;
    
    @Autowired
    private OSS ossClient;  // 改为接口类型
    
    @Autowired
    private RedisService redisService;

    @Value("${file.max-time:10}")
    private int maxTime;

    @Value("${file.test:false}")
    private boolean test;

    /**
     * 上传文件
     */
    public OSSResult uploadFile(MultipartFile file) {
        // 非测试模式下检查上传次数
        if (!test) {
            checkUploadCount();
        }
        
        // 参数校验
        if (file == null || file.isEmpty()) {
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD, "文件不能为空");
        }
        
        // 使用try-with-resources自动关闭流
        try (InputStream inputStream = file.getInputStream()) {
            String extName = getFileExtension(file);
            return upload(extName, inputStream);
        } catch (IOException e) {
            log.error("OSS upload file error: {}", ExceptionUtil.stacktraceToOneLineString(e, 500));
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD, "文件读取失败");
        } catch (Exception e) {
            log.error("OSS upload file error: {}", ExceptionUtil.stacktraceToOneLineString(e, 500));
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD, "文件上传失败");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            // 使用文件内容类型推断扩展名
            String contentType = file.getContentType();
            if (contentType != null) {
                switch (contentType) {
                    case "image/jpeg":
                        return "jpg";
                    case "image/png":
                        return "png";
                    case "image/gif":
                        return "gif";
                    default:
                        return "bin";
                }
            }
            return "bin";
        }
        
        String extName = FileUtil.extName(originalFilename);
        return StrUtil.isBlank(extName) ? "bin" : extName.toLowerCase();
    }

    /**
     * 检查用户上传次数限制
     */
    private void checkUploadCount() {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if (userId == null) {
            log.warn("User ID not found in thread local");
            return;
        }
        
        String userKey = String.valueOf(userId);
        Long times = redisService.getCacheMapValue(
            CacheConstants.USER_UPLOAD_TIMES_KEY, 
            userKey, 
            Long.class
        );
        
        // 检查是否超过限制
        if (times != null && times >= maxTime) {
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD_TIME_LIMIT);
        }
        
        // 设置过期时间（在增加计数前设置，确保存在过期时间）
        if (times == null) {
            long secondsUntilMidnight = getSecondsUntilMidnight();
            redisService.expire(CacheConstants.USER_UPLOAD_TIMES_KEY, secondsUntilMidnight, TimeUnit.SECONDS);
        }
        
        // 增加上传计数
        redisService.incrementHashValue(CacheConstants.USER_UPLOAD_TIMES_KEY, userKey, 1);
    }
    
    /**
     * 获取到第二天凌晨的秒数
     */
    private long getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return ChronoUnit.SECONDS.between(now, midnight);
    }

    /**
     * 执行OSS上传
     */
    private OSSResult upload(String fileType, InputStream inputStream) {
        // 生成唯一文件名：pathPrefix/id.xxx
        String key = prop.getPathPrefix() + ObjectId.next() + "." + fileType;
        
        // 设置对象元数据
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setObjectAcl(CannedAccessControlList.PublicRead);
        
        // 构建上传请求
        PutObjectRequest request = new PutObjectRequest(
            prop.getBucketName(), 
            key, 
            inputStream, 
            objectMetadata
        );
        
        try {
            PutObjectResult putObjectResult = ossClient.putObject(request);
            return assembleOSSResult(key, putObjectResult);
        } catch (Exception e) {
            log.error("OSS put object error: {}", ExceptionUtil.stacktraceToOneLineString(e, 500));
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD, "OSS上传失败");
        }
    }

    /**
     * 组装返回结果
     */
    private OSSResult assembleOSSResult(String key, PutObjectResult putObjectResult) {
        OSSResult ossResult = new OSSResult();
        
        if (putObjectResult != null && StrUtil.isNotBlank(putObjectResult.getRequestId())) {
            ossResult.setSuccess(true);
            ossResult.setName(FileUtil.getName(key));
            // 添加更多返回信息
            ossResult.setUrl(prop.getEndpoint() + "/" + key);  // 需要根据实际配置调整
            ossResult.setKey(key);
        } else {
            ossResult.setSuccess(false);
            log.warn("OSS put object result is invalid");
        }
        
        return ossResult;
    }
}