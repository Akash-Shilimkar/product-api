package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.ItemRequest;
import com.zestindia.productapi.dto.ItemResponse;
import com.zestindia.productapi.entity.Item;
import com.zestindia.productapi.entity.Product;
import com.zestindia.productapi.exception.ResourceNotFoundException;
import com.zestindia.productapi.repository.ItemRepository;
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
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Product product;
    private Item item;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(1).productName("Keyboard").build();
        item = Item.builder().id(10).product(product).quantity(5).build();
    }

    @Test
    void addItem_savesItem_whenProductExists() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponse response = itemService.addItem(1, new ItemRequest(5));

        assertThat(response.getProductId()).isEqualTo(1);
        assertThat(response.getQuantity()).isEqualTo(5);
    }

    @Test
    void addItem_throwsResourceNotFound_whenProductMissing() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addItem(99, new ItemRequest(3)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getItemsByProduct_returnsPagedItems() {
        when(productRepository.existsById(1)).thenReturn(true);
        Page<Item> page = new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);
        when(itemRepository.findByProductId(eq(1), any(Pageable.class))).thenReturn(page);

        var result = itemService.getItemsByProduct(1, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void getItemsByProduct_throwsResourceNotFound_whenProductMissing() {
        when(productRepository.existsById(42)).thenReturn(false);

        assertThatThrownBy(() -> itemService.getItemsByProduct(42, 0, 20))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
