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
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;

/**
 * WeChat virtual payment callback endpoint for Midas.
 */
@RestController
@RequestMapping("/api/pay")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String MODE_COIN = "short_series_coin";
    private static final String MODE_GOODS = "short_series_goods";
    private static final String EVENT_COIN_PAY = "xpay_coin_pay_notify";
    private static final String EVENT_GOODS_DELIVER = "xpay_goods_deliver_notify";

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

    @GetMapping("/midas/notify")
    public String midasVerify(@RequestParam("signature") String signature,
                              @RequestParam("timestamp") String timestamp,
                              @RequestParam("nonce") String nonce,
                              @RequestParam("echostr") String echostr) {
        String token = paymentProperties.getMidas().getNotifyToken();
        if (token == null || token.isBlank()) {
            log.warn("Midas notify token is not configured");
            return "";
        }

        String[] arr = {token, timestamp, nonce};
        Arrays.sort(arr);
        String calcSig = sha1Hex(arr[0] + arr[1] + arr[2]);
        if (!calcSig.equals(signature)) {
            log.warn("Midas URL verification failed: expected={}, actual={}", signature, calcSig);
            return "";
        }
        return echostr;
    }

    @PostMapping("/midas/notify")
    public Map<String, Object> midasNotify(@RequestBody Map<String, Object> params) {
        try {
            log.info("Received Midas callback: {}", params);

            String msgType = firstString(valueOf(params, "MsgType"), valueOf(params, "msgType"));
            String event = firstString(valueOf(params, "Event"), valueOf(params, "event"));
            if (!"event".equals(msgType)) {
                log.warn("Unsupported Midas message type: {}", msgType);
                return fail("unsupported message type");
            }
            if (!EVENT_GOODS_DELIVER.equals(event) && !EVENT_COIN_PAY.equals(event)) {
                log.warn("Unsupported Midas event: {}", event);
                return fail("unsupported event");
            }

            CallbackPayload callback = parsePayload(params, event);
            Map<String, Object> payloadData = callback.payloadData();
            Integer env = callback.env();

            String outTradeNo = firstString(valueOf(payloadData, "OutTradeNo"), valueOf(payloadData, "outTradeNo"));
            if (outTradeNo == null || outTradeNo.isBlank()) {
                log.error("Midas callback missing OutTradeNo");
                return fail("missing OutTradeNo");
            }

            OrderEntity order = orderMapper.findByOrderNo(outTradeNo);
            if (order == null) {
                log.error("Midas callback order not found: orderNo={}", outTradeNo);
                return fail("order not found");
            }

            String openId = firstString(
                    valueOf(payloadData, "OpenId"),
                    valueOf(payloadData, "openid"),
                    valueOf(payloadData, "openId")
            );
            UserEntity user = userMapper.findById(order.getUserId());
            if (user == null || user.getOpenId() == null || !user.getOpenId().equals(openId)) {
                log.error("Midas callback openid mismatch: orderNo={}, expected={}, actual={}",
                        outTradeNo, user != null ? user.getOpenId() : null, openId);
                return fail("openid mismatch");
            }

            int expectedEnv = paymentProperties.getMidas().isSandbox() ? 1 : 0;
            if (env != null && env != expectedEnv) {
                log.error("Midas callback env mismatch: orderNo={}, expected={}, actual={}",
                        outTradeNo, expectedEnv, env);
                return fail("env mismatch");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> goodsInfo = (Map<String, Object>) valueOf(payloadData, "GoodsInfo");
            @SuppressWarnings("unchecked")
            Map<String, Object> coinInfo = (Map<String, Object>) valueOf(payloadData, "CoinInfo");

            String midasMode = normalizeMode(paymentProperties.getMidas().getMode());
            if (MODE_GOODS.equals(midasMode)) {
                Map<String, Object> info = goodsInfo != null ? goodsInfo : payloadData;
                validateGoodsCallback(outTradeNo, event, order, info);
            } else if (MODE_COIN.equals(midasMode)) {
                Map<String, Object> info = coinInfo != null ? coinInfo : (goodsInfo != null ? goodsInfo : payloadData);
                validateCoinCallback(outTradeNo, event, order, info);
            } else {
                log.error("Unsupported Midas mode: {}", midasMode);
                return fail("unsupported mode");
            }

            String transactionId = resolveTransactionId(payloadData);
            orderService.handlePaymentCallback(outTradeNo, transactionId != null ? transactionId : outTradeNo);
            return success();
        } catch (Exception e) {
            log.error("Failed to handle Midas callback", e);
            return fail(e.getMessage());
        }
    }

    @GetMapping("/status/{orderNo}")
    public Map<String, Object> getPaymentStatus(@PathVariable String orderNo) {
        return orderService.getPaymentStatus(orderNo);
    }

    private CallbackPayload parsePayload(Map<String, Object> params, String event) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> miniGame = firstMap(valueOf(params, "MiniGame"), valueOf(params, "miniGame"));
        if (miniGame == null) {
            return new CallbackPayload(params, firstInteger(valueOf(params, "Env"), valueOf(params, "env")));
        }

        String payload = firstString(valueOf(miniGame, "Payload"), valueOf(miniGame, "payload"));
        String payEventSig = firstString(valueOf(miniGame, "PayEventSig"), valueOf(miniGame, "payEventSig"));
        if (payload == null || payEventSig == null) {
            throw new IllegalArgumentException("missing Payload or PayEventSig");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> payloadData = objectMapper.readValue(payload, Map.class);
        Integer env = firstInteger(valueOf(payloadData, "Env"), valueOf(payloadData, "env"));

        String appKey = paymentProperties.getMidas().appKeyForEnv(env);
        if (appKey == null || appKey.isBlank()) {
            throw new IllegalStateException("Midas appKey is not configured");
        }
        String calcSig = hmacSha256Hex(event + "&" + payload, appKey);
        if (!calcSig.equals(payEventSig)) {
            throw new IllegalArgumentException("PayEventSig mismatch");
        }
        return new CallbackPayload(payloadData, env);
    }

    private void validateGoodsCallback(String orderNo, String event, OrderEntity order, Map<String, Object> info) {
        if (!EVENT_GOODS_DELIVER.equals(event)) {
            throw new IllegalArgumentException("event mismatch");
        }
        String productId = firstString(
                valueOf(info, "ProductId"),
                valueOf(info, "productId"),
                valueOf(info, "productid")
        );
        String expectedProductId = order.getMidasProductId() != null && !order.getMidasProductId().isBlank()
                ? order.getMidasProductId()
                : order.getPackageCode();
        if (!expectedProductId.equals(productId)) {
            log.error("Midas ProductId mismatch: orderNo={}, expected={}, actual={}",
                    orderNo, expectedProductId, productId);
            throw new IllegalArgumentException("product mismatch");
        }
    }

    private void validateCoinCallback(String orderNo, String event, OrderEntity order, Map<String, Object> info) {
        if (!EVENT_COIN_PAY.equals(event) && !EVENT_GOODS_DELIVER.equals(event)) {
            throw new IllegalArgumentException("event mismatch");
        }

        int expectedQuantity = toCentAmount(order);
        Integer paidQuantity = firstInteger(
                valueOf(info, "BuyQuantity"),
                valueOf(info, "buyQuantity"),
                valueOf(info, "Quantity"),
                valueOf(info, "quantity")
        );
        if (paidQuantity == null) {
            log.error("Midas coin quantity missing: orderNo={}, info={}", orderNo, info);
            throw new IllegalArgumentException("quantity missing");
        }
        if (paidQuantity != expectedQuantity) {
            log.error("Midas coin quantity mismatch: orderNo={}, expected={}, actual={}",
                    orderNo, expectedQuantity, paidQuantity);
            throw new IllegalArgumentException("quantity mismatch");
        }
    }

    private static String resolveTransactionId(Map<String, Object> payloadData) {
        Map<String, Object> payInfo = firstMap(
                valueOf(payloadData, "WeChatPayInfo"),
                valueOf(payloadData, "wechatPayInfo"),
                valueOf(payloadData, "weChatPayInfo")
        );
        String transactionId = firstString(
                valueOf(payInfo, "TransactionId"),
                valueOf(payInfo, "transactionId"),
                valueOf(payloadData, "TransactionId"),
                valueOf(payloadData, "transactionId")
        );
        if (transactionId != null) {
            return transactionId;
        }
        return firstString(valueOf(payInfo, "MchOrderNo"), valueOf(payloadData, "MchOrderNo"));
    }

    private static String normalizeMode(String mode) {
        return mode == null || mode.isBlank() ? MODE_COIN : mode;
    }

    private static Map<String, Object> success() {
        return Map.of("ErrCode", 0, "ErrMsg", "Success");
    }

    private static Map<String, Object> fail(String message) {
        return Map.of("ErrCode", -1, "ErrMsg", message != null ? message : "fail");
    }

    private static Object valueOf(Map<String, Object> map, String key) {
        return map != null ? map.get(key) : null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstMap(Object... values) {
        for (Object value : values) {
            if (value instanceof Map<?, ?>) {
                return (Map<String, Object>) value;
            }
        }
        return null;
    }

    private static String firstString(Object... values) {
        for (Object value : values) {
            if (value instanceof String text && !text.isBlank()) {
                return text;
            }
        }
        return null;
    }

    private static Integer firstInteger(Object... values) {
        for (Object value : values) {
            Integer parsed = toInteger(value);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private static int toCentAmount(OrderEntity order) {
        return order.getAmount()
                .movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .intValueExact();
    }

    private static Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
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

    private static String sha1Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA1 failed", e);
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
            throw new RuntimeException("HMAC-SHA256 failed", e);
        }
    }

    private record CallbackPayload(Map<String, Object> payloadData, Integer env) {
    }
}
