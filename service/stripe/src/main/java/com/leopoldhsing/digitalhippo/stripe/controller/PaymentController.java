package com.leopoldhsing.digitalhippo.stripe.controller;

import com.leopoldhsing.digitalhippo.common.constants.StripeConstants;
import com.leopoldhsing.digitalhippo.model.vo.payment.CreateCheckoutSessionVo;
import com.leopoldhsing.digitalhippo.stripe.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/checkout-session")
    public ResponseEntity<String> createCheckoutSession(@RequestBody CreateCheckoutSessionVo createCheckoutSessionVo) {
        String sessionUrl = paymentService
                .createCheckoutSession(createCheckoutSessionVo.getPayloadOrderId(), createCheckoutSessionVo.getProductIdList());

        return ResponseEntity.ok(sessionUrl);
    }

    @PostMapping("/confirm")
    public void confirmPayment(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        String responseCode = paymentService.confirmPayment(payload, sigHeader);

        if (StripeConstants.PAYMENT_SUCCESS.equalsIgnoreCase(responseCode)) {
            // log payment result as successful
        } else {
            // log payment result failed
        }
    }
}
