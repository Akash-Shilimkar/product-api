package com.zestindia.productapi.controller;

import com.zestindia.productapi.dto.ItemRequest;
import com.zestindia.productapi.dto.ItemResponse;
import com.zestindia.productapi.dto.PagedResponse;
import com.zestindia.productapi.dto.ProductRequest;
import com.zestindia.productapi.dto.ProductResponse;
import com.zestindia.productapi.service.ItemService;
import com.zestindia.productapi.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "CRUD operations on Products, plus nested Items")
public class ProductController {

    private final ProductService productService;
    private final ItemService itemService;

    @PostMapping
    @Operation(summary = "Create a new product  ")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request,
                                                    Authentication authentication) {
        ProductResponse created = productService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id")
    public ResponseEntity<ProductResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List products (paginated, optional name search)")
    public ResponseEntity<PagedResponse<ProductResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productService.getAll(page, size, search));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product  ")
    public ResponseEntity<ProductResponse> update(@PathVariable Integer id,
                                                    @Valid @RequestBody ProductRequest request,
                                                    Authentication authentication) {
        return ResponseEntity.ok(productService.update(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product  ")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "List items belonging to a product (paginated)")
    public ResponseEntity<PagedResponse<ItemResponse>> getItems(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(itemService.getItemsByProduct(id, page, size));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Add an item to a product  ")
    public ResponseEntity<ItemResponse> addItem(@PathVariable Integer id,
                                                  @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.addItem(id, request));
    }
}
