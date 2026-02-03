package com.demo.groceryorderingsystem.service;

import com.demo.groceryorderingsystem.dto.ProductDTO;
import com.demo.groceryorderingsystem.entity.Product;
import com.demo.groceryorderingsystem.repository.ProductRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts(Integer startPosition) {
        PageRequest pageable = PageRequest.of(startPosition, 10, Sort.by(Sort.Direction.ASC, "createdDate"));
        Page<Product> products = productRepository.findAll(pageable);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductByCode(String code) {
        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + code));
        return convertToDTO(product);
    }

    @Transactional
    public void addNewProduct(ProductDTO productDTO) {
        if (productRepository.existsByCode(productDTO.code())) {
            throw new DuplicateKeyException("Product already exists: " + productDTO.code());
        }

        Product product = new Product(productDTO.code(), productDTO.name(), productDTO.price());
        productRepository.save(product);
    }

    @Transactional
    public void updateProduct(ProductDTO productDTO) {
        Product product = productRepository.findByCode(productDTO.code())
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productDTO.code()));

        product.setName(productDTO.name());
        product.setPrice(productDTO.price());

        productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(String code) {
        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + code));
        productRepository.delete(product);
    }

    private ProductDTO convertToDTO(Product product) {
        return new ProductDTO(product.getCode(), product.getName(), product.getPrice());
    }
}
