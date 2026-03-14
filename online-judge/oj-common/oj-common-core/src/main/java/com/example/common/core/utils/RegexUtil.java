package com.example.common.core.utils;

import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

public class RegexUtil {

    // ==================== 手机号校验 ====================

    /**
     * 校验手机号（基础版）
     * 规则：1开头的11位数字，第二位限制为3-9
     * 说明：这是目前最通用的校验，覆盖了主流号段
     *
     * @param phone 手机号
     * @return true=合法，false=不合法
     */
    public static boolean checkMobile(String phone) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }
        // 正则解释：^1 开头，[3-9] 第二位是3-9，[0-9]{9} 后面9位数字，$ 结尾
        String regex = "^1[3-9][0-9]{9}$";
        return Pattern.matches(regex, phone);
    }

    // ==================== 通用邮箱校验 ====================

    /**
     * 校验邮箱（基础实用版）
     * 规则：xxx@xx.xx 形式的邮箱
     * 说明：这是最常用的校验方式，覆盖了绝大多数正常邮箱
     *
     * @param email 邮箱地址
     * @return true=合法，false=不合法
     */
    public static boolean checkMail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        /**
         * 正则含义：
         * ^[a-z0-9]+          - 以字母或数字开头
         * ([._\\-]*[a-z0-9])* - 允许点、下划线、短横线，后面跟字母数字（可重复0次或多次）
         * @                    - 必须包含@
         * ([a-z0-9]+[.-]*[a-z0-9]+.) - 域名部分：字母数字 + 可选的点或短横线 + 字母数字 + 点
         * {1,63}               - 域名段重复1-63次
         * [a-z0-9]+$           - 顶级域以字母数字结尾
         */
        String regex = "^[a-z0-9]+([._\\\\-]*[a-z0-9])*@([a-z0-9]+[.-]*[a-z0-9]+\\.){1,63}[a-z0-9]+$";
        return Pattern.matches(regex, email.toLowerCase());
    }

    // ==================== QQ邮箱校验 ====================

    /** QQ号最小长度 */
    public static final int QQ_MIN_LENGTH = 5;

    /** QQ号最大长度 */
    public static final int QQ_MAX_LENGTH = 11;

    /** 支持的QQ邮箱域名 */
    public static final String[] QQ_SUPPORTED_DOMAINS = {
            "qq.com",
            "vip.qq.com",
            "foxmail.com"
    };

    // 预编译Pattern，提高性能
    private static final Pattern QQ_EMAIL_STRICT_PATTERN =
            Pattern.compile("^[1-9]\\d{4,10}@(qq|vip\\.qq|foxmail)\\.com$", Pattern.CASE_INSENSITIVE);

    private static final Pattern QQ_EMAIL_LOOSE_PATTERN =
            Pattern.compile("^[1-9]\\d{4,10}@(?:[a-z0-9]+\\.)?(?:qq|foxmail)\\.com$", Pattern.CASE_INSENSITIVE);

    private static final Pattern QQ_NUMBER_PATTERN =
            Pattern.compile("^[1-9]\\d{4,10}$");

    /**
     * 校验是否为QQ邮箱（基础版）
     * 规则：5-11位数字@qq.com，第一位不能为0
     *
     * @param email 邮箱地址
     * @return true=是QQ邮箱
     */
    public static boolean isQQEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return QQ_EMAIL_STRICT_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * 校验是否为QQ邮箱（扩展版）
     * 支持：@qq.com, @vip.qq.com, @foxmail.com
     *
     * @param email 邮箱地址
     * @return true=是QQ邮箱
     */
    public static boolean isQQEmailExtended(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return QQ_EMAIL_STRICT_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * 校验是否为QQ邮箱（宽松版）
     * 支持所有以qq.com或foxmail.com结尾的域名
     *
     * @param email 邮箱地址
     * @return true=是QQ邮箱
     */
    public static boolean isQQEmailLoose(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return QQ_EMAIL_LOOSE_PATTERN.matcher(email.trim().toLowerCase()).matches();
    }

    /**
     * 校验是否为QQ邮箱（增强版，带详细错误信息）
     * @param email 邮箱地址
     * @return 校验结果对象
     */
    public static QQEmailCheckResult checkQQEmailDetail(String email) {
        QQEmailCheckResult result = new QQEmailCheckResult();
        result.setEmail(email);

        if (!StringUtils.hasText(email)) {
            result.setValid(false);
            result.setMessage("邮箱不能为空");
            return result;
        }

        email = email.trim();

        // 1. 检查是否包含@
        if (!email.contains("@")) {
            result.setValid(false);
            result.setMessage("邮箱格式不正确，缺少@符号");
            return result;
        }

        // 2. 分割
        String[] parts = email.split("@");
        if (parts.length != 2) {
            result.setValid(false);
            result.setMessage("邮箱格式不正确");
            return result;
        }

        String qqNumber = parts[0];
        String domain = parts[1].toLowerCase();

        // 3. 检查QQ号是否为纯数字
        if (!qqNumber.matches("\\d+")) {
            result.setValid(false);
            result.setMessage("QQ号必须为纯数字");
            return result;
        }

        // 4. 检查QQ号首位是否为0
        if (qqNumber.startsWith("0")) {
            result.setValid(false);
            result.setMessage("QQ号首位不能是0");
            return result;
        }

        // 5. 检查QQ号位数
        int length = qqNumber.length();
        if (length < 5 || length > 11) {
            result.setValid(false);
            result.setMessage("QQ号必须是5-11位数字，当前为" + length + "位");
            return result;
        }

        // 6. 检查域名
        if (!domain.equals("qq.com") && !domain.equals("vip.qq.com") && !domain.equals("foxmail.com")) {
            result.setValid(false);
            result.setMessage("只支持qq.com/vip.qq.com/foxmail.com域名");
            return result;
        }

        // 7. 额外提示：某些QQ号可能不存在
        result.setValid(true);
        result.setMessage("格式正确");
        result.setQqNumber(qqNumber);
        result.setQqNumberLength(length);
        result.setDomain(domain);

        // 添加一个警告：如果QQ号是9位，提示可能不存在
        if (length < 10) {
            result.setWarning("注意：较短的QQ号可能已停用或不存在，请确认QQ号正确");
        }

        return result;
    }

    /**
     * QQ邮箱校验结果类
     */
    public static class QQEmailCheckResult {
        private boolean valid;
        private String message;
        private String warning;
        private String email;
        private String qqNumber;
        private int qqNumberLength;
        private String domain;

        // getters and setters...
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getWarning() { return warning; }
        public void setWarning(String warning) { this.warning = warning; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getQqNumber() { return qqNumber; }
        public void setQqNumber(String qqNumber) { this.qqNumber = qqNumber; }

        public int getQqNumberLength() { return qqNumberLength; }
        public void setQqNumberLength(int qqNumberLength) { this.qqNumberLength = qqNumberLength; }

        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
    }
    /**
     * 从QQ邮箱中提取QQ号
     *
     * @param email QQ邮箱
     * @return QQ号，如果不是QQ邮箱返回null
     */
    public static String extractQQNumber(String email) {
        if (!isQQEmail(email)) {
            return null;
        }
        return email.trim().split("@")[0];
    }

    /**
     * 校验QQ号格式
     * 规则：5-11位数字，第一位不能为0
     *
     * @param qqNumber QQ号
     * @return true=合法QQ号
     */
    public static boolean isValidQQNumber(String qqNumber) {
        if (!StringUtils.hasText(qqNumber)) {
            return false;
        }
        return QQ_NUMBER_PATTERN.matcher(qqNumber.trim()).matches();
    }

    /**
     * 获取QQ号的位数
     *
     * @param qqNumber QQ号
     * @return 位数，如果不是合法QQ号返回-1
     */
    public static int getQQNumberLength(String qqNumber) {
        if (!isValidQQNumber(qqNumber)) {
            return -1;
        }
        return qqNumber.trim().length();
    }

    /**
     * 将QQ号转换为QQ邮箱格式
     *
     * @param qqNumber QQ号
     * @return QQ邮箱，如果不是合法QQ号返回null
     */
    public static String toQQEmail(String qqNumber) {
        if (!isValidQQNumber(qqNumber)) {
            return null;
        }
        return qqNumber.trim() + "@qq.com";
    }

    /**
     * 格式化QQ邮箱（转为小写）
     *
     * @param email QQ邮箱
     * @return 格式化后的邮箱
     * @throws IllegalArgumentException 如果不是有效QQ邮箱
     */
    public static String formatQQEmail(String email) {
        if (!isQQEmail(email)) {
            throw new IllegalArgumentException("无效的QQ邮箱: " + email);
        }
        return email.trim().toLowerCase();
    }

    /**
     * 校验QQ邮箱并返回详细结果
     *
     * @param email 邮箱地址
     * @return 校验结果对象
     */
    public static QQEmailValidationResult validateQQEmail(String email) {
        QQEmailValidationResult result = new QQEmailValidationResult();
        result.setOriginalEmail(email);

        if (!StringUtils.hasText(email)) {
            result.setValid(false);
            result.setMessage("邮箱地址不能为空");
            return result;
        }

        email = email.trim();

        // 1. 先检查基本邮箱格式
        if (!checkMail(email)) {
            result.setValid(false);
            result.setMessage("邮箱格式不正确");
            return result;
        }

        // 2. 分割用户名和域名
        String[] parts = email.split("@");
        if (parts.length != 2) {
            result.setValid(false);
            result.setMessage("邮箱格式错误");
            return result;
        }

        String username = parts[0];
        String domain = parts[1].toLowerCase();

        // 3. 检查QQ号部分
        if (!username.matches("\\d+")) {
            result.setValid(false);
            result.setMessage("QQ号必须为纯数字");
            return result;
        }

        if (username.startsWith("0")) {
            result.setValid(false);
            result.setMessage("QQ号首位不能是0");
            return result;
        }

        int length = username.length();
        if (length < QQ_MIN_LENGTH || length > QQ_MAX_LENGTH) {
            result.setValid(false);
            result.setMessage("QQ号必须是" + QQ_MIN_LENGTH + "-" + QQ_MAX_LENGTH + "位数字");
            return result;
        }

        // 4. 检查域名
        boolean domainValid = false;
        for (String supportedDomain : QQ_SUPPORTED_DOMAINS) {
            if (supportedDomain.equals(domain)) {
                domainValid = true;
                break;
            }
        }

        if (!domainValid) {
            result.setValid(false);
            result.setMessage("只支持以下域名: " + String.join(", ", QQ_SUPPORTED_DOMAINS));
            return result;
        }

        // 5. 全部通过
        result.setValid(true);
        result.setMessage("校验通过");
        result.setQqNumber(username);
        result.setQqNumberLength(length);
        result.setDomain(domain);
        result.setFormattedEmail(email.toLowerCase());

        return result;
    }

    // ==================== 内部类 ====================

    /**
     * QQ邮箱校验结果类
     */
    public static class QQEmailValidationResult {
        private boolean valid;
        private String message;
        private String originalEmail;
        private String formattedEmail;
        private String qqNumber;
        private int qqNumberLength;
        private String domain;

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getOriginalEmail() {
            return originalEmail;
        }

        public void setOriginalEmail(String originalEmail) {
            this.originalEmail = originalEmail;
        }

        public String getFormattedEmail() {
            return formattedEmail;
        }

        public void setFormattedEmail(String formattedEmail) {
            this.formattedEmail = formattedEmail;
        }

        public String getQqNumber() {
            return qqNumber;
        }

        public void setQqNumber(String qqNumber) {
            this.qqNumber = qqNumber;
        }

        public int getQqNumberLength() {
            return qqNumberLength;
        }

        public void setQqNumberLength(int qqNumberLength) {
            this.qqNumberLength = qqNumberLength;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        @Override
        public String toString() {
            return "QQEmailValidationResult{" +
                    "valid=" + valid +
                    ", message='" + message + '\'' +
                    ", originalEmail='" + originalEmail + '\'' +
                    ", formattedEmail='" + formattedEmail + '\'' +
                    ", qqNumber='" + qqNumber + '\'' +
                    ", qqNumberLength=" + qqNumberLength +
                    ", domain='" + domain + '\'' +
                    '}';
        }
    }
}