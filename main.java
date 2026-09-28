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
        CourierManager manager = new CourierManager();

        int choice;

        do {
            System.out.println("\n===== Courier Management System =====");
            System.out.println("1. Add Domestic Courier");
            System.out.println("2. Add International Courier");
            System.out.println("3. Display All Couriers");
            System.out.println("4. Search Courier by ID");
            System.out.println("5. Remove Courier");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    System.out.print("Enter ID: ");
                    int id1 = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter Sender Name: ");
                    String sender1 = sc.nextLine();

                    System.out.print("Enter Receiver Name: ");
                    String receiver1 = sc.nextLine();

                    System.out.print("Enter Address: ");
                    String address1 = sc.nextLine();

                    System.out.print("Enter Weight (kg): ");
                    double weight1 = sc.nextDouble();

                    manager.add(new DomesticCourier(id1, sender1, receiver1, address1, weight1));
                    break;

                case 2:
                    System.out.print("Enter ID: ");
                    int id2 = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter Sender Name: ");
                    String sender2 = sc.nextLine();

                    System.out.print("Enter Receiver Name: ");
                    String receiver2 = sc.nextLine();

                    System.out.print("Enter Address: ");
                    String address2 = sc.nextLine();

                    System.out.print("Enter Weight (kg): ");
                    double weight2 = sc.nextDouble();

                    manager.add(new InternationalCourier(id2, sender2, receiver2, address2, weight2));
                    break;

                case 3:
                    manager.displayAll();
                    break;

                case 4:
                    System.out.print("Enter Courier ID to Search: ");
                    int searchId = sc.nextInt();

                    Courier c = manager.find(searchId);

                    if (c != null) {
                        System.out.println("Courier Found:");
                        c.display();
                    } else {
                        System.out.println("Courier Not Found!");
                    }
                    break;

                case 5:
                    System.out.print("Enter Courier ID to Remove: ");
                    int removeId = sc.nextInt();
                    manager.remove(removeId);
                    break;

                case 6:
                    System.out.println("Exiting Courier Management System...");
                    break;

                default:
                    System.out.println("Invalid Choice!");
            }

        } while (choice != 6);

        sc.close();

    }
}    
