package com.assignment.asm.delegate;

import com.assignment.asm.model.Order;
import com.assignment.asm.model.User;
import com.assignment.asm.repository.OrderRepository;
import com.assignment.asm.repository.UserRepository;
import com.assignment.asm.service.JavaMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;

@Component
@Slf4j
@RequiredArgsConstructor
public class DelegateEmailOrderInformation implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(DelegateEmailOrderInformation.class);
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final JavaMailService javaMailService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        Long orderId = (Long) delegateExecution.getVariable("orderId");
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            logger.error("Order not found with id: {}", orderId);
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        User user = userRepository.findById(order.getUser().getId()).orElse(null);
        if (user == null) {
            logger.error("User not found with id: {}", order.getUser().getId());
            throw new RuntimeException("User not found with id: " + order.getUser().getId());
        }

        try {
            javaMailService.sendEmail(user.getEmail(), "abggg", "abc");
            logger.info("Email sent to: {} for order id: {}", user.getEmail(), orderId);
        } catch (Exception e) {
            logger.error("Failed to send email to: {} for order id: {}", user.getEmail(), orderId, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
