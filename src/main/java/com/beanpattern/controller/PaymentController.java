package com.beanpattern.controller;

import com.beanpattern.config.PaymentProperties;
import com.beanpattern.entity.OrderEntity;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.mapper.OrderMapper;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;

/**
 * 支付回调接口 — 微信虚拟支付（米大师 Midas）。
 */
@RestController
@RequestMapping("/api/pay")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final OrderService orderService;
    private final PaymentProperties paymentProperties;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    public PaymentController(OrderService orderService,
                             PaymentProperties paymentProperties,
                             OrderMapper orderMapper,
                             UserMapper userMapper) {
        this.orderService = orderService;
        this.paymentProperties = paymentProperties;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
    }

    /**
     * 米大师发货回调（同时处理 GET 验证和 POST 通知）。
     */
    @GetMapping("/midas/notify")
    public String midasVerify(@RequestParam("signature") String signature,
                              @RequestParam("timestamp") String timestamp,
                              @RequestParam("nonce") String nonce,
                              @RequestParam("echostr") String echostr) {
        // URL 验证：SHA1(token, timestamp, nonce) == signature
        String token = paymentProperties.getMidas().getNotifyToken();
        if (token == null || token.isBlank()) {
            log.warn("米大师回调 Token 未配置，无法验证 URL");
            return "";
        }

        String[] arr = {token, timestamp, nonce};
        Arrays.sort(arr);
        String raw = arr[0] + arr[1] + arr[2];
        String calcSig = sha1Hex(raw);

        if (!calcSig.equals(signature)) {
            log.warn("米大师 URL 验证签名不匹配: expected={}, actual={}", signature, calcSig);
            return "";
        }

        log.info("米大师 URL 验证通过");
        return echostr;
    }

    @PostMapping("/midas/notify")
    public Map<String, Object> midasNotify(@RequestBody Map<String, Object> params) {
        try {
            log.info("收到米大师回调: {}", params);

            String msgType = (String) params.get("MsgType");
            String event = (String) params.get("Event");

            if (!"event".equals(msgType)) {
                log.warn("非事件消息，忽略: MsgType={}", msgType);
                return Map.of("ErrCode", -1, "ErrMsg", "非事件消息");
            }
            if (!"xpay_goods_deliver_notify".equals(event)) {
                log.warn("忽略非道具发货事件: Event={}", event);
                return Map.of("ErrCode", -1, "ErrMsg", "unsupported event");
            }

            // 提取 Payload 和 PayEventSig
            @SuppressWarnings("unchecked")
            Map<String, Object> miniGame = (Map<String, Object>) params.get("MiniGame");
            if (miniGame == null) {
                log.error("回调缺少 MiniGame 字段");
                return Map.of("ErrCode", -1, "ErrMsg", "缺少 MiniGame");
            }

            String payload = (String) miniGame.get("Payload");
            String payEventSig = (String) miniGame.get("PayEventSig");

            if (payload == null || payEventSig == null) {
                log.error("回调缺少 Payload 或 PayEventSig");
                return Map.of("ErrCode", -1, "ErrMsg", "参数不完整");
            }

            // 解析 Payload
            @SuppressWarnings("unchecked")
            Map<String, Object> payloadData = objectMapper.readValue(payload, Map.class);
            Integer env = toInteger(payloadData.get("Env"));

            // 验证 PayEventSig = HMAC-SHA256(AppKey, Event + "&" + Payload)
            String appKey = paymentProperties.getMidas().appKeyForEnv(env);
            if (appKey == null || appKey.isBlank()) {
                log.error("PayEventSig 验签失败: env={} 对应 AppKey 未配置", env);
                return Map.of("ErrCode", -1, "ErrMsg", "AppKey not configured");
            }
            String calcSig = hmacSha256Hex(event + "&" + payload, appKey);
            if (!calcSig.equals(payEventSig)) {
                log.error("PayEventSig 验证失败: expected={}, actual={}", payEventSig, calcSig);
                return Map.of("ErrCode", -1, "ErrMsg", "签名验证失败");
            }

            String outTradeNo = (String) payloadData.get("OutTradeNo");
            String openId = (String) payloadData.get("OpenId");
            String productId = (String) payloadData.get("ProductId");
            String transactionId = null;

            @SuppressWarnings("unchecked")
            Map<String, Object> payInfo = (Map<String, Object>) payloadData.get("WeChatPayInfo");
            if (payInfo != null) {
                transactionId = (String) payInfo.get("TransactionId");
            }
            if (transactionId == null) {
                transactionId = (String) payloadData.get("MchOrderNo");
            }

            log.info("米大师回调验签通过: outTradeNo={}, transactionId={}, event={}",
                    outTradeNo, transactionId, event);

            if (outTradeNo == null) {
                log.error("回调缺少 OutTradeNo");
                return Map.of("ErrCode", -1, "ErrMsg", "缺少 OutTradeNo");
            }
            OrderEntity order = orderMapper.findByOrderNo(outTradeNo);
            if (order == null) {
                log.error("米大师回调订单不存在: outTradeNo={}", outTradeNo);
                return Map.of("ErrCode", -1, "ErrMsg", "order not found");
            }
            UserEntity user = userMapper.findById(order.getUserId());
            if (user == null || user.getOpenId() == null || !user.getOpenId().equals(openId)) {
                log.error("米大师回调 OpenId 不匹配: orderNo={}, expected={}, actual={}",
                        outTradeNo, user != null ? user.getOpenId() : null, openId);
                return Map.of("ErrCode", -1, "ErrMsg", "openid mismatch");
            }
            String expectedProductId = order.getMidasProductId() != null && !order.getMidasProductId().isBlank()
                    ? order.getMidasProductId()
                    : order.getPackageCode();
            if (productId == null || !productId.equals(expectedProductId)) {
                log.error("米大师回调 ProductId 不匹配: orderNo={}, expected={}, actual={}",
                        outTradeNo, expectedProductId, productId);
                return Map.of("ErrCode", -1, "ErrMsg", "product mismatch");
            }
            int expectedEnv = paymentProperties.getMidas().isSandbox() ? 1 : 0;
            if (env != null && env != expectedEnv) {
                log.error("米大师回调 Env 不匹配: orderNo={}, expected={}, actual={}",
                        outTradeNo, expectedEnv, env);
                return Map.of("ErrCode", -1, "ErrMsg", "env mismatch");
            }

            orderService.handlePaymentCallback(outTradeNo, transactionId != null ? transactionId : outTradeNo);

            return Map.of("ErrCode", 0, "ErrMsg", "Success");

        } catch (Exception e) {
            log.error("处理米大师回调失败", e);
            return Map.of("ErrCode", -1, "ErrMsg", e.getMessage());
        }
    }

    /**
     * 查询订单支付状态（用于前端轮询）。
     */
    @GetMapping("/status/{orderNo}")
    public Map<String, Object> getPaymentStatus(@PathVariable String orderNo) {
        return orderService.getPaymentStatus(orderNo);
    }

    private static String sha1Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA1 失败", e);
        }
    }

    private static String hmacSha256Hex(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(spec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 失败", e);
        }
    }

    private static Integer toInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
