package com.assignment.asm.service;

import com.assignment.asm.dto.request.order.CreateOrderRequest;
import com.assignment.asm.dto.request.orderDetail.CreateOrderDetailRequest;
import com.assignment.asm.dto.response.order.CreateOrderResponse;
import com.assignment.asm.dto.response.orderDetail.CreateOrderDetailResponse;
import com.assignment.asm.mapper.OrderDetailMapper;
import com.assignment.asm.model.Order;
import com.assignment.asm.model.OrderDetail;
import com.assignment.asm.model.Product;
import com.assignment.asm.model.User;
import com.assignment.asm.repository.OrderDetailRepository;
import com.assignment.asm.repository.OrderRepository;
import com.assignment.asm.repository.ProductRepository;
import com.assignment.asm.repository.UserRepository;
import com.assignment.asm.utils.AuthenUtil;
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
    private final UserRepository userRepository;
    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
//        long userId = AuthenUtil.getUserId();
//        User user = userRepository.findById(userId).orElse(null);
        double totalPrice = 0;
        int totalProduct = 0;
        List<CreateOrderDetailResponse> createOrderDetailResponses = new ArrayList<>();
        Order order = new Order();
        order.setStatus("ACTIVE");
        orderRepository.save(order);
        for (CreateOrderDetailRequest createOrderDetailRequest : request.getDetails()){
            Product product = productRepository.findById(createOrderDetailRequest.getProduct_id()).orElse(null);
            OrderDetail orderDetail = orderDetailMapper.toModel(createOrderDetailRequest);
            orderDetail.setPrice(product.getPrice() * createOrderDetailRequest.getQuantity());
            orderDetail.setProduct(product);
            orderDetail.setOrder(order);
            orderDetailRepository.save(orderDetail);
            totalPrice += product.getPrice() * orderDetail.getQuantity();
            totalProduct += orderDetail.getQuantity();
            createOrderDetailResponses.add(orderDetailMapper.toResponse(orderDetail));
        }


        order.setTotalPrice(totalPrice);
        order.setTotalProduct(totalProduct);
//        order.setUser(user);
        orderRepository.save(order);
        CreateOrderResponse createOrderResponse = new CreateOrderResponse();
        createOrderResponse.setTotalPrice(totalPrice);
        createOrderResponse.setTotalProduct(totalProduct);
        createOrderResponse.setStatus(order.getStatus());
        createOrderResponse.setCreateOrderDetailResponses(createOrderDetailResponses);
        return createOrderResponse;

    }
}
