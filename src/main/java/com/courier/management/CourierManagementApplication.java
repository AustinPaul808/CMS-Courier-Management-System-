package com.courier.management;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Spring Boot entry point.
 *
 * Startup sequence:
 *  1. Spring Boot initialises and Hikari connection pool connects to Supabase.
 *  2. CourierRepository (via JdbcTemplate) loads all existing rows from the
 *     'couriers' table into the in-memory CourierManager (Vector).
 *  3. The original console menu starts — the user interacts with it as before.
 *
 * No REST controllers, no web layer, no authentication — console only.
 */
@SpringBootApplication
public class CourierManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourierManagementApplication.class, args);
    }

    /**
     * CommandLineRunner runs after the application context is fully loaded.
     * CourierRepository is auto-wired by Spring because it is a @Repository bean.
     */
    @Bean
    CommandLineRunner startConsoleApp(CourierRepository repository) {
        return args -> {
            System.out.println("\n================================");
            System.out.println("COURIER MANAGEMENT SYSTEM");
            System.out.println("Connecting to Supabase...");
            System.out.println("================================\n");

            // ---- Build in-memory manager and wire repository ----
            CourierManager manager = new CourierManager();
            manager.setRepository(repository);

            // ---- Load existing records from Supabase at startup ----
            System.out.println("Loading existing couriers from database...");
            try {
                List<Courier> existing = repository.findAll();
                for (Courier c : existing) {
                    manager.addDirectly(c);
                }
                System.out.println("Loaded " + existing.size() + " courier(s) from Supabase.");
            } catch (Exception e) {
                System.out.println("Warning: Could not load couriers from database: " + e.getMessage());
                System.out.println("Starting with empty courier list.");
            }

            System.out.println("SUPABASE CONNECTION SUCCESSFUL!\n");

            // ---- Start the original console application ----
            CourierConsole console = new CourierConsole(manager);
            console.start();

            // ---- Graceful shutdown ----
            System.out.println("\nApplication terminated.");
            System.exit(0);
        };
    }
}
