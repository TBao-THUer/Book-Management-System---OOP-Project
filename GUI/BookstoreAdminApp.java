package GUI;

import models.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Vector;


public class BookstoreAdminApp extends JFrame {

    // --- DATABASE CONFIG --- //
    private static final String DB_URL = "jdbc:mysql://localhost:3306/java";
    private static final String DB_USER = "java";
    private static final String DB_PASS = "java";

    private String userRole;

    // --- BOOK TABLE COMPONENTS --- //
    private JTable bookTable;
    private DefaultTableModel bookModel;

    // --- STAFF TABLE COMPONENTS --- //
    private JTable staffTable;
    private DefaultTableModel staffModel;

    private Person user;

public BookstoreAdminApp(Person user) {
    this.user = user;

    setTitle("Bookstore Admin Panel - Logged in as: " + user.getRole());

    JTabbedPane tabs = new JTabbedPane();

    tabs.add("Inventory", createBookPanel());

    if (user.canViewStaff()) {
        tabs.add("Staff", createStaffPanel());
    }

    add(tabs);

    loadBooksFromDB();
    if (user.canViewStaff()) loadStaffFromDB();

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1300, 600);
    setLocationRelativeTo(null);
    setVisible(true);
}

    private JPanel createBookPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    bookModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Price", "Quantity","Type"}, 0);
    bookTable = new JTable(bookModel);

    JPanel buttonPanel = new JPanel();
    JButton addBtnB = new JButton("Add Book");
    JButton updateBtnB = new JButton("Update Book");
    JButton deleteBtnB = new JButton("Delete Book");

    JButton addBtnM = new JButton("Add Magazine");
    JButton updateBtnM = new JButton("Update Magazine");
    JButton deleteBtnM = new JButton("Delete Magazine");
    JButton btnViewAllBooks = new JButton("View All Books");
    JButton btnViewBooksByPrice = new JButton("View Books by Max Price");



    
    // Attach actions
    addBtnB.addActionListener(e->{ addItem("Book"); });
    updateBtnB.addActionListener(e->{ updateItem("Book"); });
    deleteBtnB.addActionListener(e->{ deleteItem("Book"); });
    addBtnM.addActionListener(e->{ addItem("Magazine"); });
    updateBtnM.addActionListener(e->{ updateItem("Magazine"); });
    deleteBtnM.addActionListener(e->{ deleteItem("Magazine"); });
    
    btnViewAllBooks.addActionListener(e -> {
        user.viewBooks();  
    });

    btnViewBooksByPrice.addActionListener(e -> {
        String input = JOptionPane.showInputDialog(
            null,
            "Enter maximum price:",
            "Filter Books by Price",
            JOptionPane.QUESTION_MESSAGE
        );

        if (input != null && !input.trim().isEmpty()) {
            try {
                double price = Double.parseDouble(input.trim());
                user.viewBooks(price);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid price.");
            }
        }
    });


    // --- ROLE RESTRICTION ---
    if (!user.canManageBooks()) {
    JButton borrowBtn = new JButton("Borrow");
    JButton paidBtn = new JButton("Paid");
    borrowBtn.addActionListener(e->{user.borrow();});
    paidBtn.addActionListener(e->{user.paid();});
    buttonPanel.add(borrowBtn);
    buttonPanel.add(paidBtn);
    addBtnB.setEnabled(false);
    updateBtnB.setEnabled(false);
    deleteBtnB.setEnabled(false);
    addBtnM.setEnabled(false);
    updateBtnM.setEnabled(false);
    deleteBtnM.setEnabled(false);
    }


    buttonPanel.add(addBtnB);
    buttonPanel.add(updateBtnB);
    buttonPanel.add(deleteBtnB);
    buttonPanel.add(addBtnM);
    buttonPanel.add(updateBtnM);
    buttonPanel.add(deleteBtnM);
    buttonPanel.add(btnViewAllBooks);
    buttonPanel.add(btnViewBooksByPrice);

    panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.SOUTH);
    return panel;
}

    // STAFF MANAGEMENT PANEL
    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        staffModel = new DefaultTableModel(new String[]{"ID", "Name", "Role"}, 0);
        staffTable = new JTable(staffModel);

        JPanel buttonPanel = new JPanel();

        panel.add(new JScrollPane(staffTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
    /*
    private void borrow(ActionEvent e){
        JOptionPane.showMessageDialog(this,"You have borrowed succesfully");
    }

    private void paid(ActionEvent e){
        JOptionPane.showMessageDialog(this,"You have paid succesfully");
    }
    */
    // BOOK DATABASE 
    private void loadBooksFromDB() {
        bookModel.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("title"));
                row.add(rs.getString("author"));
                row.add(rs.getDouble("price"));
                row.add(rs.getInt("quantity"));
                row.add(rs.getString("type"));
                bookModel.addRow(row);
            }
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }
    }

    private void addItem(String type) {
        String title = JOptionPane.showInputDialog("Enter title:");
        if (title == null) return;
        String author = JOptionPane.showInputDialog("Enter author:");
        if (author == null) return;
        double price = Double.parseDouble(JOptionPane.showInputDialog("Enter price:"));
        int quantity = Integer.parseInt(JOptionPane.showInputDialog("Enter quantity:"));

        String sql = "INSERT INTO books (title, author, price, quantity, type) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, author);
            ps.setDouble(3, price);
            ps.setInt(4, quantity);
            ps.setString(5, type);
            ps.executeUpdate();

            loadBooksFromDB();
            conn.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding book: " + ex.getMessage());
        }
    }

    private void updateItem(String type) {
        int row = bookTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a book first."); return; }

        int id = (int) bookModel.getValueAt(row, 0);

        String newTitle = JOptionPane.showInputDialog("New title:", bookModel.getValueAt(row, 1));
        if (newTitle == null) return;

        String newAuthor = JOptionPane.showInputDialog("New author:", bookModel.getValueAt(row, 2));
        if (newAuthor == null) return;

        double newPrice = Double.parseDouble(JOptionPane.showInputDialog("New price:", bookModel.getValueAt(row, 3)));
        int newQuantity = Integer.parseInt(JOptionPane.showInputDialog("New quantity:", bookModel.getValueAt(row, 4)));

        String sql = "UPDATE books SET title=?, author=?, price=?, quantity=?, type=? WHERE id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newTitle);
            ps.setString(2, newAuthor);
            ps.setDouble(3, newPrice);
            ps.setInt(4, newQuantity);
            ps.setInt(5, id);
            ps.setString(6,type);
            ps.executeUpdate();

            loadBooksFromDB();
            conn.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating book: " + ex.getMessage());
        }
    }

    private void deleteItem(String type) {
        int row = bookTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a book first."); return; }

        int id = (int) bookModel.getValueAt(row, 0);

        String sql = "DELETE FROM books WHERE id=? AND type=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2,type);
            ps.executeUpdate();
            loadBooksFromDB();
            conn.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deleting book: " + ex.getMessage());
        }
    }


    private void loadStaffFromDB() {
        staffModel.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM staff")) {
        
            while (rs.next()) {
    
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("role"));
                staffModel.addRow(row);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading staff: " + e.getMessage());
        }
    }

}
