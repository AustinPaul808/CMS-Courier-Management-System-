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

// ---------- Multithreaded Feature: Asynchronous File Saver ----------
class AsyncFileSaver implements Runnable {
    private Vector<Courier> list;
    private String filename;

    public AsyncFileSaver(Vector<Courier> list, String filename) {
        this.list = list;
        this.filename = filename;
    }

    @Override
    public void run() {
        System.out.println("\n[Background Thread] Starting asynchronous save to " + filename + "...");
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (Courier c : list) {
                pw.println(c.toFileString());
            }
            Thread.sleep(1000); // Simulate background processing delay
            System.out.println("[Background Thread] Successfully saved " + list.size() + " couriers to " + filename + "!");
        } catch (IOException | InterruptedException e) {
            System.out.println("[Background Thread] Error during async save: " + e.getMessage());
        }
    }
}

// ---------- Multithreaded Feature: Concurrent Courier Processor ----------
class CourierBatchProcessor extends Thread {
    private Courier courier;

    public CourierBatchProcessor(Courier courier) {
        this.courier = courier;
    }

    @Override
    public void run() {
        System.out.println("[Thread-" + Thread.currentThread().getId() + "] Processing shipment ID: " + courier.getId() + " (" + courier.getType() + ")");
        try {
            Thread.sleep(500); // Simulate network/dispatch processing
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted.");
        }
        System.out.println("[Thread-" + Thread.currentThread().getId() + "] Shipment ID " + courier.getId() + " dispatched successfully!");
    }
}
 
// ---------- Manager (collection + file handling) ----------
class CourierManager {
    Vector<Courier> list = new Vector<>();
 
    void add(Courier c) {
        if (find(c.getId()) != null) {
            throw new IllegalArgumentException("A courier with ID " + c.getId() + " already exists.");
        }
        list.add(c);
    }
 
    void displayAll() {
        if (list.isEmpty()) {
            System.out.println("No couriers to show.");
            return;
        }
        for (Courier c : list) {
            System.out.println("-------------------------");
            c.display();
        }
        System.out.println("-------------------------");
        System.out.println("Total couriers: " + list.size());
    }
 
    Courier find(int id) {
        for (Courier c : list) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }
 
    void remove(int id) throws CourierNotFoundException {
        Courier c = find(id);
        if (c == null) {
            throw new CourierNotFoundException(id);
        }
        list.remove(c);
    }
 
    void sortByWeight() {
        Collections.sort(list, Comparator.comparingDouble(Courier::getWeight));
    }
 
    void sortByPrice() {
        Collections.sort(list, Comparator.comparingDouble(Courier::getPrice));
    }
 
    // ----- File handling -----
    void saveToFile(String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (Courier c : list) {
                pw.println(c.toFileString());
            }
        }
    }

    // Multithreaded Save
    void saveToFileAsync(String filename) {
        AsyncFileSaver saver = new AsyncFileSaver(list, filename);
        Thread t = new Thread(saver);
        t.start();
        System.out.println("Save task handed off to a background thread. You can continue using the menu.");
    }

    // Multithreaded Batch Dispatch Simulation
    void processAllConcurrently() {
        if (list.isEmpty()) {
            System.out.println("No couriers to process.");
            return;
        }
        System.out.println("Starting parallel processing of all shipments...");
        Vector<CourierBatchProcessor> threads = new Vector<>();
        for (Courier c : list) {
            CourierBatchProcessor tp = new CourierBatchProcessor(c);
            threads.add(tp);
            tp.start();
        }
        // Wait for all threads to finish
        for (CourierBatchProcessor tp : threads) {
            try {
                tp.join();
            } catch (InterruptedException e) {
                System.out.println("Thread error.");
            }
        }
        System.out.println("All shipments processed concurrently!");
    }
 
    void loadFromFile(String filename) throws IOException {
        File f = new File(filename);
        if (!f.exists()) {
            return; 
        }
 
        int skipped = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    String[] p = line.split("\\|");
                    int id = Integer.parseInt(p[1]);
                    double weight = Double.parseDouble(p[5]);
                    Courier c;
                    if (p[0].equals("D")) {
                        c = new DomesticCourier(id, p[2], p[3], p[4], weight);
                    } else if (p[0].equals("I")) {
                        c = new InternationalCourier(id, p[2], p[3], p[4], weight);
                    } else {
                        throw new IllegalArgumentException("Unknown type");
                    }
                    add(c);
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                    skipped++; 
                }
            }
        }
        if (skipped > 0) {
            System.out.println("Warning: skipped " + skipped + " corrupted line(s) in " + filename);
        }
    }
}

// ---------- Main Class (Terminal CLI) ----------
public class main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CourierManager manager = new CourierManager();
        String filename = "couriers.txt";

        // Load existing records on startup
        try {
            manager.loadFromFile(filename);
            System.out.println("Loaded existing data from " + filename);
        } catch (IOException e) {
            System.out.println("Could not load data file.");
        }

        int choice;
        do {
            System.out.println("\n===== COURIER MANAGEMENT SYSTEM =====");
            System.out.println("1. Add Domestic Courier");
            System.out.println("2. Add International Courier");
            System.out.println("3. Display All Couriers");
            System.out.println("4. Find Courier by ID");
            System.out.println("5. Remove Courier by ID");
            System.out.println("6. Sort by Weight");
            System.out.println("7. Sort by Price");
            System.out.println("8. Save to File (Synchronous)");
            System.out.println("9. Save to File (Asynchronously via Thread) ⚡");
            System.out.println("10. Process All Couriers Concurrently (Multithreading) ⚡");
            System.out.println("11. Exit & Save");
            System.out.print("Enter your choice (1-11): ");

            while (!scanner.hasNextInt()) {
                System.out.print("Please enter a valid number: ");
                scanner.next();
            }
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            try {
                switch (choice) {
                    case 1:
                    case 2:
                        System.out.print("Enter ID (integer): ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Enter Sender Name: ");
                        String sender = scanner.nextLine();
                        System.out.print("Enter Receiver Name: ");
                        String receiver = scanner.nextLine();
                        System.out.print("Enter Address: ");
                        String address = scanner.nextLine();
                        System.out.print("Enter Weight (kg): ");
                        double weight = scanner.nextDouble();

                        if (choice == 1) {
                            manager.add(new DomesticCourier(id, sender, receiver, address, weight));
                            System.out.println("Domestic courier added successfully!");
                        } else {
                            manager.add(new InternationalCourier(id, sender, receiver, address, weight));
                            System.out.println("International courier added successfully!");
                        }
                        break;

                    case 3:
                        manager.displayAll();
                        break;

                    case 4:
                        System.out.print("Enter Courier ID to search: ");
                        int searchId = scanner.nextInt();
                        Courier found = manager.find(searchId);
                        if (found != null) {
                            System.out.println("\nCourier Found:");
                            found.display();
                        } else {
                            System.out.println("Courier with ID " + searchId + " not found.");
                        }
                        break;

                    case 5:
                        System.out.print("Enter Courier ID to remove: ");
                        int removeId = scanner.nextInt();
                        manager.remove(removeId);
                        System.out.println("Courier removed successfully.");
                        break;

                    case 6:
                        manager.sortByWeight();
                        System.out.println("Couriers sorted by weight successfully!");
                        manager.displayAll();
                        break;

                    case 7:
                        manager.sortByPrice();
                        System.out.println("Couriers sorted by price successfully!");
                        manager.displayAll();
                        break;

                    case 8:
                        manager.saveToFile(filename);
                        System.out.println("Data saved synchronously to " + filename);
                        break;

                    case 9:
                        manager.saveToFileAsync(filename);
                        break;

                    case 10:
                        manager.processAllConcurrently();
                        break;

                    case 11:
                        manager.saveToFile(filename);
                        System.out.println("Data saved. Exiting... Thank you!");
                        break;

                    default:
                        System.out.println("Invalid choice! Please choose between 1 and 11.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

        } while (choice != 11);

        scanner.close();
    }
}