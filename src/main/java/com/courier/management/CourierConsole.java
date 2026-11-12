package com.courier.management;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Vector;

// ---------- Custom exception ----------
class CourierNotFoundException extends Exception {
    CourierNotFoundException(int id) {
        super("No courier found with ID " + id);
    }
}

// ---------- Base class ----------
abstract class Courier {
    private int id;
    private String sender;
    private String receiver;
    private String address;
    private double weight;

    Courier(int id, String sender, String receiver, String address, double weight) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number.");
        }
        this.id = id;
        setSender(sender);
        setReceiver(receiver);
        setAddress(address);
        setWeight(weight);
    }

    private static String clean(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty.");
        }
        if (value.contains("|")) {
            throw new IllegalArgumentException(field + " cannot contain the '|' character.");
        }
        return value.trim();
    }

    public int getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = clean(sender, "Sender");
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = clean(receiver, "Receiver");
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = clean(address, "Address");
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0.");
        }
        this.weight = weight;
    }

    abstract double calculatePrice();

    abstract double getPrice();

    abstract String getType();

    // One line per courier for the file: TYPE|id|sender|receiver|address|weight
    String toFileString() {
        return getType().charAt(0) + "|" + id + "|" + sender + "|" + receiver + "|" + address + "|" + weight;
    }

    void display() {
        System.out.println("ID: " + id);
        System.out.println("Sender: " + sender);
        System.out.println("Receiver: " + receiver);
        System.out.println("Address: " + address);
        System.out.println("Weight: " + weight + " kg");
    }
}

// ---------- Domestic ----------
class DomesticCourier extends Courier {
    private double price;

    DomesticCourier(int id, String sender, String receiver,
                    String address, double weight) {
        super(id, sender, receiver, address, weight);
        calculatePrice();
    }

    @Override
    double calculatePrice() {
        // Rs. 50 base + Rs. 20 per kg
        price = 50 + 20 * getWeight();
        return price;
    }

    @Override
    double getPrice() {
        return price;
    }

    @Override
    String getType() {
        return "Domestic";
    }

    @Override
    void display() {
        super.display();
        System.out.println("Type: Domestic");
        System.out.println("Price: Rs. " + String.format("%.2f", price));
    }
}

// ---------- International ----------
class InternationalCourier extends Courier {
    private double price;

    InternationalCourier(int id, String sender, String receiver,
                         String address, double weight) {
        super(id, sender, receiver, address, weight);
        calculatePrice();
    }

    @Override
    double calculatePrice() {
        // Rs. 500 base + Rs. 150 per kg
        price = 500 + 150 * getWeight();
        return price;
    }

    @Override
    double getPrice() {
        return price;
    }

    @Override
    String getType() {
        return "International";
    }

    @Override
    void display() {
        super.display();
        System.out.println("Type: International");
        System.out.println("Price: Rs. " + String.format("%.2f", price));
    }
}

// ---------- Async File Saver (Multithreading) ----------
class AsyncFileSaver extends Thread {
    private final Vector<Courier> couriers;
    private final String filename;

    AsyncFileSaver(Vector<Courier> couriers, String filename) {
        this.couriers = couriers;
        this.filename = filename;
    }

    @Override
    public void run() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (Courier c : couriers) {
                pw.println(c.toFileString());
            }
            System.out.println("[File] Couriers saved to " + filename + " (async).");
        } catch (IOException e) {
            System.out.println("[File] Error saving couriers: " + e.getMessage());
        }
    }
}

// ---------- Batch Processor (Multithreading) ----------
class CourierBatchProcessor extends Thread {
    private final Vector<Courier> couriers;

    CourierBatchProcessor(Vector<Courier> couriers) {
        this.couriers = couriers;
    }

    @Override
    public void run() {
        System.out.println("[Batch] Processing " + couriers.size() + " courier(s) in background...");
        for (Courier c : couriers) {
            System.out.println("[Batch] Processed -> ID: " + c.getId()
                    + " | " + c.getType() + " | " + c.getSender() + " -> " + c.getReceiver()
                    + " | Rs. " + String.format("%.2f", c.getPrice()));
        }
        System.out.println("[Batch] Batch processing complete.");
    }
}

// ---------- Courier Manager ----------
class CourierManager {
    private final Vector<Courier> couriers = new Vector<>();
    private static final String FILE_NAME = "couriers.txt";

    // Inject repository from Spring context (set after construction)
    private CourierRepository repository;

    void setRepository(CourierRepository repository) {
        this.repository = repository;
    }

    CourierRepository getRepository() {
        return repository;
    }

    /** Directly add a courier to the in-memory vector (used during DB load at startup). */
    void addDirectly(Courier c) {
        couriers.add(c);
    }

    /** Add a new courier: validate no duplicate ID, insert into DB, then add to vector. */
    void addCourier(Courier c) {
        // Check duplicate ID in memory
        for (Courier existing : couriers) {
            if (existing.getId() == c.getId()) {
                throw new IllegalArgumentException("Courier with ID " + c.getId() + " already exists.");
            }
        }
        // Insert into database first
        if (repository != null) {
            repository.save(c);
        }
        // Only add to vector if DB insert succeeded (or no repository)
        couriers.add(c);
    }

    /** Display all couriers. */
    void displayAll() {
        if (couriers.isEmpty()) {
            System.out.println("No couriers found.");
            return;
        }
        System.out.println("------------------------------------------------------------");
        for (Courier c : couriers) {
            c.display();
            System.out.println("------------------------------------------------------------");
        }
    }

    /** Search by ID. Throws CourierNotFoundException if not found. */
    Courier searchById(int id) throws CourierNotFoundException {
        for (Courier c : couriers) {
            if (c.getId() == id) {
                return c;
            }
        }
        throw new CourierNotFoundException(id);
    }

    /** Remove courier by ID: delete from DB first, then from vector. */
    void removeCourier(int id) throws CourierNotFoundException {
        Courier target = searchById(id);
        if (repository != null) {
            repository.deleteById(id);
        }
        couriers.remove(target);
    }

    /** Sort by ID (ascending). */
    void sortById() {
        Collections.sort(couriers, Comparator.comparingInt(Courier::getId));
        System.out.println("Couriers sorted by ID.");
    }

    /** Sort by weight (ascending). */
    void sortByWeight() {
        couriers.sort(Comparator.comparingDouble(Courier::getWeight));
        System.out.println("Couriers sorted by weight.");
    }

    /** Save to text file asynchronously. */
    void saveToFileAsync() {
        new AsyncFileSaver(new Vector<>(couriers), FILE_NAME).start();
    }

    /** Load from text file (used only if DB load is unavailable). */
    void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("[File] No saved file found (" + FILE_NAME + "). Starting fresh.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int loaded = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|", 6);
                if (parts.length < 6) continue;
                char type   = parts[0].charAt(0);
                int    id   = Integer.parseInt(parts[1]);
                String sndr = parts[2];
                String rcvr = parts[3];
                String addr = parts[4];
                double wt   = Double.parseDouble(parts[5]);
                Courier c;
                if (type == 'D') {
                    c = new DomesticCourier(id, sndr, rcvr, addr, wt);
                } else {
                    c = new InternationalCourier(id, sndr, rcvr, addr, wt);
                }
                // Skip duplicates
                boolean dup = false;
                for (Courier existing : couriers) {
                    if (existing.getId() == id) { dup = true; break; }
                }
                if (!dup) {
                    couriers.add(c);
                    loaded++;
                }
            }
            System.out.println("[File] Loaded " + loaded + " courier(s) from " + FILE_NAME + ".");
        } catch (IOException | NumberFormatException e) {
            System.out.println("[File] Error loading file: " + e.getMessage());
        }
    }

    /** Trigger batch processing thread. */
    void batchProcess() {
        new CourierBatchProcessor(new Vector<>(couriers)).start();
    }

    int size() {
        return couriers.size();
    }
}

// ---------- Console UI ----------
public class CourierConsole {

    private final CourierManager manager;
    private final Scanner scanner;

    public CourierConsole(CourierManager manager) {
        this.manager = manager;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║     COURIER MANAGEMENT SYSTEM (CMS)     ║");
        System.out.println("║         Powered by Supabase + JDBC       ║");
        System.out.println("╚══════════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            System.out.println();
            switch (choice) {
                case 1 -> addCourier();
                case 2 -> manager.displayAll();
                case 3 -> searchCourier();
                case 4 -> removeCourier();
                case 5 -> sortMenu();
                case 6 -> {
                    manager.saveToFileAsync();
                    System.out.println("Save triggered asynchronously.");
                }
                case 7 -> {
                    manager.batchProcess();
                    System.out.println("Batch processing triggered.");
                }
                case 8 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please enter 1-8.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n========== MENU ==========");
        System.out.println("1. Add Courier");
        System.out.println("2. Display All Couriers");
        System.out.println("3. Search Courier by ID");
        System.out.println("4. Remove Courier");
        System.out.println("5. Sort Couriers");
        System.out.println("6. Save to File");
        System.out.println("7. Run Batch Processing");
        System.out.println("8. Exit");
        System.out.println("==========================");
    }

    private void addCourier() {
        System.out.println("--- Add Courier ---");
        int id = readInt("Enter Courier ID: ");
        String sender   = readString("Enter Sender name: ");
        String receiver = readString("Enter Receiver name: ");
        String address  = readString("Enter Delivery address: ");
        double weight   = readDouble("Enter Weight (kg): ");
        System.out.println("Select Type:");
        System.out.println("  1. Domestic");
        System.out.println("  2. International");
        int typeChoice = readInt("Enter type (1 or 2): ");

        try {
            Courier c;
            if (typeChoice == 1) {
                c = new DomesticCourier(id, sender, receiver, address, weight);
            } else if (typeChoice == 2) {
                c = new InternationalCourier(id, sender, receiver, address, weight);
            } else {
                System.out.println("Invalid type. Courier not added.");
                return;
            }
            manager.addCourier(c);
            System.out.println("Courier added successfully! Price: Rs. " + String.format("%.2f", c.getPrice()));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Database error while adding courier: " + e.getMessage());
        }
    }

    private void searchCourier() {
        int id = readInt("Enter Courier ID to search: ");
        try {
            Courier c = manager.searchById(id);
            System.out.println("--- Courier Found ---");
            c.display();
        } catch (CourierNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void removeCourier() {
        int id = readInt("Enter Courier ID to remove: ");
        try {
            manager.removeCourier(id);
            System.out.println("Courier with ID " + id + " removed successfully.");
        } catch (CourierNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Database error while removing courier: " + e.getMessage());
        }
    }

    private void sortMenu() {
        System.out.println("Sort by:");
        System.out.println("  1. ID");
        System.out.println("  2. Weight");
        int choice = readInt("Enter sort option: ");
        if (choice == 1) {
            manager.sortById();
        } else if (choice == 2) {
            manager.sortByWeight();
        } else {
            System.out.println("Invalid sort option.");
        }
    }

    // ---- Input helpers ----

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double val = Double.parseDouble(scanner.nextLine().trim());
                if (val <= 0) {
                    System.out.println("Value must be greater than 0.");
                } else {
                    return val;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String val = scanner.nextLine().trim();
            if (!val.isEmpty()) return val;
            System.out.println("This field cannot be empty.");
        }
    }
}
