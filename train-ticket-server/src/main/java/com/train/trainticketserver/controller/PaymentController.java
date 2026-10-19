package com.train.trainticketserver.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.train.trainticketserver.config.AlipayConfig;
import com.train.trainticketserver.dto.PaymentDTO;
import com.train.trainticketserver.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final AlipayConfig alipayConfig;

    public PaymentController(
            PaymentService paymentService,
            AlipayClient alipayClient,
            AlipayConfig alipayConfig
    ) {
        this.paymentService = paymentService;
        this.alipayConfig = alipayConfig;
    }

    @PostMapping("/create/{orderId}")
    public String createPayment(@PathVariable Long orderId) {
        return paymentService.createPayment(orderId);
    }

    @PostMapping("/createTransfer/{firstOrderId}/{secondOrderId}")
    public String createTransferPayment(
            @PathVariable Long firstOrderId,
            @PathVariable Long secondOrderId
    ) {
        return paymentService.createTransferPayment(
                firstOrderId,
                secondOrderId
        );
    }

    @PutMapping("/change/pay/{orderId}")
    public String changePay(
            @PathVariable Long orderId,
            @RequestBody PaymentDTO dto
    ) {
        return paymentService.createChangePayment(
                orderId,
                dto.getOldOrderId()
        );
    }

    @PostMapping("/refund/{orderId}")
    public void refund(@PathVariable Long orderId) {
        paymentService.refund(orderId);
    }

    @PostMapping("/change/refund/{orderId}/{oldOrderId}")
    public void changeRefund(
            @PathVariable Long orderId,
            @PathVariable Long oldOrderId
    ) {
        paymentService.changeRefund(orderId, oldOrderId);
    }

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {

        Map<String, String> params = new HashMap<>();

        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });

        try {

            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2"
            );

            if (!signVerified) {
                System.out.println("支付宝签名验证失败");
                return "failure";
            }

            String tradeStatus =
                    params.get("trade_status");

            if (!"TRADE_SUCCESS".equals(tradeStatus)
                    && !"TRADE_FINISHED".equals(tradeStatus)) {
                return "failure";
            }

            String outTradeNo =
                    params.get("out_trade_no");

            if (outTradeNo == null) {
                return "failure";
            }

            if (outTradeNo.startsWith("TRANSFER_")) {

                String[] parts = outTradeNo.split("_");

                if (parts.length != 3) {
                    return "failure";
                }

                String totalAmount = params.get("total_amount");

                if (totalAmount == null) {
                    return "failure";
                }

                Long firstOrderId = Long.valueOf(parts[1]);
                Long secondOrderId = Long.valueOf(parts[2]);

                paymentService.payTransferSuccess(
                        firstOrderId,
                        secondOrderId,
                        new BigDecimal(totalAmount),
                        "支付宝"
                );

            } else if (outTradeNo.startsWith("CHANGE_")) {

                String[] parts =
                        outTradeNo.split("_");

                if (parts.length != 3) {
                    return "failure";
                }

                Long orderId =
                        Long.valueOf(parts[1]);

                Long oldOrderId =
                        Long.valueOf(parts[2]);

                System.out.println(
                        "改签支付成功，新订单："
                                + orderId
                                + "，原订单："
                                + oldOrderId
                );

                paymentService.changeWithPay(
                        orderId,
                        oldOrderId,
                        "支付宝"
                );

            } else {

                Long orderId =
                        Long.valueOf(outTradeNo);

                System.out.println(
                        "普通购票支付成功，订单："
                                + orderId
                );

                paymentService.paySuccess(
                        orderId,
                        "支付宝"
                );
            }

            return "success";

        } catch (AlipayApiException e) {

            e.printStackTrace();

            return "failure";

        } catch (Exception e) {

            e.printStackTrace();

            return "failure";
        }
    }
}
