import java.sql.*;
import java.util.*;
public class Vehicle {
    public Connection getConnect() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/vehicle","root","");
        return conn;
    }
    public void logindetails(String email,String password) throws SQLException {
        Connection con=getConnect();
        Statement st= con.createStatement();
        String checkExistingUser = "SELECT user_email FROM user_details WHERE user_email='" + email + "'";
        ResultSet existingUserRows = st.executeQuery(checkExistingUser);
        if (existingUserRows.next()) {
            System.out.println("User already exists. Please sign in or use a different email for sign up.");
        } else {
            String insertSignUp = "INSERT INTO user_details(user_email, user_password) VALUES ('" + email + "','" + password + "')";
            int rows = st.executeUpdate(insertSignUp);
            System.out.println("SIGN_UP DETAILS INSERTED SUCCESSFULLY");
        }
    }
    public void addVehicle(String name, String type, int availableCount, String numberPlate, int securityDeposit) throws SQLException {
        try (Connection con = getConnect();
             PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO vehicles(name, type, available_count, number_plate, security_deposit) VALUES (?, ?, ?, ?, ?)")) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, type);
            preparedStatement.setInt(3, availableCount);
            preparedStatement.setString(4, numberPlate);
            preparedStatement.setInt(5, securityDeposit);

            int rows = preparedStatement.executeUpdate();
            System.out.println("Vehicle details added successfully");
        }
    }
    public void modifyVehicle(String name, int availableCount, String numberPlate) throws SQLException {
        try (Connection con = getConnect();
             PreparedStatement preparedStatement = con.prepareStatement("UPDATE vehicles SET available_count = ? WHERE name = ? AND number_plate = ?")) {

            preparedStatement.setInt(1, availableCount);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, numberPlate);

            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Vehicle specifications modified successfully");
            } else {
                System.out.println("Vehicle not found");
            }
        }
    }
    public void deleteVehicle(String name, String numberPlate) throws SQLException {
        try (Connection con = getConnect();
             PreparedStatement preparedStatement = con.prepareStatement("DELETE FROM vehicles WHERE name = ? AND number_plate = ?")) {

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, numberPlate);

            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Vehicle deleted successfully");
            } else {
                System.out.println("Vehicle not found");
            }
        }
    }
    public void viewAllVehicles() throws SQLException {
        try (Connection con = getConnect();
             Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM vehicles")) {

            System.out.println("*****ALL VEHICLES*****");
            System.out.printf("%-5s %-15s %-15s %-15s %-15s %-15s%n", "ID", "Name", "Type", "Available Count", "Number Plate", "Security Deposit");
            System.out.println("----------------------------------------------------------------------------------------------------");

            while (resultSet.next()) {
                int id = resultSet.getInt("vehicle_id");
                String name = resultSet.getString("name");
                String type = resultSet.getString("type");
                int availableCount = resultSet.getInt("available_count");
                String numberPlate = resultSet.getString("number_plate");
                int securityDeposit = resultSet.getInt("security_deposit");

                System.out.printf("%-5s %-15s %-15s %-15s %-15s %-15s%n", id, name, type, availableCount, numberPlate, securityDeposit);
            }
        }
    }
    public void searchVehicle(String searchKeyword) throws SQLException {
        try (Connection con = getConnect();
             PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM vehicles WHERE name LIKE ? OR number_plate LIKE ?")) {

            preparedStatement.setString(1, "%" + searchKeyword + "%");
            preparedStatement.setString(2, "%" + searchKeyword + "%");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("*****SEARCH RESULTS*****");
                    System.out.printf("%-5s %-15s %-15s %-15s %-15s %-15s%n", "ID", "Name", "Type", "Available Count", "Number Plate", "Security Deposit");
                    System.out.println("---------------------------------------------------------------------------------------------------------------------------");

                    do {
                        int id = resultSet.getInt("vehicle_id");
                        String name = resultSet.getString("name");
                        String type = resultSet.getString("type");
                        int availableCount = resultSet.getInt("available_count");
                        String numberPlate = resultSet.getString("number_plate");
                        int securityDeposit = resultSet.getInt("security_deposit");

                        System.out.printf("%-5s %-15s %-15s %-15s %-15s %-15s%n", id, name, type, availableCount, numberPlate, securityDeposit);
                    } while (resultSet.next());
                } else {
                    System.out.println("No matching vehicles found.");
                }
            }
        }
    }
    public void changeSecurityDeposit(int newSecurityDeposit) throws SQLException {
        try (Connection con = getConnect();
             Statement statement = con.createStatement()) {

            statement.executeUpdate("UPDATE vehicles SET security_deposit = " + newSecurityDeposit);
            System.out.println("Security deposit amount changed successfully for all vehicles.");
        }
    }
    public void viewAvailableVehicles() throws SQLException {
        Connection con = getConnect();
        Statement st = con.createStatement();
        String query = "SELECT * FROM vehicles WHERE available_count > 0";
        ResultSet result = st.executeQuery(query);

        System.out.println("*****AVAILABLE VEHICLES FOR RENT*****");
        while (result.next()) {
            System.out.println("ID: " + result.getInt("vehicle_id") +
                    ", Name: " + result.getString("name") +
                    ", Type: " + result.getString("type") +
                    ", Available Count: " + result.getInt("available_count") +
                    ", Security Deposit: " + result.getInt("security_deposit"));
        }
    }

    public void checkoutVehicle(int vehicleId, String userId) throws SQLException {
        Scanner sc = new Scanner(System.in);

        Connection con = getConnect();
        Statement st = con.createStatement();

        // Check if the vehicle exists and is available
        String checkAvailabilityQuery = "SELECT * FROM vehicles WHERE vehicle_id=" + vehicleId + " AND available_count > 0";
        ResultSet availabilityResult = st.executeQuery(checkAvailabilityQuery);

        if (availabilityResult.next()) {
            int securityDeposit = availabilityResult.getInt("security_deposit");
            int minimumSecurityDeposit = (securityDeposit >= 10000) ? 10000 : 3000; // Minimum security deposit requirement

            // Prompt user for the initial deposit
            System.out.println("Enter the amount you want to deposit (Minimum deposit: " + minimumSecurityDeposit + "):");
            int initialDeposit = sc.nextInt();

            if (initialDeposit >= minimumSecurityDeposit) {
                // Update vehicle availability and add to user's checkout cart
                String updateAvailabilityQuery = "UPDATE vehicles SET available_count = available_count - 1 WHERE vehicle_id=" + vehicleId;
                st.executeUpdate(updateAvailabilityQuery);

                String addToCartQuery = "INSERT INTO checkout(user_email, vehicle_id, initial_deposit) VALUES(" + userId + "," + vehicleId + "," + initialDeposit + ")";
                st.executeUpdate(addToCartQuery);

                System.out.println("Vehicle checked out successfully!");
            } else {
                System.out.println("Insufficient initial deposit. Minimum deposit required: " + minimumSecurityDeposit);
            }
        } else {
            System.out.println("Invalid vehicle ID or the vehicle is not available for rent.");
        }
    }


    public void viewCheckoutCart(String userId) throws SQLException {
        Connection con = getConnect();
        Statement st = con.createStatement();
        String query = "SELECT v.vehicle_id, v.name, v.type FROM checkout c JOIN vehicles v ON c.vehicle_id = v.vehicle_id WHERE c.user_email=" + userId;
        ResultSet result = st.executeQuery(query);

        System.out.println("*****CHECKOUT CART*****");
        while (result.next()) {
            System.out.println("ID: " + result.getInt("vehicle_id") +
                    ", Name: " + result.getString("name") +
                    ", Type: " + result.getString("type"));
        }
    }



    public static void main(String[] args) throws SQLException {
        Scanner sc = new Scanner(System.in);
        String value = "";
        Vehicle obj = new Vehicle();
        boolean flag = false;

        do {
            System.out.println("*****WELCOME*****");
            System.out.println("1) SIGN IN \n2) SIGN UP");

            int enter = sc.nextInt();
            sc.nextLine();
            System.out.println("Enter the email:");
            String email = sc.nextLine();
            System.out.println("Enter the password");
            String password = sc.nextLine();

            if (enter == 1) {
                if ("hari123@gmail.com".equals(email) && "hari123".equals(password)) {
                    value = "ADMIN";
                    flag = true;
                } else {
                    try {
                        Connection con = obj.getConnect();
                        Statement st = con.createStatement();
                        String check_email_password = "select user_email,user_password from user_details where user_email='" + email + "'AND user_password='" + password + "'";
                        ResultSet rows = st.executeQuery(check_email_password);
                        while (rows.next()) {
                            if (rows.getString(1).equals(email) && rows.getString(2).equals(password)) {
                                value = "USER";
                                flag = true;
                            }
                        }
                        if (!flag) {
                            System.out.println("You need to create an account: ");
                        }
                    } catch (SQLException e) {
                        System.out.println("Syntax error");
                    }
                }
            } else {
                obj.logindetails(email, password);
            }

            if (flag) {
                System.out.println(value);

                if ("ADMIN".equals(value)) {
                    int adminOption;
                    do {
                        System.out.println("*****ADMIN MENU*****");
                        System.out.println("1) Add Vehicle\n2) Modify Vehicle\n3) Delete Vehicle\n4) View All Vehicles\n5) Search Vehicles\n6) Change Security Deposit\n0) Logout");

                        adminOption = sc.nextInt();
                        sc.nextLine();

                        switch (adminOption) {
                            case 1:
                                System.out.println("Enter vehicle details:");
                                System.out.println("Name:");
                                String name = sc.nextLine();
                                System.out.println("Type:");
                                String type = sc.nextLine();
                                System.out.println("Available Count:");
                                int availableCount = sc.nextInt();
                                sc.nextLine();
                                System.out.println("Number Plate:");
                                String numberPlate = sc.nextLine();
                                System.out.println("Security Deposit:");
                                int securityDeposit = sc.nextInt();
                                obj.addVehicle(name, type, availableCount, numberPlate, securityDeposit);
                                break;

                            case 2:
                                System.out.println("Enter vehicle details to modify:");
                                System.out.println("Name:");
                                String modifyName = sc.nextLine();
                                System.out.println("New Available Count:");
                                int modifyAvailableCount = sc.nextInt();
                                sc.nextLine();
                                System.out.println("Number Plate:");
                                String modifyNumberPlate = sc.nextLine();
                                obj.modifyVehicle(modifyName, modifyAvailableCount, modifyNumberPlate);
                                break;

                            case 3:
                                System.out.println("Enter vehicle details to delete:");
                                System.out.println("Name:");
                                String deleteName = sc.nextLine();
                                System.out.println("Number Plate:");
                                String deleteNumberPlate = sc.nextLine();
                                obj.deleteVehicle(deleteName, deleteNumberPlate);
                                break;

                            case 4:
                                obj.viewAllVehicles();
                                break;

                            case 5:
                                System.out.println("Enter keyword to search:");
                                String searchKeyword = sc.nextLine();
                                obj.searchVehicle(searchKeyword);
                                break;

                            case 6:
                                System.out.println("Enter new security deposit amount:");
                                int newSecurityDeposit = sc.nextInt();
                                obj.changeSecurityDeposit(newSecurityDeposit);
                                break;
                            case 0:
                                System.out.println("Logged out as Admin");
                                value = "";
                                flag = false;
                                break;
                        }
                    } while (adminOption != 0);
                }
                if ("USER".equals(value)) {
                    int renterOption;
                    String userId = value;

                    do {
                        System.out.println("*****RENTER MENU*****");
                        System.out.println("1) View Available Vehicles\n2) Checkout Vehicle\n3) View Checkout Cart\n0) Logout");

                        renterOption = sc.nextInt();
                        sc.nextLine();
                        switch (renterOption) {
                            case 1:
                                obj.viewAvailableVehicles();
                                break;

                            case 2:
                                System.out.println("Enter the ID of the vehicle you want to rent:");
                                int vehicleId = sc.nextInt();
                                obj.checkoutVehicle(vehicleId, userId);
                                break;

                            case 3:
                                obj.viewCheckoutCart(userId);
                                break;

                            case 0:
                                System.out.println("Logged out as Renter");
                                value = "";
                                flag = true;
                                break;
                        }
                    } while (renterOption != 0);
                }
            }
        } while (!flag);
    }
}