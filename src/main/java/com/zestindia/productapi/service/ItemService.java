package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.ItemRequest;
import com.zestindia.productapi.dto.ItemResponse;
import com.zestindia.productapi.dto.PagedResponse;

public interface ItemService {
	PagedResponse<ItemResponse> getItemsByProduct(Integer productId, int page, int size);

	ItemResponse addItem(Integer productId, ItemRequest request);
}
