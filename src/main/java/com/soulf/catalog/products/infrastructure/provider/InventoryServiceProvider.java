package com.soulf.catalog.products.infrastructure.provider;

import com.soulf.catalog.products.infrastructure.provider.dto.InventoryResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceProvider {

    private final InventoryServiceClient client;

    public Integer getAvailableQuantity(Long inventoryId) {
        try {
            InventoryResponseDTO inventory = client.getInventoryById(inventoryId);
            return inventory != null ? inventory.getAvailableQuantity() : null;
        } catch (Exception e) {
            log.warn("Failed to fetch inventory for inventoryId: {}. Error: {}", inventoryId, e.getMessage());
            return null;
        }
    }
}

