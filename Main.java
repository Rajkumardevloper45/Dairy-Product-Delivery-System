package com.demo.raj.kumar;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Connection connection = DBConnection.getConnection();
            System.out.println("Connected to database.");

            while (true) {
                displayMainMenu();
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline character

                switch (choice) {
                    case 1:
                        signUpUser(connection);
                        break;
                    case 2:
                        loginUser(connection);
                        break;
                    case 3:
                        adminLogin(connection);
                        break;
                    case 4:
                        System.out.println("Exiting application.");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void signUpUser(Connection connection) throws SQLException {
        System.out.println("\n=== User Sign Up ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        // Check if user already exists
        if (checkUserExists(connection, email)) {
            System.out.println("User with this email already exists. Please try again.");
            return;
        }

        // Insert new user into database
        String insertUserQuery = "INSERT INTO signup (username, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertUserQuery)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("User signed up successfully.");
            } else {
                System.out.println("Failed to sign up user.");
            }
        }
    }

    private static void loginUser(Connection connection) throws SQLException {
        System.out.println("\n=== User Login ===");
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        String loginUserQuery = "SELECT * FROM signup WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(loginUserQuery)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("user_id"));
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));

                System.out.println("Login successful. Welcome, " + user.getUsername() + "!");
                displayProducts(connection);
                placeOrder(connection, user);
                checkDeliveryStatus(connection, user.getUserId());
            } else {
                System.out.println("Invalid email or password. Please try again.");
            }
        }
    }

    private static void adminLogin(Connection connection) throws SQLException {
        System.out.println("\n=== Admin Login ===");
        System.out.print("Enter admin email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter admin password: ");
        String password = scanner.nextLine().trim();

        String adminLoginQuery = "SELECT * FROM admin WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(adminLoginQuery)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                System.out.println("Admin login successful.");

                // Admin logged in successfully, now display admin menu
                displayAdminMenu(connection);
            } else {
                System.out.println("Invalid admin credentials. Please try again.");
            }
        }
    }

    private static void displayProducts(Connection connection) throws SQLException {
        System.out.println("\n=== Available Products ===");
        String productsQuery = "SELECT * FROM products";
        try (PreparedStatement stmt = connection.prepareStatement(productsQuery)) {
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Product product = new Product();
                product.setProductId(resultSet.getInt("product_id"));
                product.setProductName(resultSet.getString("product_name"));
                product.setDescription(resultSet.getString("description"));
                product.setPrice(resultSet.getDouble("price"));
                product.setStockQuantity(resultSet.getInt("stock_quantity"));

                System.out.println("---------------------------------");
                System.out.println("Product ID: " + product.getProductId());
                System.out.println("Name: " + product.getProductName());
                System.out.println("Description: " + product.getDescription());
                System.out.println("Price: $" + product.getPrice());
                System.out.println("Stock Quantity: " + product.getStockQuantity());
            }
        }
    }

    private static void placeOrder(Connection connection, User user) throws SQLException {
        System.out.println("\n=== Place Order ===");
        System.out.print("Enter product ID to order: ");
        int productId = scanner.nextInt();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();

        // Check if product exists and has enough stock
        if (!checkProductExists(connection, productId)) {
            System.out.println("Invalid product ID. Please try again.");
            return;
        }

        int currentStock = getProductStock(connection, productId);
        if (currentStock < quantity) {
            System.out.println("Insufficient stock for this product. Available stock: " + currentStock);
            return;
        }

        // Calculate total amount
        double price = getProductPrice(connection, productId);
        double totalAmount = price * quantity;

        // Insert order into database
        String insertOrderQuery = "INSERT INTO orders (user_id, total_amount) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertOrderQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, user.getUserId());
            stmt.setDouble(2, totalAmount);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);

                    // Insert order details
                    String insertOrderDetailsQuery = "INSERT INTO order_details (order_id, product_id, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement stmtDetails = connection.prepareStatement(insertOrderDetailsQuery)) {
                        stmtDetails.setInt(1, orderId);
                        stmtDetails.setInt(2, productId);
                        stmtDetails.setInt(3, quantity);
                        stmtDetails.executeUpdate();
                        System.out.println("Order placed successfully. Total amount: $" + totalAmount);
                    }
                }
            } else {
                System.out.println("Failed to place order.");
            }
        }
    }

    private static void checkDeliveryStatus(Connection connection, int userId) throws SQLException {
        System.out.println("\n=== Delivery Status ===");
        System.out.println("1. Transit Status");
        System.out.println("2. Delivered Status");
        System.out.println("3. All Statuses");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        String deliveryStatusQuery = "";
        switch (choice) {
            case 1:
                deliveryStatusQuery = "SELECT d.delivery_id, d.delivery_status, d.delivery_date, d.tracking_number, " +
                        "o.user_id " +
                        "FROM delivery d " +
                        "JOIN orders o ON d.order_id = o.order_id " +
                        "WHERE d.delivery_status = 'Transit'";
                break;
            case 2:
                deliveryStatusQuery = "SELECT d.delivery_id, d.delivery_status, d.delivery_date, d.tracking_number, " +
                        "o.user_id " +
                        "FROM delivery d " +
                        "JOIN orders o ON d.order_id = o.order_id " +
                        "WHERE d.delivery_status = 'Delivered'";
                break;
            case 3:
                deliveryStatusQuery = "SELECT d.delivery_id, d.delivery_status, d.delivery_date, d.tracking_number, " +
                        "o.user_id " +
                        "FROM delivery d " +
                        "JOIN orders o ON d.order_id = o.order_id";
                break;
            default:
                System.out.println("Invalid choice. Displaying all statuses.");
                deliveryStatusQuery = "SELECT d.delivery_id, d.delivery_status, d.delivery_date, d.tracking_number, " +
                        "o.user_id " +
                        "FROM delivery d " +
                        "JOIN orders o ON d.order_id = o.order_id";
        }

        try (PreparedStatement stmt = connection.prepareStatement(deliveryStatusQuery)) {
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Delivery delivery = new Delivery();
                delivery.setDeliveryId(resultSet.getInt("delivery_id"));
                delivery.setDeliveryStatus(resultSet.getString("delivery_status"));
                delivery.setDeliveryDate(resultSet.getDate("delivery_date"));
                delivery.setTrackingNumber(resultSet.getString("tracking_number"));

                System.out.println("---------------------------------");
                System.out.println("Delivery ID: " + delivery.getDeliveryId());
                System.out.println("Status: " + delivery.getDeliveryStatus());
                System.out.println("Delivery Date: " + delivery.getDeliveryDate());
                System.out.println("Tracking Number: " + delivery.getTrackingNumber());
                System.out.println("User ID: " + resultSet.getInt("user_id"));
            }
        }
    }

    private static boolean checkUserExists(Connection connection, String email) throws SQLException {
        String checkUserQuery = "SELECT * FROM signup WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkUserQuery)) {
            stmt.setString(1, email);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        }
    }

    private static boolean checkProductExists(Connection connection, int productId) throws SQLException {
        String checkProductQuery = "SELECT * FROM products WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkProductQuery)) {
            stmt.setInt(1, productId);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        }
    }

    private static int getProductStock(Connection connection, int productId) throws SQLException {
        String stockQuery = "SELECT stock_quantity FROM products WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(stockQuery)) {
            stmt.setInt(1, productId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("stock_quantity");
            } else {
                return 0;
            }
        }
    }

    private static double getProductPrice(Connection connection, int productId) throws SQLException {
        String priceQuery = "SELECT price FROM products WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(priceQuery)) {
            stmt.setInt(1, productId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("price");
            } else {
                return 0.0;
            }
        }
    }

    private static void displayMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Sign Up");
        System.out.println("2. Log In");
        System.out.println("3. Admin Login");
        System.out.println("4. Exit");
    }

    private static void displayAdminMenu(Connection connection) throws SQLException {
        boolean isAdminLoggedIn = true;

        while (isAdminLoggedIn) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. View Orders");
            System.out.println("2. Update Order Status");
            System.out.println("3. Log Out");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            switch (choice) {
                case 1:
                    viewOrders(connection);
                    break;
                case 2:
                    updateOrderStatus(connection);
                    break;
                case 3:
                    System.out.println("Logging out from admin account.");
                    isAdminLoggedIn = false; // Set flag to false to exit admin module
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewOrders(Connection connection) throws SQLException {
        System.out.println("\n=== View Orders ===");
        String ordersQuery = "SELECT * FROM orders";
        try (PreparedStatement stmt = connection.prepareStatement(ordersQuery)) {
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Order order = new Order();
                order.setOrderId(resultSet.getInt("order_id"));
                order.setUserId(resultSet.getInt("user_id"));
                order.setTotalAmount(resultSet.getDouble("total_amount"));
                order.setOrderDate(resultSet.getDate("order_date"));

                System.out.println("---------------------------------");
                System.out.println("Order ID: " + order.getOrderId());
                System.out.println("User ID: " + order.getUserId());
                System.out.println("Total Amount: $" + order.getTotalAmount());
                System.out.println("Order Date: " + order.getOrderDate());
            }
        }
    }

    private static void updateOrderStatus(Connection connection) throws SQLException {
        System.out.println("\n=== Update Order Status ===");
        System.out.print("Enter order ID to update status: ");
        int orderId = scanner.nextInt();
        scanner.nextLine(); // Consume newline character
        System.out.print("Enter new status (Transit or Delivered): ");
        String newStatus = scanner.nextLine().trim();

        // Check if order exists
        if (!checkOrderExists(connection, orderId)) {
            System.out.println("Invalid order ID. Please try again.");
            return;
        }

        // Update order status
        String updateStatusQuery = "UPDATE delivery SET delivery_status = ? WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateStatusQuery)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Order status updated successfully.");
            } else {
                System.out.println("Failed to update order status.");
            }
        }
    }

    private static boolean checkOrderExists(Connection connection, int orderId) throws SQLException {
        String checkOrderQuery = "SELECT * FROM orders WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkOrderQuery)) {
            stmt.setInt(1, orderId);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        }
    }
}
