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
 
    void loadFromFile(String filename) throws IOException {
        File f = new File(filename);
        if (!f.exists()) {
            return; // first run, nothing to load
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
                    skipped++; // bad line, skip it
                }
            }
        }
        if (skipped > 0) {
            System.out.println("Warning: skipped " + skipped + " corrupted line(s) in " + filename);
        }
    }
}
