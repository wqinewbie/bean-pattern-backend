package com.beanpattern.service;

import com.beanpattern.config.AppProperties;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsCodeService {

    private static final String KEY_PREFIX = "sms:code:";
    private final SecureRandom random = new SecureRandom();
    private final Map<String, CodeRecord> localCodeStore = new ConcurrentHashMap<>();

    private final AppProperties appProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public SmsCodeService(AppProperties appProperties,
                          StringRedisTemplate stringRedisTemplate) {
        this.appProperties = appProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void sendCode(String phone) {
        if (!phone.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        AppProperties.Sms sms = appProperties.getSms();
        long ttlSeconds = Math.max(60, sms.getCodeTtlSeconds());

        if (!sms.isEnabled()) {
            saveCode(phone, code, ttlSeconds);
            return;
        }

        if (!StringUtils.hasText(sms.getSecretId()) || !StringUtils.hasText(sms.getSecretKey())
                || !StringUtils.hasText(sms.getSdkAppId()) || !StringUtils.hasText(sms.getSignName())
                || !StringUtils.hasText(sms.getTemplateId())) {
            throw new IllegalStateException("短信配置不完整");
        }

        try {
            Credential cred = new Credential(sms.getSecretId(), sms.getSecretKey());
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            SmsClient client = new SmsClient(cred, sms.getRegion(), clientProfile);

            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(sms.getSdkAppId());
            req.setSignName(sms.getSignName());
            req.setTemplateId(sms.getTemplateId());
            req.setTemplateParamSet(new String[]{code, String.valueOf(Math.max(1, sms.getCodeTtlSeconds() / 60))});
            req.setPhoneNumberSet(new String[]{"+86" + phone});

            SendSmsResponse resp = client.SendSms(req);
            if (resp.getSendStatusSet() == null || resp.getSendStatusSet().length == 0
                    || !"Ok".equalsIgnoreCase(resp.getSendStatusSet()[0].getCode())) {
                String reason = (resp.getSendStatusSet() != null && resp.getSendStatusSet().length > 0)
                        ? resp.getSendStatusSet()[0].getMessage()
                        : "未知错误";
                throw new IllegalStateException("短信发送失败: " + reason);
            }

            saveCode(phone, code, ttlSeconds);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("短信发送失败: " + e.getMessage(), e);
        }
    }

    public boolean verifyCode(String phone, String code) {
        if (!phone.matches("^1\\d{10}$")) return false;
        if (!StringUtils.hasText(code)) return false;

        CodeRecord localRecord = localCodeStore.get(KEY_PREFIX + phone);
        if (localRecord != null) {
            if (localRecord.isExpired()) {
                localCodeStore.remove(KEY_PREFIX + phone);
                return false;
            }
            if (!code.equals(localRecord.code())) return false;
            localCodeStore.remove(KEY_PREFIX + phone);
            return true;
        }

        String key = KEY_PREFIX + phone;
        try {
            String saved = stringRedisTemplate.opsForValue().get(key);
            if (!code.equals(saved)) return false;
            stringRedisTemplate.delete(key);
            return true;
        } catch (RedisConnectionFailureException | DataAccessException ex) {
            return false;
        }
    }

    private void saveCode(String phone, String code, long ttlSeconds) {
        String key = KEY_PREFIX + phone;
        try {
            stringRedisTemplate.opsForValue().set(key, code, Duration.ofSeconds(ttlSeconds));
            localCodeStore.remove(key);
        } catch (RedisConnectionFailureException | DataAccessException ex) {
            localCodeStore.put(key, new CodeRecord(code, Instant.now().plusSeconds(ttlSeconds)));
        }
    }

    private record CodeRecord(String code, Instant expireAt) {
        private boolean isExpired() {
            return Instant.now().isAfter(expireAt);
        }
    }
}
