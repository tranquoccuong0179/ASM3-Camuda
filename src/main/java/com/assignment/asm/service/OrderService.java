package com.assignment.asm.service;

import com.assignment.asm.dto.request.order.CreateOrderRequest;
import com.assignment.asm.dto.request.orderDetail.CreateOrderDetailRequest;
import com.assignment.asm.dto.response.order.CreateOrderResponse;
import com.assignment.asm.dto.response.orderDetail.CreateOrderDetailResponse;
import com.assignment.asm.mapper.OrderDetailMapper;
import com.assignment.asm.model.Order;
import com.assignment.asm.model.OrderDetail;
import com.assignment.asm.model.Product;
import com.assignment.asm.repository.OrderDetailRepository;
import com.assignment.asm.repository.OrderRepository;
import com.assignment.asm.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final OrderDetailMapper orderDetailMapper;
    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        double totalPrice = 0;
        int totalProduct = 0;
        List<CreateOrderDetailResponse> createOrderDetailResponses = new ArrayList<>();
        for (CreateOrderDetailRequest createOrderDetailRequest : request.getDetails()){
            Product product = productRepository.findById(createOrderDetailRequest.getProduct_id()).orElse(null);
            OrderDetail orderDetail = orderDetailMapper.toModel(createOrderDetailRequest);
            totalPrice += product.getPrice() * orderDetail.getQuantity();
            totalProduct += orderDetail.getQuantity();
            orderDetailRepository.save(orderDetail);
            createOrderDetailResponses.add(orderDetailMapper.toResponse(orderDetail));
        }

        Order order = new Order();
        order.setTotalPrice(totalPrice);
        order.setTotalProduct(totalProduct);
        order.setStatus("ACTIVE");
//        order.setUser();
        orderRepository.save(order);
        CreateOrderResponse createOrderResponse = new CreateOrderResponse();
        createOrderResponse.setTotalPrice(totalPrice);
        createOrderResponse.setTotalProduct(totalProduct);
        createOrderResponse.setStatus(order.getStatus());
        createOrderResponse.setCreateOrderDetailResponses(createOrderDetailResponses);
        return createOrderResponse;

    }
}
