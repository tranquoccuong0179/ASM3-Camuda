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
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.camunda.bpm.admin.impl.plugin.resources.MetricsRestService.objectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final OrderDetailMapper orderDetailMapper;
    private final UserRepository userRepository;
    private final RuntimeService runtimeService;
    private final TaskService taskService;


    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process_order", UUID.randomUUID().toString());

        double totalPrice = 0;
        int totalProduct = 0;
        boolean orderIsValid = true;
        List<CreateOrderDetailResponse> createOrderDetailResponses = new ArrayList<>();

        Order order = new Order();
        order.setStatus("ACTIVE");
        orderRepository.save(order);

        for (CreateOrderDetailRequest detailRequest : request.getDetails()) {
            Product product = productRepository.findById(detailRequest.getProduct_id()).orElse(null);
            if (product == null) {
                continue;
            }

            if (detailRequest.getQuantity() > product.getQuantity()) {
                orderIsValid = false;
            }

            OrderDetail orderDetail = orderDetailMapper.toModel(detailRequest);
            double itemPrice = product.getPrice() * detailRequest.getQuantity();
            orderDetail.setPrice(itemPrice);
            orderDetail.setProduct(product);
            orderDetail.setOrder(order);
            orderDetailRepository.save(orderDetail);

            totalPrice += itemPrice;
            totalProduct += detailRequest.getQuantity();

            createOrderDetailResponses.add(orderDetailMapper.toResponse(orderDetail));
        }

        order.setTotalPrice(totalPrice);
        order.setTotalProduct(totalProduct);
        order.setBusinessKey(processInstance.getBusinessKey());
        orderRepository.save(order);

        CreateOrderResponse createOrderResponse = new CreateOrderResponse();
        createOrderResponse.setTotalPrice(totalPrice);
        createOrderResponse.setTotalProduct(totalProduct);
        createOrderResponse.setStatus(order.getStatus());
        createOrderResponse.setCreateOrderDetailResponses(createOrderDetailResponses);

        String businessKey = processInstance.getBusinessKey();
        Task task = taskService.createTaskQuery()
                .processInstanceBusinessKey(businessKey)
                .taskDefinitionKey("Activity_Create_Order")
                .singleResult();

        if (task != null) {
            Map<String, Object> variables = new HashMap<>();
            try {
                String jsonResponse = objectMapper.writeValueAsString(createOrderResponse);
                variables.put("orderResponse", jsonResponse);
                variables.put("orderIsValid", orderIsValid);

                taskService.complete(task.getId(), variables);
            } catch (Exception e) {
                log.error("Error processing order task: ", e);
            }
        } else {
            log.warn("Không tìm thấy task với businessKey: {}", businessKey);
        }

        return createOrderResponse;
    }
}
