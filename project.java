import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * ============================================================================
 * DEPARTMENT OF COMPUTER SCIENCE AND ENGINEERING
 * MINI PROJECT: COURIER & LOGISTICS TRACKING SYSTEM
 * 
 * AUTHOR: M. CHANDRA RAJ (REG NO: 410625104111)
 * INSTITUTION: DHAANISH CHENNAI AUTONOMOUS
 * ============================================================================
 */

// Delivery status enum
enum DeliveryStatus {
    BOOKED("Order Booked"),
    PICKED_UP("Picked Up"),
    IN_TRANSIT("In Transit"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String label;

    DeliveryStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

// Destination Address Class
class Address implements Serializable {
    private static final long serialVersionUID = 1L;
    private String street, city, state, zipCode;

    public Address(String street, String city, String state, String zipCode) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }

    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " - " + zipCode;
    }
}

// Shipment Data Entity
class Shipment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String trackingId;
    private String senderName;
    private String senderPhone;
    private String receiverName;
    private String receiverPhone;
    private Address destinationAddress;
    private String packageType;
    private double weightKg;
    private double shippingCost;
    private DeliveryStatus currentStatus;
    private String bookingDate;
    private List<String> statusHistory;
    private String adminRemarks;

    public Shipment(String trackingId, String senderName, String senderPhone,
                    String receiverName, String receiverPhone, Address destinationAddress,
                    String packageType, double weightKg) {
        this.trackingId = trackingId;
        this.senderName = senderName;
        this.senderPhone = senderPhone;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.destinationAddress = destinationAddress;
        this.packageType = packageType;
        this.weightKg = weightKg;
        this.shippingCost = calculateCost(weightKg, packageType);
        this.currentStatus = DeliveryStatus.BOOKED;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.bookingDate = sdf.format(new Date());

        this.statusHistory = new ArrayList<>();
        this.statusHistory.add("[" + bookingDate + "] Order Booked successfully.");
        this.adminRemarks = "Initial shipment creation.";
    }

    private double calculateCost(double weight, String type) {
        double baseRate = 50.0;
        double weightRate = weight * 20.0;
        double typeMultiplier = 1.0;

        if (type.equalsIgnoreCase("Express") || type.equalsIgnoreCase("Fragile")) {
            typeMultiplier = 1.5;
        } else if (type.equalsIgnoreCase("Document")) {
            typeMultiplier = 0.8;
        }
        return (baseRate + weightRate) * typeMultiplier;
    }

    public String getTrackingId() { return trackingId; }
    public String getSenderName() { return senderName; }
    public DeliveryStatus getCurrentStatus() { return currentStatus; }
    public double getShippingCost() { return shippingCost; }

    public void updateStatus(DeliveryStatus newStatus, String remark) {
        this.currentStatus = newStatus;
        this.adminRemarks = remark;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        this.statusHistory.add("[" + timestamp + "] Status changed to: " + newStatus.getLabel() + " | Note: " + remark);
    }

    public void displayDetailedReport() {
        System.out.println("================================================================================");
        System.out.println("                        SHIPMENT DETAILED TRACKING REPORT                       ");
        System.out.println("================================================================================");
        System.out.printf(" TRACKING ID      : %-30s BOOKING DATE : %s\n", trackingId, bookingDate);
        System.out.printf(" SENDER NAME      : %-30s PHONE        : %s\n", senderName, senderPhone);
        System.out.printf(" RECEIVER NAME    : %-30s PHONE        : %s\n", receiverName, receiverPhone);
        System.out.printf(" DESTINATION      : %s\n", destinationAddress.toString());
        System.out.printf(" PACKAGE TYPE     : %-15s WEIGHT: %-8.2f kg COST: Rs. %.2f\n", packageType, weightKg, shippingCost);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf(" CURRENT STATUS   : [%s]\n", currentStatus.getLabel());
        System.out.printf(" LATEST REMARKS   : %s\n", adminRemarks);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(" STATUS AUDIT HISTORY LOG:");
        for (String log : statusHistory) {
            System.out.println("   -> " + log);
        }
        System.out.println("================================================================================");
    }

    public void displaySummaryRow() {
        System.out.printf("| %-12s | %-15s | %-15s | %-16s | Rs.%-7.2f |\n",
                trackingId, senderName, receiverName, currentStatus.getLabel(), shippingCost);
    }
}

// Base User Class
abstract class User {
    protected String userId, name, email;

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public abstract void showMenu(ShipmentManager manager, Scanner scanner);
}

// Customer User Implementation
class CustomerUser extends User {
    public CustomerUser(String userId, String name, String email) {
        super(userId, name, email);
    }

    @Override
    public void showMenu(ShipmentManager manager, Scanner scanner) {
        while (true) {
            System.out.println("\n------------------------------------------------");
            System.out.println("            CUSTOMER PORTAL - MAIN MENU         ");
            System.out.println("------------------------------------------------");
            System.out.println(" 1. Book a New Shipment");
            System.out.println(" 2. Track Shipment by Tracking ID");
            System.out.println(" 3. View My Shipment History");
            System.out.println(" 4. Return to Main Screen");
            System.out.print(" Select Option (1-4): ");

            int choice = readInt(scanner);
            switch (choice) {
                case 1: bookShipment(manager, scanner); break;
                case 2: trackShipment(manager, scanner); break;
                case 3: manager.viewShipmentsBySender(this.name); break;
                case 4: return;
                default: System.out.println(" [!] Invalid option.");
            }
        }
    }

    private void bookShipment(ShipmentManager manager, Scanner scanner) {
        System.out.println("\n--- BOOK NEW SHIPMENT ---");
        System.out.print(" Enter Sender Phone: ");
        String sPhone = scanner.nextLine();
        System.out.print(" Enter Receiver Name: ");
        String rName = scanner.nextLine();
        System.out.print(" Enter Receiver Phone: ");
        String rPhone = scanner.nextLine();
        System.out.print(" Enter Street Address: ");
        String street = scanner.nextLine();
        System.out.print(" Enter Destination City: ");
        String city = scanner.nextLine();
        System.out.print(" Enter Destination State: ");
        String state = scanner.nextLine();
        System.out.print(" Enter Zip Code: ");
        String zip = scanner.nextLine();

        System.out.print(" Enter Package Type (Standard/Express/Document/Fragile): ");
        String pkgType = scanner.nextLine();
        System.out.print(" Enter Package Weight (kg): ");
        double weight = readDouble(scanner);

        String tid = manager.generateTrackingId();
        Shipment s = new Shipment(tid, this.name, sPhone, rName, rPhone, new Address(street, city, state, zip), pkgType, weight);
        manager.addShipment(s);

        System.out.println("\n [SUCCESS] Shipment booked successfully!");
        System.out.println(" [SUCCESS] Generated Tracking ID: " + tid);
        System.out.printf(" [SUCCESS] Total Shipping Freight: Rs. %.2f\n", s.getShippingCost());
    }

    private void trackShipment(ShipmentManager manager, Scanner scanner) {
        System.out.print("\n Enter Tracking ID (e.g., CL20261001): ");
        String tid = scanner.nextLine().trim();
        Shipment s = manager.findShipment(tid);
        if (s != null) {
            s.displayDetailedReport();
        } else {
            System.out.println(" [ERROR] Shipment ID not found.");
        }
    }

    private int readInt(Scanner sc) {
        try { return Integer.parseInt(sc.nextLine().trim()); } catch (Exception e) { return -1; }
    }

    private double readDouble(Scanner sc) {
        try { return Double.parseDouble(sc.nextLine().trim()); } catch (Exception e) { return 1.0; }
    }
}

// Administrator User Implementation
class AdminUser extends User {
    public AdminUser(String userId, String name, String email) {
        super(userId, name, email);
    }

    @Override
    public void showMenu(ShipmentManager manager, Scanner scanner) {
        while (true) {
            System.out.println("\n------------------------------------------------");
            System.out.println("          ADMINISTRATOR CONTROL DASHBOARD       ");
            System.out.println("------------------------------------------------");
            System.out.println(" 1. View Key System Metrics");
            System.out.println(" 2. Display All Shipments");
            System.out.println(" 3. Update Shipment Delivery Status");
            System.out.println(" 4. Return to Main Menu");
            System.out.print(" Select Option (1-4): ");

            int choice = readInt(scanner);
            switch (choice) {
                case 1: manager.displayMetrics(); break;
                case 2: manager.displayAllShipments(); break;
                case 3: updateStatus(manager, scanner); break;
                case 4: return;
                default: System.out.println(" [!] Invalid option.");
            }
        }
    }

    private void updateStatus(ShipmentManager manager, Scanner scanner) {
        System.out.print("\n Enter Tracking ID: ");
        String tid = scanner.nextLine().trim();
        Shipment s = manager.findShipment(tid);
        if (s == null) {
            System.out.println(" [ERROR] Shipment record not found.");
            return;
        }

        System.out.println(" Current Status: " + s.getCurrentStatus().getLabel());
        System.out.println(" Select New Status: 1.Picked Up 2.In Transit 3.Out for Delivery 4.Delivered 5.Cancelled");
        System.out.print(" Choice (1-5): ");

        int stChoice = readInt(scanner);
        DeliveryStatus newStatus = s.getCurrentStatus();
        switch (stChoice) {
            case 1: newStatus = DeliveryStatus.PICKED_UP; break;
            case 2: newStatus = DeliveryStatus.IN_TRANSIT; break;
            case 3: newStatus = DeliveryStatus.OUT_FOR_DELIVERY; break;
            case 4: newStatus = DeliveryStatus.DELIVERED; break;
            case 5: newStatus = DeliveryStatus.CANCELLED; break;
            default: System.out.println(" [!] Keeping current status."); return;
        }

        System.out.print(" Enter Delivery Remarks / Notes: ");
        String remark = scanner.nextLine();
        s.updateStatus(newStatus, remark);
        manager.saveDataToFile();
        System.out.println(" [SUCCESS] Status updated successfully!");
    }

    private int readInt(Scanner sc) {
        try { return Integer.parseInt(sc.nextLine().trim()); } catch (Exception e) { return -1; }
    }
}

// Logistics Management System Repository Engine
class ShipmentManager {
    private List<Shipment> shipmentList;
    private int counter;
    private static final String DATA_FILE = "shipments_db.dat";

    public ShipmentManager() {
        this.shipmentList = new ArrayList<>();
        this.counter = 1001;
        loadDataFromFile();
    }

    public synchronized String generateTrackingId() {
        return "CL2026" + (counter++);
    }

    public void addShipment(Shipment shipment) {
        shipmentList.add(shipment);
        saveDataToFile();
    }

    public Shipment findShipment(String trackingId) {
        for (Shipment s : shipmentList) {
            if (s.getTrackingId().equalsIgnoreCase(trackingId)) return s;
        }
        return null;
    }

    public void displayAllShipments() {
        if (shipmentList.isEmpty()) {
            System.out.println(" [INFO] No shipments registered.");
            return;
        }
        System.out.println("\n+--------------+-----------------+-----------------+------------------+------------+");
        System.out.println("| TRACKING ID  | SENDER NAME     | RECEIVER NAME   | STATUS           | COST (RS)  |");
        System.out.println("+--------------+-----------------+-----------------+------------------+------------+");
        for (Shipment s : shipmentList) {
            s.displaySummaryRow();
        }
        System.out.println("+--------------+-----------------+-----------------+------------------+------------+");
    }

    public void viewShipmentsBySender(String senderName) {
        System.out.println("\n Historical Shipments for: " + senderName);
        boolean found = false;
        for (Shipment s : shipmentList) {
            if (s.getSenderName().equalsIgnoreCase(senderName)) {
                s.displaySummaryRow();
                found = true;
            }
        }
        if (!found) System.out.println(" [INFO] No shipments found.");
    }

    public void displayMetrics() {
        int total = shipmentList.size(), booked = 0, inTransit = 0, delivered = 0;
        double totalRevenue = 0;

        for (Shipment s : shipmentList) {
            totalRevenue += s.getShippingCost();
            if (s.getCurrentStatus() == DeliveryStatus.BOOKED) booked++;
            else if (s.getCurrentStatus() == DeliveryStatus.IN_TRANSIT) inTransit++;
            else if (s.getCurrentStatus() == DeliveryStatus.DELIVERED) delivered++;
        }

        System.out.println("\n=================================================");
        System.out.println("          LOGISTICS SYSTEM KEY METRICS           ");
        System.out.println("=================================================");
        System.out.printf(" Total Registered Shipments : %d\n", total);
        System.out.printf(" Pending / Booked Parcels  : %d\n", booked);
        System.out.printf(" Parcels In-Transit         : %d\n", inTransit);
        System.out.printf(" Successfully Delivered    : %d\n", delivered);
        System.out.printf(" Total Revenue Freight     : Rs. %.2f\n", totalRevenue);
        System.out.println("=================================================");
    }

    @SuppressWarnings("unchecked")
    public void loadDataFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            shipmentList = (List<Shipment>) ois.readObject();
            counter = ois.readInt();
        } catch (Exception e) {
            System.out.println(" [SYSTEM] Starting fresh session.");
        }
    }

    public void saveDataToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(shipmentList);
            oos.writeInt(counter);
        } catch (Exception e) {
            System.out.println(" [ERROR] Storage save failed.");
        }
    }
}

// Application Entry Point Driver
public class CourierTrackingSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ShipmentManager manager = new ShipmentManager();

        if (manager.findShipment("CL20261001") == null) {
            Address a = new Address("12 GST Road", "Chennai", "Tamil Nadu", "600045");
            Shipment s = new Shipment(manager.generateTrackingId(), "M. Chandra Raj", "9876543210",
                    "Ravi Kumar", "9123456789", a, "Standard", 2.5);
            manager.addShipment(s);
        }

        while (true) {
            System.out.println("\n================================================================================");
            System.out.println("                  DHAANISH CHENNAI - LOGISTICS PORTAL                           ");
            System.out.println("                    COURIER & LOGISTICS TRACKING SYSTEM                         ");
            System.out.println("================================================================================");
            System.out.println(" 1. Customer Portal (Book / Track Shipment)");
            System.out.println(" 2. Administrator Login (Status & Logistics Management)");
            System.out.println(" 3. Exit Application");
            System.out.print(" Select Gateway Option (1-3): ");

            int option = readInt(scanner);
            switch (option) {
                case 1:
                    System.out.print("\n Enter Customer Name: ");
                    String name = scanner.nextLine().trim();
                    if (name.isEmpty()) name = "M. Chandra Raj";
                    new CustomerUser("CUST101", name, "customer@dhaanish.in").showMenu(manager, scanner);
                    break;
                case 2:
                    System.out.print("\n Enter Admin Password: ");
                    String pwd = scanner.nextLine();
                    if (pwd.equals("admin") || pwd.equals("admin123")) {
                        new AdminUser("ADM001", "Dhanushiya Guide", "admin@dhaanish.in").showMenu(manager, scanner);
                    } else {
                        System.out.println(" [ERROR] Invalid Admin Password.");
                    }
                    break;
                case 3:
                    System.out.println("\n [SYSTEM] Saving state and shutting down. Goodbye!");
                    manager.saveDataToFile();
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println(" [!] Invalid Option.");
            }
        }
    }

    private static int readInt(Scanner sc) {
        try { return Integer.parseInt(sc.nextLine().trim()); } catch (Exception e) { return -1; }
    }
}
