package com.beanpattern.service;

import com.beanpattern.config.AppProperties;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class SmsCodeService {

    private static final String KEY_PREFIX = "sms:code:";
    private final SecureRandom random = new SecureRandom();

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

        if (!sms.isEnabled()) {
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + phone, code,
                    Duration.ofSeconds(Math.max(60, sms.getCodeTtlSeconds())));
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

            stringRedisTemplate.opsForValue().set(KEY_PREFIX + phone, code,
                    Duration.ofSeconds(Math.max(60, sms.getCodeTtlSeconds())));
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("短信发送失败: " + e.getMessage(), e);
        }
    }

    public boolean verifyCode(String phone, String code) {
        if (!phone.matches("^1\\d{10}$")) return false;
        if (!StringUtils.hasText(code)) return false;
        String key = KEY_PREFIX + phone;
        String saved = stringRedisTemplate.opsForValue().get(key);
        if (!code.equals(saved)) return false;
        stringRedisTemplate.delete(key);
        return true;
    }
}
