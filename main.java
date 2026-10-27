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
