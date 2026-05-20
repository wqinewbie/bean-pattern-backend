package com.beanpattern.controller;

import com.beanpattern.config.PaymentProperties;
import com.beanpattern.entity.OrderEntity;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.mapper.OrderMapper;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.service.OrderService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Test
    void acceptsCoinCallbackWhenQuantityMatchesOrderAmount() {
        OrderService orderService = mock(OrderService.class);
        OrderMapper orderMapper = mock(OrderMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        PaymentController controller = new PaymentController(orderService, paymentProperties(), orderMapper, userMapper);

        OrderEntity order = order("ORD12345678", 7L, "0.99");
        UserEntity user = user(7L, "OPENID_1");
        when(orderMapper.findByOrderNo(order.getOrderNo())).thenReturn(order);
        when(userMapper.findById(order.getUserId())).thenReturn(user);

        Map<String, Object> result = controller.midasNotify(Map.of(
                "MsgType", "event",
                "Event", "xpay_coin_pay_notify",
                "OutTradeNo", order.getOrderNo(),
                "openid", user.getOpenId(),
                "env", 1,
                "GoodsInfo", Map.of("Quantity", 99),
                "WeChatPayInfo", Map.of("TransactionId", "TX123")
        ));

        assertEquals(0, result.get("ErrCode"));
        verify(orderService).handlePaymentCallback(order.getOrderNo(), "TX123");
    }

    @Test
    void rejectsCoinCallbackWhenQuantityDiffersFromOrderAmount() {
        OrderService orderService = mock(OrderService.class);
        OrderMapper orderMapper = mock(OrderMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        PaymentController controller = new PaymentController(orderService, paymentProperties(), orderMapper, userMapper);

        OrderEntity order = order("ORD12345678", 7L, "0.99");
        UserEntity user = user(7L, "OPENID_1");
        when(orderMapper.findByOrderNo(order.getOrderNo())).thenReturn(order);
        when(userMapper.findById(order.getUserId())).thenReturn(user);

        Map<String, Object> result = controller.midasNotify(Map.of(
                "MsgType", "event",
                "Event", "xpay_coin_pay_notify",
                "OutTradeNo", order.getOrderNo(),
                "openid", user.getOpenId(),
                "env", 1,
                "GoodsInfo", Map.of("Quantity", 98),
                "WeChatPayInfo", Map.of("TransactionId", "TX123")
        ));

        assertEquals(-1, result.get("ErrCode"));
        assertEquals("quantity mismatch", result.get("ErrMsg"));
        verify(orderService, never()).handlePaymentCallback(anyString(), anyString());
    }

    private PaymentProperties paymentProperties() {
        PaymentProperties properties = new PaymentProperties();
        properties.getMidas().setMode("short_series_coin");
        properties.getMidas().setSandbox(true);
        return properties;
    }

    private OrderEntity order(String orderNo, Long userId, String amount) {
        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAmount(new BigDecimal(amount));
        return order;
    }

    private UserEntity user(Long id, String openId) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setOpenId(openId);
        return user;
    }
}
