package com.train.trainticketserver.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.train.trainticketserver.entity.Payment;
import com.train.trainticketserver.entity.Refund;
import com.train.trainticketserver.entity.Ticket;
import com.train.trainticketserver.entity.TicketOrder;
import com.train.trainticketserver.mapper.PaymentMapper;
import com.train.trainticketserver.mapper.RefundMapper;
import com.train.trainticketserver.mapper.SeatOccupancyMapper;
import com.train.trainticketserver.mapper.TicketMapper;
import com.train.trainticketserver.mapper.TicketOrderMapper;
import com.train.trainticketserver.mapper.TrainStationMapper;
import com.train.trainticketserver.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${alipay.return-url}")
    private String returnUrl;

    @Value("${alipay.notify-url:}")
    private String notifyUrl;

    private static final Logger log =
            LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final AlipayClient alipayClient;
    private final TicketMapper ticketMapper;
    private final TicketOrderMapper ticketOrderMapper;
    private final PaymentMapper paymentMapper;
    private final SeatOccupancyMapper seatOccupancyMapper;
    private final TrainStationMapper trainStationMapper;
    private final RefundMapper refundMapper;

    public PaymentServiceImpl(
            AlipayClient alipayClient,
            TicketMapper ticketMapper,
            TicketOrderMapper ticketOrderMapper,
            PaymentMapper paymentMapper,
            SeatOccupancyMapper seatOccupancyMapper,
            TrainStationMapper trainStationMapper,
            RefundMapper refundMapper
    ) {
        this.alipayClient = alipayClient;
        this.ticketMapper = ticketMapper;
        this.ticketOrderMapper = ticketOrderMapper;
        this.paymentMapper = paymentMapper;
        this.seatOccupancyMapper = seatOccupancyMapper;
        this.trainStationMapper = trainStationMapper;
        this.refundMapper = refundMapper;
    }

    @Override
    public String createPayment(Long orderId) {

        Ticket ticket = ticketMapper.findByOrderId(orderId);

        if (ticket == null) {
            throw new RuntimeException("订单不存在");
        }

        TicketOrder order = ticketOrderMapper.findById(orderId);

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!"待支付".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许支付");
        }

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);
        String outTradeNo = String.valueOf(orderId);
        String totalAmount = ticket.getPrice().toString();
        String subject = "12704火车票订单-" + orderId;
        String bizContent =
                "{"
                        + "\"out_trade_no\":\""
                        + outTradeNo
                        + "\","
                        + "\"total_amount\":\""
                        + totalAmount
                        + "\","
                        + "\"subject\":\""
                        + subject
                        + "\","
                        + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                        + "}";
        request.setBizContent(bizContent);

        try {
            AlipayTradePagePayResponse response =
                    alipayClient.pageExecute(request);

            if (!response.isSuccess()) {
                throw new RuntimeException(
                        "支付宝支付创建失败："
                                + response.getSubMsg()
                );
            }

            return response.getBody();

        } catch (AlipayApiException e) {
            throw new RuntimeException(
                    "支付宝支付请求失败",
                    e
            );
        }
    }

    @Override
    public String createTransferPayment(
            Long firstOrderId,
            Long secondOrderId
    ) {
        if (firstOrderId.equals(secondOrderId)) {
            throw new RuntimeException("中转订单不能使用相同的订单号");
        }

        Ticket firstTicket = ticketMapper.findByOrderId(firstOrderId);
        Ticket secondTicket = ticketMapper.findByOrderId(secondOrderId);

        if (firstTicket == null || secondTicket == null) {
            throw new RuntimeException("中转订单不存在");
        }

        TicketOrder firstOrder = ticketOrderMapper.findById(firstOrderId);
        TicketOrder secondOrder = ticketOrderMapper.findById(secondOrderId);

        if (firstOrder == null || secondOrder == null) {
            throw new RuntimeException("中转订单不存在");
        }

        if (!firstOrder.getUserId().equals(secondOrder.getUserId())) {
            throw new RuntimeException("两段订单不属于同一用户");
        }

        if (!"待支付".equals(firstOrder.getStatus())
                || !"待支付".equals(secondOrder.getStatus())) {
            throw new RuntimeException("中转订单状态不允许支付");
        }

        BigDecimal totalAmount = firstTicket.getPrice()
                .add(secondTicket.getPrice());
        String outTradeNo =
                "TRANSFER_" + firstOrderId + "_" + secondOrderId;

        AlipayTradePagePayRequest request =
                new AlipayTradePagePayRequest();

        request.setReturnUrl(
                returnUrl
                        + (returnUrl.contains("?") ? "&" : "?") + "transfer=true"
                        + "&firstOrderId=" + firstOrderId
                        + "&secondOrderId=" + secondOrderId
                        + "&from=payment"
        );
        request.setNotifyUrl(
                notifyUrl
        );

        String bizContent =
                "{"
                        + "\"out_trade_no\":\"" + outTradeNo + "\","
                        + "\"total_amount\":\"" + totalAmount + "\","
                        + "\"subject\":\"12704火车票中转订单-"
                        + firstOrderId + "-" + secondOrderId + "\","
                        + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                        + "}";

        request.setBizContent(bizContent);

        try {
            AlipayTradePagePayResponse response =
                    alipayClient.pageExecute(request);

            if (!response.isSuccess()) {
                throw new RuntimeException(
                        "支付宝中转票支付创建失败："
                                + response.getSubMsg()
                );
            }

            return response.getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException(
                    "支付宝中转票支付请求失败",
                    e
            );
        }
    }

    @Override
    @Transactional
    public void paySuccess(
            Long orderId,
            String payMethod
    ) {

        Ticket ticket =
                ticketMapper.findByOrderId(orderId);

        if (ticket == null) {
            throw new RuntimeException("订单不存在");
        }

        TicketOrder order =
                ticketOrderMapper.findById(orderId);

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!"待支付".equals(order.getStatus())) {
            log.info(
                    "忽略重复支付通知，orderId={}，当前状态={}",
                    orderId,
                    order.getStatus()
            );
            return;
        }

        ticketOrderMapper.payOrder(orderId);
        ticketMapper.payTicket(orderId);

        Payment payment = new Payment();

        payment.setOrderId(orderId);
        payment.setAmount(ticket.getPrice());
        payment.setPayMethod(payMethod);
        payment.setOutTradeNo(String.valueOf(orderId));
        payment.setPayTime(LocalDateTime.now());

        paymentMapper.insert(payment);
    }

    @Override
    @Transactional
    public void payTransferSuccess(
            Long firstOrderId,
            Long secondOrderId,
            BigDecimal paidAmount,
            String payMethod
    ) {
        if (firstOrderId.equals(secondOrderId)) {
            throw new RuntimeException("中转订单号无效");
        }

        Ticket firstTicket = ticketMapper.findByOrderId(firstOrderId);
        Ticket secondTicket = ticketMapper.findByOrderId(secondOrderId);
        TicketOrder firstOrder = ticketOrderMapper.findById(firstOrderId);
        TicketOrder secondOrder = ticketOrderMapper.findById(secondOrderId);

        if (firstTicket == null || secondTicket == null
                || firstOrder == null || secondOrder == null) {
            throw new RuntimeException("中转订单不存在");
        }

        if (!firstOrder.getUserId().equals(secondOrder.getUserId())) {
            throw new RuntimeException("两段订单不属于同一用户");
        }

        if ("已支付".equals(firstOrder.getStatus())
                && "已支付".equals(secondOrder.getStatus())) {
            log.info(
                    "忽略重复中转支付通知，第一程订单={}，第二程订单={}",
                    firstOrderId,
                    secondOrderId
            );
            return;
        }

        if (!"待支付".equals(firstOrder.getStatus())
                || !"待支付".equals(secondOrder.getStatus())) {
            throw new RuntimeException("中转订单状态不一致，无法确认支付");
        }

        BigDecimal expectedAmount = firstTicket.getPrice()
                .add(secondTicket.getPrice());

        if (paidAmount == null
                || expectedAmount.compareTo(paidAmount) != 0) {
            throw new RuntimeException("中转订单支付金额不一致");
        }

        ticketOrderMapper.payOrder(firstOrderId);
        ticketMapper.payTicket(firstOrderId);
        ticketOrderMapper.payOrder(secondOrderId);
        ticketMapper.payTicket(secondOrderId);

        String outTradeNo =
                "TRANSFER_" + firstOrderId + "_" + secondOrderId;
        LocalDateTime payTime = LocalDateTime.now();

        Payment firstPayment = new Payment();
        firstPayment.setOrderId(firstOrderId);
        firstPayment.setAmount(firstTicket.getPrice());
        firstPayment.setPayMethod(payMethod);
        firstPayment.setOutTradeNo(outTradeNo);
        firstPayment.setPayTime(payTime);
        paymentMapper.insert(firstPayment);

        Payment secondPayment = new Payment();
        secondPayment.setOrderId(secondOrderId);
        secondPayment.setAmount(secondTicket.getPrice());
        secondPayment.setPayMethod(payMethod);
        secondPayment.setOutTradeNo(outTradeNo);
        secondPayment.setPayTime(payTime);
        paymentMapper.insert(secondPayment);
    }

    @Override
    public String createChangePayment(
            Long orderId,
            Long oldOrderId
    ) {

        Ticket newTicket =
                ticketMapper.findByOrderId(orderId);

        Ticket oldTicket =
                ticketMapper.findByOrderId(oldOrderId);

        if (newTicket == null) {
            throw new RuntimeException("改签订单不存在");
        }

        if (oldTicket == null) {
            throw new RuntimeException("原订单不存在");
        }

        BigDecimal diffPrice = newTicket.getPrice().subtract(oldTicket.getPrice());
        if (diffPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("该订单无需支付差价");
        }
        TicketOrder newOrder = ticketOrderMapper.findById(orderId);
        if (newOrder == null) {throw new RuntimeException("订单不存在");}
        if (!"待支付".equals(newOrder.getStatus())) {
            throw new RuntimeException("订单状态不允许支付");}
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);
        String outTradeNo = "CHANGE_" + orderId + "_" + oldOrderId;
        String totalAmount = diffPrice.toString();
        String subject = "12704火车票改签差价-" + orderId;
        String bizContent =
                "{"
                        + "\"out_trade_no\":\""
                        + outTradeNo
                        + "\","
                        + "\"total_amount\":\""
                        + totalAmount
                        + "\","
                        + "\"subject\":\""
                        + subject
                        + "\","
                        + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                        + "}";

        request.setBizContent(bizContent);
        try {
            AlipayTradePagePayResponse response =alipayClient.pageExecute(request);
            if (!response.isSuccess()) {
                throw new RuntimeException("支付宝改签支付创建失败："+ response.getSubMsg());}
            return response.getBody();

        } catch (AlipayApiException e) {
            throw new RuntimeException(
                    "支付宝改签支付请求失败",
                    e
            );
        }
    }

    @Override
    @Transactional
    public void changeWithPay(
            Long orderId,
            Long oldOrderId,
            String payMethod
    ) {

        Ticket newTicket =
                ticketMapper.findByOrderId(orderId);

        Ticket oldTicket =
                ticketMapper.findByOrderId(oldOrderId);

        if (newTicket == null) {
            throw new RuntimeException("改签订单不存在");
        }

        if (oldTicket == null) {
            throw new RuntimeException("原订单不存在");
        }

        TicketOrder newOrder =
                ticketOrderMapper.findById(orderId);

        TicketOrder oldOrder =
                ticketOrderMapper.findById(oldOrderId);

        if (newOrder == null) {
            throw new RuntimeException("改签订单不存在");
        }

        if (oldOrder == null) {
            throw new RuntimeException("原订单不存在");
        }
        if (!"待支付".equals(newOrder.getStatus())) {
            log.info(
                    "忽略重复改签支付通知，新订单={}，当前状态={}",
                    orderId,
                    newOrder.getStatus()
            );
            return;
        }

        if (!"已支付".equals(oldOrder.getStatus())) {
            throw new RuntimeException(
                    "原订单状态不允许改签"
            );
        }

        BigDecimal diffPrice =
                newTicket.getPrice()
                        .subtract(oldTicket.getPrice());

        if (diffPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("改签差价错误");
        }

        ticketOrderMapper.changeOrder(oldOrderId);
        ticketMapper.changeTicket(oldOrderId);
        Integer startOrder =
                trainStationMapper.findStopOrderById(
                        oldTicket.getScheduleId(),
                        oldTicket.getStartStationId()
                );

        Integer endOrder =
                trainStationMapper.findStopOrderById(
                        oldTicket.getScheduleId(),
                        oldTicket.getEndStationId()
                );

        seatOccupancyMapper.deleteOccupancy(
                oldTicket.getScheduleId(),
                oldTicket.getSeatId(),
                startOrder,
                endOrder
        );
        ticketOrderMapper.payChangeOrder(orderId);
        ticketMapper.payChangeTicket(orderId);
        Payment payment = new Payment();

        payment.setOrderId(orderId);
        payment.setAmount(diffPrice);
        payment.setPayMethod(payMethod);
        payment.setOutTradeNo(
                "CHANGE_" + orderId + "_" + oldOrderId
        );
        payment.setPayTime(LocalDateTime.now());

        paymentMapper.insert(payment);
    }

    @Override
    @Transactional
    public void refund(Long orderId) {
        TicketOrder order = ticketOrderMapper.findById(orderId);
        if (order == null) {throw new RuntimeException("订单不存在");}
        if (!"已支付".equals(order.getStatus())
                && !"改签票".equals(order.getStatus())) {
            throw new RuntimeException("当前订单不允许退票");}
        Ticket ticket = ticketMapper.findByOrderId(orderId);
        if (ticket == null) {throw new RuntimeException("车票不存在");}
        Payment payment = paymentMapper.findByOrderId(orderId);
        if (payment == null) {
            throw new RuntimeException("支付记录不存在");}
        if (!"支付宝".equals(payment.getPayMethod())) {
            throw new RuntimeException("当前订单不是支付宝支付");}
        BigDecimal refundAmount = ticket.getPrice();
        String outTradeNo = payment.getOutTradeNo();
        if (outTradeNo == null || outTradeNo.isBlank()) {
            throw new RuntimeException("支付记录缺少支付宝交易号");}
        if (outTradeNo.startsWith("CHANGE_")) {
            String[] parts = outTradeNo.split("_");
            if (parts.length != 3) {
                throw new RuntimeException("改签支付交易号格式错误");}
            Long oldOrderId;
            try {
                oldOrderId = Long.valueOf(parts[2]);

            } catch (NumberFormatException e) {
                throw new RuntimeException(
                        "改签支付交易号中的原订单ID无效",
                        e
                );
            }

            Payment oldPayment =
                    paymentMapper.findByOrderId(oldOrderId);

            if (oldPayment == null) {
                throw new RuntimeException(
                        "原订单支付记录不存在"
                );
            }

            if (!"支付宝".equals(
                    oldPayment.getPayMethod()
            )) {
                throw new RuntimeException(
                        "原订单不是支付宝支付"
                );
            }

            if (oldPayment.getOutTradeNo() == null
                    || oldPayment.getOutTradeNo().isBlank()) {
                throw new RuntimeException(
                        "原订单支付记录缺少支付宝交易号"
                );
            }
            refundAlipayTrade(
                    oldPayment.getOutTradeNo(),
                    oldPayment.getAmount(),
                    "TICKET_REFUND_"
                            + orderId
                            + "_ORIGINAL",
                    "改签票退票-原票款"
            );
            refundAlipayTrade(
                    payment.getOutTradeNo(),
                    payment.getAmount(),
                    "TICKET_REFUND_"
                            + orderId
                            + "_DIFF",
                    "改签票退票-补交差价"
            );

        } else {

            refundAlipayTrade(
                    outTradeNo,
                    refundAmount,
                    "TICKET_REFUND_" + orderId,
                    "火车票退票"
            );
        }


        ticketOrderMapper.refundOrder(orderId);
        ticketMapper.refundTicket(orderId);
        Integer startOrder =trainStationMapper.findStopOrderById(
                        ticket.getScheduleId(),
                        ticket.getStartStationId());
        Integer endOrder = trainStationMapper.findStopOrderById(
                        ticket.getScheduleId(),
                        ticket.getEndStationId());
        seatOccupancyMapper.deleteOccupancy(
                ticket.getScheduleId(),ticket.getSeatId(),
                startOrder,endOrder);
        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setAmount(refundAmount);
        refund.setReason("退票");
        refund.setRefundTime(LocalDateTime.now());
        refundMapper.insert(refund);
    }

    private void refundAlipayTrade(
            String outTradeNo,
            BigDecimal refundAmount,
            String outRequestNo,
            String refundReason
    ) {

        if (outTradeNo == null
                || outTradeNo.isBlank()) {
            throw new RuntimeException(
                    "支付宝交易号不能为空"
            );
        }

        if (refundAmount == null
                || refundAmount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {
            throw new RuntimeException(
                    "退款金额必须大于0"
            );
        }

        AlipayTradeRefundRequest request =
                new AlipayTradeRefundRequest();

        String bizContent =
                "{"
                        + "\"out_trade_no\":\""
                        + outTradeNo
                        + "\","
                        + "\"out_request_no\":\""
                        + outRequestNo
                        + "\","
                        + "\"refund_amount\":\""
                        + refundAmount
                        + "\","
                        + "\"refund_reason\":\""
                        + refundReason
                        + "\""
                        + "}";

        request.setBizContent(bizContent);

        try {
            log.info(
                    "开始支付宝退款，"
                            + "outTradeNo={}，"
                            + "outRequestNo={}，"
                            + "refundAmount={}",
                    outTradeNo,
                    outRequestNo,
                    refundAmount
            );

            AlipayTradeRefundResponse response =
                    alipayClient.execute(request);

            log.info(
                    "支付宝退款返回，"
                            + "code={}，"
                            + "msg={}，"
                            + "subCode={}，"
                            + "subMsg={}，"
                            + "fundChange={}，"
                            + "refundFee={}",
                    response.getCode(),
                    response.getMsg(),
                    response.getSubCode(),
                    response.getSubMsg(),
                    response.getFundChange(),
                    response.getRefundFee()
            );

            if (!response.isSuccess()) {
                throw new RuntimeException(
                        "支付宝退款失败："
                                + response.getCode()
                                + " "
                                + response.getMsg()
                                + " "
                                + response.getSubMsg()
                );
            }

        } catch (AlipayApiException e) {
            log.error(
                    "支付宝退款请求异常，outTradeNo={}",
                    outTradeNo,
                    e
            );

            throw new RuntimeException(
                    "支付宝服务暂时繁忙，请稍后重试",
                    e
            );
        }
    }

    @Override
    @Transactional
    public void changeRefund(
            Long orderId,
            Long oldOrderId
    ) {

        Ticket newTicket =
                ticketMapper.findByOrderId(orderId);

        Ticket oldTicket =
                ticketMapper.findByOrderId(oldOrderId);

        if (newTicket == null) {
            throw new RuntimeException("改签订单不存在");
        }

        if (oldTicket == null) {
            throw new RuntimeException("原订单不存在");
        }

        TicketOrder newOrder =
                ticketOrderMapper.findById(orderId);

        TicketOrder oldOrder =
                ticketOrderMapper.findById(oldOrderId);

        if (newOrder == null) {
            throw new RuntimeException("改签订单不存在");
        }

        if (oldOrder == null) {
            throw new RuntimeException("原订单不存在");
        }

        if (!"待支付".equals(newOrder.getStatus())) {
            log.info(
                    "忽略重复改签退款请求，新订单={}，当前状态={}",
                    orderId,
                    newOrder.getStatus()
            );
            return;
        }

        if (!"已支付".equals(oldOrder.getStatus())) {
            throw new RuntimeException(
                    "原订单状态不允许改签退款"
            );
        }

        BigDecimal refundAmount =
                oldTicket.getPrice()
                        .subtract(newTicket.getPrice());

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "改签退款金额错误"
            );
        }

        Payment oldPayment =
                paymentMapper.findByOrderId(oldOrderId);

        if (oldPayment == null) {
            throw new RuntimeException(
                    "原订单支付记录不存在"
            );
        }

        if (!"支付宝".equals(
                oldPayment.getPayMethod()
        )) {
            throw new RuntimeException(
                    "原订单不是支付宝支付"
            );
        }

        String outTradeNo =
                oldPayment.getOutTradeNo();

        if (outTradeNo == null
                || outTradeNo.isBlank()) {
            throw new RuntimeException(
                    "原订单支付记录缺少支付宝交易号"
            );
        }

        refundAlipayTrade(
                outTradeNo,
                refundAmount,
                "CHANGE_REFUND_"
                        + oldOrderId
                        + "_"
                        + orderId,
                "火车票改签退差价"
        );

        Payment newPayment = new Payment();

        newPayment.setOrderId(orderId);
        newPayment.setAmount(newTicket.getPrice());
        newPayment.setPayMethod(
                oldPayment.getPayMethod()
        );
        newPayment.setOutTradeNo(
                oldPayment.getOutTradeNo()
        );
        newPayment.setPayTime(
                oldPayment.getPayTime()
        );

        paymentMapper.insert(newPayment);


        ticketOrderMapper.changeOrder(oldOrderId);
        ticketMapper.changeTicket(oldOrderId);


        Integer startOrder =
                trainStationMapper.findStopOrderById(
                        oldTicket.getScheduleId(),
                        oldTicket.getStartStationId()
                );

        Integer endOrder =
                trainStationMapper.findStopOrderById(
                        oldTicket.getScheduleId(),
                        oldTicket.getEndStationId()
                );

        seatOccupancyMapper.deleteOccupancy(
                oldTicket.getScheduleId(),
                oldTicket.getSeatId(),
                startOrder,
                endOrder
        );


        ticketOrderMapper.payChangeOrder(orderId);
        ticketMapper.payChangeTicket(orderId);


        Refund refund = new Refund();

        refund.setOrderId(orderId);
        refund.setAmount(refundAmount);
        refund.setReason("改签");
        refund.setRefundTime(LocalDateTime.now());

        refundMapper.insert(refund);
    }
}
