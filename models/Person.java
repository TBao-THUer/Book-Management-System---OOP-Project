package models;

import javax.swing.*;
import java.sql.*;

public abstract class Person {

    protected static final String DB_URL  = "jdbc:mysql://localhost:3306/java";
    protected static final String DB_USER = "java";
    protected static final String DB_PASS = "java";

    private String username;
    private String password;
    private String role;

    public Person(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public abstract boolean canManageBooks();
    public abstract boolean canViewStaff();
    
    public void borrow(){
        JOptionPane.showMessageDialog(null,"You have borrowed succesfully");
    }

    public void paid(){
        JOptionPane.showMessageDialog(null,"You have paid succesfully");
    }
    
    public void viewBooks() {
        String sql = "SELECT * FROM books ORDER BY id ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            StringBuilder sb = new StringBuilder("All Books:\n\n");

            while (rs.next()) {
                sb.append(rs.getInt("id")).append(" - ")
                  .append(rs.getString("title")).append(" (")
                  .append(rs.getString("author")).append(") - $")
                  .append(rs.getDouble("price")).append("\n");
            }

            JOptionPane.showMessageDialog(null, sb.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving books.");
        }
    }

    // ===== View books up to max price =====
    public void viewBooks(double maxPrice) {
        String sql = "SELECT * FROM books WHERE price <= ? ORDER BY price ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, maxPrice);

            try (ResultSet rs = stmt.executeQuery()) {

                StringBuilder sb = new StringBuilder(
                        "Books priced up to $" + maxPrice + ":\n\n"
                );

                boolean found = false;

                while (rs.next()) {
                    found = true;
                    sb.append(rs.getInt("id")).append(" - ")
                      .append(rs.getString("title")).append(" (")
                      .append(rs.getString("author")).append(") - $")
                      .append(rs.getDouble("price")).append("\n");
                }

                if (!found) {
                    JOptionPane.showMessageDialog(null,
                            "No books found under $" + maxPrice);
                } else {
                    JOptionPane.showMessageDialog(null, sb.toString());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving books.");
        }
    }
}

