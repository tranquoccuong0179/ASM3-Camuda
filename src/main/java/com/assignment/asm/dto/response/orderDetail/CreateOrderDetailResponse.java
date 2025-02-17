package com.assignment.asm.dto.response.orderDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderDetailResponse {
    private int quantity;
    private Double price;
    private Long product_id;
}
