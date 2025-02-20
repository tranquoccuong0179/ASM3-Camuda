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
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;

@Component
@Slf4j
@RequiredArgsConstructor
public class DelegateEmailOrderInformation implements JavaDelegate {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final JavaMailService javaMailService;
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long orderId = (Long) delegateExecution.getVariable("orderId");
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new NotFoundException("Order not found ");
        }

        User user = userRepository.findById(order.getUser().getId()).orElse(null);

        javaMailService.sendEmail(user.getEmail(), "Đơn hàng của bạn xác nhận thành công", user.getFirstName() + " " + user.getLastName());


    }
}
