package com.ecommerce.catalog.client;

import com.ecommerce.catalog.dto.InventoryResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "inventory-service", url = "${inventory.service.url:http://localhost:8081}")
public interface InventoryServiceClient {

    @GetMapping("/api/inventory/{inventoryId}")
    InventoryResponseDTO getInventoryById(@PathVariable Long inventoryId);

    @GetMapping("/api/inventory/sku/{sku}")
    InventoryResponseDTO getInventoryBySku(@PathVariable String sku);

    @GetMapping("/api/inventory/batch")
    List<InventoryResponseDTO> getInventoryBatch(@RequestParam List<Long> inventoryIds);
}

