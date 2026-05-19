package com.beanpattern.service.payment;

import com.beanpattern.config.PaymentProperties;
import com.beanpattern.entity.OrderEntity;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.model.PaymentCreateResult;
import com.beanpattern.service.WechatAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信虚拟支付网关（米大师 Midas）。
 * 签名算法参考：https://developers.weixin.qq.com/miniprogram/dev/platform-capabilities/business-capabilities/virtual-payment.html
 */
@Component
public class WechatPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(WechatPaymentGateway.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final PaymentProperties paymentProperties;
    private final UserMapper userMapper;
    private final WechatAuthService wechatAuthService;

    public WechatPaymentGateway(PaymentProperties paymentProperties, UserMapper userMapper, WechatAuthService wechatAuthService) {
        this.paymentProperties = paymentProperties;
        this.userMapper = userMapper;
        this.wechatAuthService = wechatAuthService;
    }

    @Override
    public PaymentCreateResult createPayment(OrderEntity order) {
        PaymentProperties.Midas midas = paymentProperties.getMidas();

        if (midas.getOfferId() == null || midas.getOfferId().isBlank()) {
            throw new IllegalStateException("虚拟支付 OfferID 未配置");
        }

        UserEntity user = userMapper.findById(order.getUserId());
        if (user == null || user.getOpenId() == null || user.getOpenId().isBlank()) {
            throw new IllegalStateException("用户 openid 缺失");
        }

        String openid = user.getOpenId();
        String offerId = midas.getOfferId();
        int env = midas.isSandbox() ? 1 : 0;
        String appKey = midas.appKeyForEnv(env);
        if (appKey == null || appKey.isBlank()) {
            throw new IllegalStateException("虚拟支付 AppKey 未配置");
        }
        String productId = resolveProductId(order);

        // 获取 session_key
        String sessionKey = wechatAuthService.getSessionKey(openid);
        if (sessionKey == null || sessionKey.isBlank()) {
            throw new IllegalStateException("用户 session_key 缺失，请重新登录");
        }

        // goodsPrice 单位：分
        int goodsPrice = order.getAmount().movePointRight(2).intValue();

        // ---- 构建 signData（无空格 JSON）----
        Map<String, Object> signDataMap = new LinkedHashMap<>();
        signDataMap.put("offerId", offerId);
        signDataMap.put("buyQuantity", midas.getBuyQuantity());
        signDataMap.put("env", env);
        signDataMap.put("currencyType", "CNY");
        signDataMap.put("productId", productId);
        signDataMap.put("goodsPrice", goodsPrice);
        signDataMap.put("outTradeNo", order.getOrderNo());
        signDataMap.put("attach", openid);

        String signData;
        try {
            signData = objectMapper.writeValueAsString(signDataMap);
        } catch (Exception e) {
            throw new RuntimeException("signData 序列化失败", e);
        }

        // ---- paySig = HMAC-SHA256(AppKey, "requestVirtualPayment&" + signData) ----
        String paySig = hmacSha256Hex("requestVirtualPayment&" + signData, appKey);

        // ---- signature = HMAC-SHA256(session_key, signData) ----
        String signature = hmacSha256Hex(signData, sessionKey);

        log.info("Midas 支付参数: orderNo={}, productId={}, goodsPrice={}, signData={}",
                order.getOrderNo(), productId, goodsPrice, signData);

        PaymentCreateResult result = new PaymentCreateResult();
        result.setOrderNo(order.getOrderNo());
        result.setStatus("PENDING");
        result.setProvider("midas");
        result.setMock(false);
        result.setSignData(signData);
        result.setPaySig(paySig);
        result.setSignature(signature);
        result.setMode("short_series_goods");
        return result;
    }

    private String resolveProductId(OrderEntity order) {
        if (order.getMidasProductId() != null && !order.getMidasProductId().isBlank()) {
            return order.getMidasProductId();
        }
        return order.getPackageCode();
    }

    private static String hmacSha256Hex(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(spec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 签名失败", e);
        }
    }
}
