package com.demo.groceryorderingsystem.repository;

import com.demo.groceryorderingsystem.entity.PackagingOption;
import com.demo.groceryorderingsystem.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PackagingOptionRepository extends JpaRepository<PackagingOption, UUID> {

    boolean existsPackagingOptionByProductAndQuantity(Product product, Integer quantity);

    Page<PackagingOption> findByProductCode(String productCode, Pageable pageable);

    @Query("SELECT p FROM PackagingOption p WHERE p.product.code= :productCode AND p.quantity <= :quantity ORDER BY p.quantity DESC")
    List<PackagingOption> fetchQualifiedPackagingOptions(String productCode, Integer quantity);
}
