package com.demo.groceryorderingsystem;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "Grocery Ordering System API",
                version = "1.0",
                description = "API documentation for grocery ordering system"
        )
)
@SpringBootApplication
public class GroceryOrderingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroceryOrderingSystemApplication.class, args);
    }

}
