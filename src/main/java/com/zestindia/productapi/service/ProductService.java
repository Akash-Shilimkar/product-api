package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.PagedResponse;
import com.zestindia.productapi.dto.ProductRequest;
import com.zestindia.productapi.dto.ProductResponse;

public interface ProductService {
    ProductResponse create(ProductRequest request, String actor);
    ProductResponse getById(Integer id);
    PagedResponse<ProductResponse> getAll(int page, int size, String search);
    ProductResponse update(Integer id, ProductRequest request, String actor);
    void delete(Integer id);
}
