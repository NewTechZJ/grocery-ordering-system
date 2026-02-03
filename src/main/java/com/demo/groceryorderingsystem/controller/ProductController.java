package com.demo.groceryorderingsystem.controller;

import com.demo.groceryorderingsystem.dto.ProductDTO;
import com.demo.groceryorderingsystem.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "APIs for managing grocery products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Get all products", description = "Retrieve a list of all grocery products with pagination.")
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(name = "start", defaultValue = "0") Integer startPosition) {
        return ResponseEntity.ok(productService.getAllProducts(startPosition));
    }

    @Operation(summary = "Get product by code", description = "Retrieve a specific grocery product by its unique code.")
    @GetMapping("/{code}")
    public ResponseEntity<ProductDTO> getProductByCode(@PathVariable String code) {
        return ResponseEntity.ok(productService.getProductByCode(code));
    }

    @Operation(summary = "Add new product", description = "Add a new grocery product to the system.")
    @PostMapping
    public ResponseEntity addNewProduct(@Valid @RequestBody ProductDTO productDTO) {
        productService.addNewProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Update existing product", description = "Update the details of an existing grocery product.")
    @PutMapping
    public ResponseEntity updateProduct(@Valid @RequestBody ProductDTO productDTO) {
        productService.updateProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Delete product by code", description = "Delete a specific grocery product by its unique code.")
    @DeleteMapping("/{code}")
    public ResponseEntity deleteProductByCode(@PathVariable String code) {
        productService.deleteProduct(code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
