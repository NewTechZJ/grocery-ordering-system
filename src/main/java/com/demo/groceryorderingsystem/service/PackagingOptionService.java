package com.demo.groceryorderingsystem.service;

import com.demo.groceryorderingsystem.dto.PackagingOptionDTO;
import com.demo.groceryorderingsystem.entity.PackagingOption;
import com.demo.groceryorderingsystem.entity.Product;
import com.demo.groceryorderingsystem.repository.PackagingOptionRepository;
import com.demo.groceryorderingsystem.repository.ProductRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PackagingOptionService {

    private final PackagingOptionRepository packagingOptionRepository;
    private final ProductRepository productRepository;

    public PackagingOptionService(
            PackagingOptionRepository packagingOptionRepository,
            ProductRepository productRepository) {
        this.packagingOptionRepository = packagingOptionRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<PackagingOptionDTO> getAllPackagingOptions(Integer startPosition) {
        PageRequest pageable = PageRequest.of(startPosition, 10, Sort.by(Sort.Direction.ASC, "createdDate"));
        Page<PackagingOption> packagingOptions = packagingOptionRepository.findAll(pageable);

        return packagingOptions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PackagingOption> fetchQualifiedPackagingOptions(String productCode, Integer quantity) {
        return packagingOptionRepository
                .fetchQualifiedPackagingOptions(productCode, quantity);
    }

    @Transactional(readOnly = true)
    public List<PackagingOptionDTO> getPackagingOptionsByProduct(String productCode, Integer startPosition) {
        if (!productRepository.existsByCode(productCode)) {
            throw new NoSuchElementException("Product not found: " + productCode);
        }
        PageRequest pageable = PageRequest.of(startPosition, 10, Sort.by(Sort.Direction.ASC, "createdDate"));
        Page<PackagingOption> packagingOptions = packagingOptionRepository.findByProductCode(productCode, pageable);

        return packagingOptions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createPackagingOption(PackagingOptionDTO packagingOptionDTO) {
        Product product = productRepository.findByCode(packagingOptionDTO.productCode())
                .orElseThrow(() -> new NoSuchElementException(
                        "Product not found: " + packagingOptionDTO.productCode()));
        if (packagingOptionRepository.existsPackagingOptionByProductAndQuantity(
                product, packagingOptionDTO.quantity())) {
            throw new DuplicateKeyException("Packaging Option already exists: Product - " +
                    packagingOptionDTO.productCode() + ", Quantity - " + packagingOptionDTO.quantity());
        }
        PackagingOption option = new PackagingOption(
                product,
                packagingOptionDTO.quantity(),
                packagingOptionDTO.price()
        );
        packagingOptionRepository.save(option);
    }

    @Transactional
    public void deletePackagingOption(UUID id) {
        if (!packagingOptionRepository.existsById(id)) {
            throw new NoSuchElementException("Packaging option not found: " + id);
        }
        packagingOptionRepository.deleteById(id);
    }

    private PackagingOptionDTO convertToDTO(PackagingOption option) {
        return new PackagingOptionDTO(
                option.getId(),
                option.getProduct().getCode(),
                option.getQuantity(),
                option.getPrice()
        );
    }
}
