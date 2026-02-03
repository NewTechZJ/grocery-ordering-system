package com.demo.groceryorderingsystem.controller;

import com.demo.groceryorderingsystem.dto.PackagingOptionDTO;
import com.demo.groceryorderingsystem.service.PackagingOptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/packaging")
@Tag(name = "Packaging Options", description = "APIs for grocery products packaging options")
public class PackagingOptionController {

    private final PackagingOptionService packagingOptionService;

    public PackagingOptionController(PackagingOptionService packagingOptionService) {
        this.packagingOptionService = packagingOptionService;
    }

    @Operation(summary = "Get all packaging options", description = "Retrieve a list of all packaging options with pagination.")
    @GetMapping
    public ResponseEntity<List<PackagingOptionDTO>> getAllPackagingOptions(
            @RequestParam(name = "start", defaultValue = "0", required = false) Integer startPosition
    ) {
        return ResponseEntity.ok(packagingOptionService.getAllPackagingOptions(startPosition));
    }

    @Operation(summary = "Get packaging options by product", description = "Retrieve packaging options for a specific product by its code with pagination.")
    @GetMapping("/{productCode}")
    public ResponseEntity<List<PackagingOptionDTO>> getPackagingOptionsByProduct(
            @PathVariable String productCode,
            @RequestParam(name = "start", defaultValue = "0", required = false) Integer startPosition
    ) {
        return ResponseEntity.ok(packagingOptionService.getPackagingOptionsByProduct(productCode, startPosition));
    }

    @Operation(summary = "Create packaging option", description = "Create a new packaging option for a product.")
    @PostMapping
    public ResponseEntity createPackagingOption(
            @Valid @RequestBody PackagingOptionDTO dto) {
        packagingOptionService.createPackagingOption(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Delete packaging option", description = "Delete an existing packaging option by its unique id.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackagingOption(@PathVariable UUID id) {
        packagingOptionService.deletePackagingOption(id);
        return ResponseEntity.noContent().build();
    }
}
