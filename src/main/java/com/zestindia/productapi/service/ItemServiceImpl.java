package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.ItemRequest;
import com.zestindia.productapi.dto.ItemResponse;
import com.zestindia.productapi.dto.PagedResponse;
import com.zestindia.productapi.entity.Item;
import com.zestindia.productapi.entity.Product;
import com.zestindia.productapi.exception.ResourceNotFoundException;
import com.zestindia.productapi.repository.ItemRepository;
import com.zestindia.productapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

	private final ItemRepository itemRepository;
	private final ProductRepository productRepository;

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<ItemResponse> getItemsByProduct(Integer productId, int page, int size) {
		if (!productRepository.existsById(productId)) {
			throw new ResourceNotFoundException("Product not found with id: " + productId);
		}

		Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
		Page<Item> items = itemRepository.findByProductId(productId, pageable);
		return PagedResponse.of(items.map(this::toResponse));
	}

	@Override
	public ItemResponse addItem(Integer productId, ItemRequest request) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

		Item item = Item.builder().product(product).quantity(request.getQuantity()).build();

		Item saved = itemRepository.save(item);
		return toResponse(saved);
	}

	private ItemResponse toResponse(Item item) {
		return ItemResponse.builder().id(item.getId()).productId(item.getProduct().getId()).quantity(item.getQuantity())
				.build();
	}
}
