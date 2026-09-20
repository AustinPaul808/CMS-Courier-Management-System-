import java.util.Scanner;
import java.util.Vector;

class Courier {
    private int id;
    private String sender;
    private String receiver;
    private String address;
    private double weight;

    Courier(int id, String sender, String receiver, String address, double weight) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.address = address;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        // ...
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        // ...
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        // ...
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        // ...
    }

    void display() {
        System.out.println("ID: " + id);
        System.out.println("Sender: " + sender);
        System.out.println("Receiver: " + receiver);
        System.out.println("Address: " + address);
        System.out.println("Weight: " + weight);
    }
}

class DomesticCourier extends Courier {
    private double price;

    DomesticCourier(int id, String sender, String receiver,
                    String address, double weight) {
        super(id, sender, receiver, address, weight);
    }

    void calculatePrice() {
        // price calculation
    }

    @Override
    void display() {
        super.display();
        System.out.println("Type: Domestic");
        System.out.println("Price: " + price);
    }
}

class InternationalCourier extends Courier {
    private double price;

    InternationalCourier(int id, String sender, String receiver,
                         String address, double weight) {
        super(id, sender, receiver, address, weight);
    }

    void calculatePrice() {
        // ...
    }

    @Override
    void display() {
        super.display();
        System.out.println("Type: International");
        System.out.println("Price: " + price);
    }
}

class CourierManager {
    Vector<Courier> list = new Vector<>();

    void add(Courier c) {
        // ...
    }

    void displayAll() {
        for (Courier c : list) {
            // ...
        }
    }

    Courier find(int id) {
        for (Courier c : list) {
            if (c.getId() == id) {
                // ...
            }
        }

        return null;
    }

    void remove(int id) {
        Courier c = find(id);

        if (c != null) {
            // ...
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);