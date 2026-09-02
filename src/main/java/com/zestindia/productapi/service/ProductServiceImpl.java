package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.PagedResponse;
import com.zestindia.productapi.dto.ProductRequest;
import com.zestindia.productapi.dto.ProductResponse;
import com.zestindia.productapi.entity.Product;
import com.zestindia.productapi.exception.ResourceNotFoundException;
import com.zestindia.productapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    @Override
    public ProductResponse create(ProductRequest request, String actor) {
        Product product = Product.builder()
                .productName(request.getProductName())
                .createdBy(actor)
                .build();

        Product saved = productRepository.save(product);
        auditLogService.logProductChange("CREATE", saved.getId(), actor);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Integer id) {
        Product product = findOrThrow(id);
        return toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAll(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Product> result = StringUtils.hasText(search)
                ? productRepository.findByProductNameContainingIgnoreCase(search, pageable)
                : productRepository.findAll(pageable);

        return PagedResponse.of(result.map(this::toResponse));
    }

    @Override
    public ProductResponse update(Integer id, ProductRequest request, String actor) {
        Product product = findOrThrow(id);
        product.setProductName(request.getProductName());
        product.setModifiedBy(actor);

        Product saved = productRepository.save(product);
        auditLogService.logProductChange("UPDATE", saved.getId(), actor);
        return toResponse(saved);
    }

    @Override
    public void delete(Integer id) {
        Product product = findOrThrow(id);
        productRepository.delete(product);
        auditLogService.logProductChange("DELETE", id, "system");
    }

    private Product findOrThrow(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .createdBy(product.getCreatedBy())
                .createdOn(product.getCreatedOn())
                .modifiedBy(product.getModifiedBy())
                .modifiedOn(product.getModifiedOn())
                .build();
    }
}
