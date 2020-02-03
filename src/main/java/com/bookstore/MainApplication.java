package com.bookstore;

import com.bookstore.service.InventoryService;
import com.bookstore.entity.Inventory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MainApplication {

    // Running the application should result in 
    // org.springframework.orm.ObjectOptimisticLockingFailureException
    
    private final InventoryService inventoryService;

    public MainApplication(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public ApplicationRunner init() {
        return args -> {

            System.out.println("Triggering the first transaction ...");
            Inventory firstInventory = inventoryService.firstTransactionFetchesAndReturn();
            System.out.println("First transaction committed successfully ..."); 
            
            System.out.println("Triggering the second transaction ...");
            inventoryService.secondTransactionFetchesAndReturn();
            System.out.println("Second transaction committed successfully ..."); 
            
            // AT THIS POINT, THE firstInventory IS DETACHED
            firstInventory.setQuantity(firstInventory.getQuantity() - 1);
            
            System.out.println("Triggering the third transaction ...");
            inventoryService.thirdTransactionMergesAndUpdates(firstInventory);
            System.out.println("Third transaction committed successfully ..."); 
        };
    }
}

/*
 * 
 * 
 * 
 * 
 * Versioned Optimistic Locking And Detached Entities Sample

Description: This is a sample application that shows how versioned (@Version) optimistic locking and detached entity works. Running the application will result in an optimistic locking specific exception (e.g., the Spring Boot specific, OptimisticLockingFailureException).

Key points:

in a transaction, fetch an entity via findById(1L); commit transaction and close the Persistence Context
in a second transaction, fetch another entity via findById(1L) and update it; commit the transaction and close the Persistence Context
outside transactional context, update the detached entity (fetched in the first transaction)
in a third transaction, call save() and pass to it the detached entity; trying to merge (EntityManager.merge()) the entity will end up in an optimistic locking exception since the version of the detached and just loaded entity don't match
 * 
 * 
 * 
 */
