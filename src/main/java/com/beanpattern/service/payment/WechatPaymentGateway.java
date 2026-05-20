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
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * WeChat virtual payment gateway (Midas).
 */
@Component
public class WechatPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(WechatPaymentGateway.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MODE_COIN = "short_series_coin";
    private static final String MODE_GOODS = "short_series_goods";

    private final PaymentProperties paymentProperties;
    private final UserMapper userMapper;
    private final WechatAuthService wechatAuthService;

    public WechatPaymentGateway(PaymentProperties paymentProperties,
                                UserMapper userMapper,
                                WechatAuthService wechatAuthService) {
        this.paymentProperties = paymentProperties;
        this.userMapper = userMapper;
        this.wechatAuthService = wechatAuthService;
    }

    @Override
    public PaymentCreateResult createPayment(OrderEntity order) {
        PaymentProperties.Midas midas = paymentProperties.getMidas();

        if (midas.getOfferId() == null || midas.getOfferId().isBlank()) {
            throw new IllegalStateException("Midas offerId is not configured");
        }

        UserEntity user = userMapper.findById(order.getUserId());
        if (user == null || user.getOpenId() == null || user.getOpenId().isBlank()) {
            throw new IllegalStateException("User openid is missing");
        }

        String openid = user.getOpenId();
        String offerId = midas.getOfferId();
        int env = midas.isSandbox() ? 1 : 0;
        String appKey = midas.appKeyForEnv(env);
        if (appKey == null || appKey.isBlank()) {
            throw new IllegalStateException("Midas appKey is not configured");
        }

        String sessionKey = wechatAuthService.getSessionKey(openid);
        if (sessionKey == null || sessionKey.isBlank()) {
            throw new IllegalStateException("User session_key is missing, please login again");
        }

        String mode = resolveMode(midas);
        int goodsPrice = toCentAmount(order);
        int buyQuantity = MODE_COIN.equals(mode) ? goodsPrice : midas.getBuyQuantity();

        Map<String, Object> signDataMap = new LinkedHashMap<>();
        signDataMap.put("offerId", offerId);
        signDataMap.put("buyQuantity", buyQuantity);
        signDataMap.put("env", env);
        signDataMap.put("currencyType", "CNY");
        if (MODE_GOODS.equals(mode)) {
            signDataMap.put("productId", resolveProductId(order));
            signDataMap.put("goodsPrice", goodsPrice);
        }
        signDataMap.put("outTradeNo", order.getOrderNo());
        signDataMap.put("attach", openid);

        String signData;
        try {
            signData = objectMapper.writeValueAsString(signDataMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Midas signData", e);
        }

        String paySig = hmacSha256Hex("requestVirtualPayment&" + signData, appKey);
        String signature = hmacSha256Hex(signData, sessionKey);

        log.info("Midas payment params: orderNo={}, mode={}, buyQuantity={}, goodsPrice={}, signData={}",
                order.getOrderNo(), mode, buyQuantity, goodsPrice, signData);

        PaymentCreateResult result = new PaymentCreateResult();
        result.setOrderNo(order.getOrderNo());
        result.setStatus("PENDING");
        result.setProvider("midas");
        result.setMock(false);
        result.setSignData(signData);
        result.setPaySig(paySig);
        result.setSignature(signature);
        result.setMode(mode);
        return result;
    }

    private String resolveMode(PaymentProperties.Midas midas) {
        String mode = midas.getMode();
        if (mode == null || mode.isBlank()) {
            return MODE_COIN;
        }
        if (!MODE_COIN.equals(mode) && !MODE_GOODS.equals(mode)) {
            throw new IllegalStateException("Unsupported Midas mode: " + mode);
        }
        return mode;
    }

    private String resolveProductId(OrderEntity order) {
        if (order.getMidasProductId() != null && !order.getMidasProductId().isBlank()) {
            return order.getMidasProductId();
        }
        return order.getPackageCode();
    }

    private int toCentAmount(OrderEntity order) {
        return order.getAmount()
                .movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .intValueExact();
    }

    private static String hmacSha256Hex(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(spec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign HMAC-SHA256", e);
        }
    }
}
