package com.assignment.asm.service;

import com.assignment.asm.dto.request.order.CreateOrderRequest;
import com.assignment.asm.dto.response.order.CreateOrderResponse;

public interface IOrderService {
    public CreateOrderResponse createOrder(CreateOrderRequest request);
}
