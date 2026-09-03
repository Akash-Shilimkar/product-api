package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.PagedResponse;
import com.zestindia.productapi.dto.ProductRequest;
import com.zestindia.productapi.dto.ProductResponse;
import com.zestindia.productapi.entity.Product;
import com.zestindia.productapi.exception.ResourceNotFoundException;
import com.zestindia.productapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private AuditLogService auditLogService;

	@InjectMocks
	private ProductServiceImpl productService;

	private Product product;

	@BeforeEach
	void setUp() {
		product = Product.builder().id(1).productName("Wireless Mouse").createdBy("admin")
				.createdOn(LocalDateTime.now()).build();
	}

	@Test
	void create_savesProductAndReturnsResponse() {
		when(productRepository.save(any(Product.class))).thenReturn(product);

		ProductResponse response = productService.create(new ProductRequest("Wireless Mouse"), "admin");

		assertThat(response.getId()).isEqualTo(1);
		assertThat(response.getProductName()).isEqualTo("Wireless Mouse");
		assertThat(response.getCreatedBy()).isEqualTo("admin");
		verify(productRepository, times(1)).save(any(Product.class));
		verify(auditLogService, times(1)).logProductChange(eq("CREATE"), eq(1), eq("admin"));
	}

	@Test
	void getById_returnsProduct_whenExists() {
		when(productRepository.findById(1)).thenReturn(Optional.of(product));

		ProductResponse response = productService.getById(1);

		assertThat(response.getProductName()).isEqualTo("Wireless Mouse");
	}

	@Test
	void getById_throwsResourceNotFound_whenMissing() {
		when(productRepository.findById(99)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productService.getById(99)).isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("99");
	}

	@Test
	void getAll_returnsPagedResponse() {
		Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);
		when(productRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

		PagedResponse<ProductResponse> result = productService.getAll(0, 20, null);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalElements()).isEqualTo(1);
	}

	@Test
	void update_modifiesExistingProduct() {
		when(productRepository.findById(1)).thenReturn(Optional.of(product));
		when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

		ProductResponse response = productService.update(1, new ProductRequest("Updated Mouse"), "admin");

		assertThat(response.getProductName()).isEqualTo("Updated Mouse");
		assertThat(response.getModifiedBy()).isEqualTo("admin");
	}

	@Test
	void delete_removesProduct_whenExists() {
		when(productRepository.findById(1)).thenReturn(Optional.of(product));

		productService.delete(1);

		verify(productRepository, times(1)).delete(product);
	}

	@Test
	void delete_throwsResourceNotFound_whenMissing() {
		when(productRepository.findById(5)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productService.delete(5)).isInstanceOf(ResourceNotFoundException.class);

		verify(productRepository, never()).delete(any());
	}
}
