package com.zestindia.productapi.controller;

import com.zestindia.productapi.dto.ProductRequest;
import com.zestindia.productapi.dto.ProductResponse;
import com.zestindia.productapi.dto.PagedResponse;
import com.zestindia.productapi.service.ItemService;
import com.zestindia.productapi.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

	@Mock
	private ProductService productService;

	@Mock
	private ItemService itemService;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private ProductController productController;

	private ProductResponse productResponse;

	@BeforeEach
	void setUp() {

		productResponse = new ProductResponse();

		productResponse.setId(1);
		productResponse.setProductName("Mechanical Keyboard");
		productResponse.setCreatedBy("admin");
		productResponse.setModifiedBy("admin");
	}

	// ---------------------------------------------------------
	// CREATE PRODUCT
	// ---------------------------------------------------------

	@Test
	void createProduct_shouldReturnCreated() {

		ProductRequest request = new ProductRequest("Mechanical Keyboard");

		when(authentication.getName()).thenReturn("admin");

		when(productService.create(request, "admin")).thenReturn(productResponse);

		ResponseEntity<ProductResponse> response = productController.create(request, authentication);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		assertNotNull(response.getBody());

		assertEquals(1, response.getBody().getId());

		assertEquals("Mechanical Keyboard", response.getBody().getProductName());

		verify(authentication).getName();

		verify(productService).create(request, "admin");
	}

	// ---------------------------------------------------------
	// GET PRODUCT BY ID
	// ---------------------------------------------------------

	@Test
	void getProductById_shouldReturnProduct() {

		when(productService.getById(1)).thenReturn(productResponse);

		ResponseEntity<ProductResponse> response = productController.getById(1);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		assertNotNull(response.getBody());

		assertEquals(1, response.getBody().getId());

		assertEquals("Mechanical Keyboard", response.getBody().getProductName());

		verify(productService).getById(1);
	}

	// ---------------------------------------------------------
	// GET ALL PRODUCTS
	// ---------------------------------------------------------

	@Test
	void getAllProducts_shouldReturnPagedResponse() {

		PagedResponse<ProductResponse> pagedResponse = new PagedResponse<>();

		when(productService.getAll(0, 20, null)).thenReturn(pagedResponse);

		ResponseEntity<PagedResponse<ProductResponse>> response = productController.getAll(0, 20, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		assertNotNull(response.getBody());

		verify(productService).getAll(0, 20, null);
	}

	// ---------------------------------------------------------
	// GET ALL PRODUCTS WITH SEARCH
	// ---------------------------------------------------------

	@Test
	void getAllProducts_withSearch_shouldReturnPagedResponse() {

		PagedResponse<ProductResponse> pagedResponse = new PagedResponse<>();

		when(productService.getAll(0, 10, "Keyboard")).thenReturn(pagedResponse);

		ResponseEntity<PagedResponse<ProductResponse>> response = productController.getAll(0, 10, "Keyboard");

		assertEquals(HttpStatus.OK, response.getStatusCode());

		assertNotNull(response.getBody());

		verify(productService).getAll(0, 10, "Keyboard");
	}

	// ---------------------------------------------------------
	// UPDATE PRODUCT
	// ---------------------------------------------------------

	@Test
	void updateProduct_shouldReturnUpdatedProduct() {

		ProductRequest request = new ProductRequest("Mechanical Keyboard RGB");

		ProductResponse updatedResponse = new ProductResponse();

		updatedResponse.setId(1);
		updatedResponse.setProductName("Mechanical Keyboard RGB");
		updatedResponse.setModifiedBy("admin");

		when(authentication.getName()).thenReturn("admin");

		when(productService.update(1, request, "admin")).thenReturn(updatedResponse);

		ResponseEntity<ProductResponse> response = productController.update(1, request, authentication);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		assertNotNull(response.getBody());

		assertEquals(1, response.getBody().getId());

		assertEquals("Mechanical Keyboard RGB", response.getBody().getProductName());

		verify(productService).update(1, request, "admin");
	}

	// ---------------------------------------------------------
	// DELETE PRODUCT
	// ---------------------------------------------------------

	@Test
	void deleteProduct_shouldReturnNoContent() {

		doNothing().when(productService).delete(1);

		ResponseEntity<Void> response = productController.delete(1);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

		assertNull(response.getBody());

		verify(productService).delete(1);
	}
}