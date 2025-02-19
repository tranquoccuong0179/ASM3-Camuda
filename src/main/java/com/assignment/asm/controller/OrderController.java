package com.assignment.asm.controller;

import com.assignment.asm.dto.ApiResponse;
import com.assignment.asm.dto.request.order.CreateOrderRequest;
import com.assignment.asm.dto.response.order.CreateOrderResponse;
import com.assignment.asm.service.IOrderService;
import com.assignment.asm.service.IProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
@Slf4j
public class OrderController {
    private final IOrderService orderService;
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(@RequestBody CreateOrderRequest request) {

        CreateOrderResponse response = orderService.createOrder(request);

        String businessKey = request.getBusinessKey();
        Task task = taskService.createTaskQuery()
                .processInstanceBusinessKey(businessKey)
                .taskDefinitionKey("Activity_Create_Order")
                .singleResult();

        if (task != null) {
            Map<String, Object> variables = new HashMap<>();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = objectMapper.writeValueAsString(response);
                variables.put("orderResponse", jsonResponse);

                // Tính toán giá trị của orderIsValid dựa trên request hoặc response
//                boolean orderIsValid = isValidOrder(request, response); // Thay thế bằng logic thực tế
                boolean orderIsValid = true;
                variables.put("orderIsValid", orderIsValid);

                taskService.complete(task.getId(), variables);
            } catch (Exception e) {
                log.error("Error processing order: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse<>(500, "Error processing order", null));
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, "Create Success", response));
    }

    // Hàm helper để xác định xem order có hợp lệ không
//    private boolean isValidOrder(CreateOrderRequest request, CreateOrderResponse response) {
//        // Thay thế bằng logic nghiệp vụ thực tế của bạn
//        // Ví dụ: kiểm tra xem request và response có dữ liệu hợp lệ không
//        return request != null && response != null && !response.getId().isEmpty();
//    }


}
