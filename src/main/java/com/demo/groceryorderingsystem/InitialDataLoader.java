package com.demo.groceryorderingsystem;

import com.demo.groceryorderingsystem.entity.PackagingOption;
import com.demo.groceryorderingsystem.entity.Product;
import com.demo.groceryorderingsystem.repository.PackagingOptionRepository;
import com.demo.groceryorderingsystem.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Loads initial sample data into the database.
 */
@Component
@ConditionalOnProperty(name = "app.data-loader.enabled", havingValue = "true", matchIfMissing = true)
public class InitialDataLoader implements CommandLineRunner {
    
    private final ProductRepository productRepository;
    private final PackagingOptionRepository packagingOptionRepository;

    public InitialDataLoader(
            ProductRepository productRepository,
            PackagingOptionRepository packagingOptionRepository) {
        this.productRepository = productRepository;
        this.packagingOptionRepository = packagingOptionRepository;
    }
    
    @Override
    public void run(String... args) {
        loadSampleData();
    }
    
    private void loadSampleData() {
        // Create products
        Product cheese = new Product("CE", "Cheese", new BigDecimal("5.95"));
        Product ham = new Product("HM", "Ham", new BigDecimal("7.95"));
        Product soySauce = new Product("SS", "Soy Sauce", new BigDecimal("11.95"));
        
        productRepository.save(cheese);
        productRepository.save(ham);
        productRepository.save(soySauce);

        // Create packaging options for Cheese (CE)
        PackagingOption ce3 = new PackagingOption(cheese, 3, new BigDecimal("14.95"));
        PackagingOption ce5 = new PackagingOption(cheese, 5, new BigDecimal("20.95"));

        packagingOptionRepository.save(ce3);
        packagingOptionRepository.save(ce5);

        // Create packaging options for Ham (HM)
        PackagingOption hm2 = new PackagingOption(ham, 2, new BigDecimal("13.95"));
        PackagingOption hm5 = new PackagingOption(ham, 5, new BigDecimal("29.95"));
        PackagingOption hm8 = new PackagingOption(ham, 8, new BigDecimal("40.95"));

        packagingOptionRepository.save(hm2);
        packagingOptionRepository.save(hm5);
        packagingOptionRepository.save(hm8);
        
        System.out.println("Sample data loaded successfully!");
        System.out.println("Products: CE (Cheese), HM (Ham), SS (Soy Sauce)");
    }
}