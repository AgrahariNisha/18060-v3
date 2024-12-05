import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Class Store Item
class StationeryItem {
    private String name;
    private double price;
    private int quantity;

    public StationeryItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void subtractFromQuantity(int quantIn) {
        if (quantIn <= quantity) {
            this.quantity = this.quantity - quantIn;
        }
    }
}

// Starting Class
public class StationeryItemsShop {
    private static final String DB_URL = "jdbc:mysql://localhost:3307/<stationary_shop_db>";
    private static final String USER = "<root>";
    private static final String PASS = "<nisha>";

    public static void main(String[] args) {
        List<StationeryItem> inventory = new ArrayList<>();
        double percentSale = 0;

        // Loading the JDBC Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL JDBC Driver
        } catch (ClassNotFoundException lstde) {
            System.out.println("JDBC Driver not found.");
            lstde.printStackTrace();
            return;
        }

        // Connecting to the database and Getting Items
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            String query = "select name, price, quantity from stationary_items";
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                inventory.add(new StationeryItem(name, price, quantity));
            }
        } catch (SQLException e) {
            System.out.println("Some error occurred while trying to get access to DB.");
            e.printStackTrace();
            return;
        }

        // Allow scanner object to read the input from the user
        Scanner scan = new Scanner(System.in);

        // Display menu
        System.out.println("Welcome to the Stationary Shop!");
        System.out.println("-------------------------------");
        for (int i = 0; i < inventory.size(); i++) {
            StationeryItem item = inventory.get(i);
            System.out.println((i + 1) + ". " + item.getName() + " - $" + item.getPrice());
        }
        System.out.println("-------------------------------");

        // Loop till user chooses to exit
        double totalSales = 0;
        while (true) {
            System.out.print("Enter the number of the product you'd like to purchase (0 for exit): ");
            int choice = scan.nextInt();

            if (choice == 0)
                break;
            else if (choice > 0 && choice <= inventory.size()) {
                System.out.printf("Enter quantity you would like to purchase: ");
                int quantity = scan.nextInt();

                StationeryItem selectedItem = inventory.get(choice - 1);

                if (quantity > 0 && quantity <= selectedItem.getQuantity()) {
                    double subtotal = selectedItem.getPrice() * quantity;
                    totalSales += subtotal;
                    selectedItem.subtractFromQuantity(quantity);
                    System.out.println("You got " + subtotal + " - " + quantity + " " + selectedItem.getName());

                    try {
                        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                        String updateQuery = "UPDATE stationery_items SET quantity=? WHERE name=?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                        updateStmt.setInt(1, selectedItem.getQuantity());
                        updateStmt.setString(2, selectedItem.getName());
                        updateStmt.executeUpdate();
                    } catch (SQLException e) {
                        System.out.println("Error occurred while updating the inventory.");
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Sorry, we do not have enough stock available.");
                }
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }

        System.out.println("Thanks for shopping at the Stationary Shop!");
        System.out.println("Total Sales: $" + totalSales);
    }
}