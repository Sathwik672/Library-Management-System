package com.library.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.library.util.DBUtil;

public class LibraryDao {

    public void addBook(String title, String author) throws Exception {
        String sql = "INSERT INTO books (title, author, available) VALUES (?, ?, true)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.executeUpdate();
        }
    }

    public String getBooks() throws Exception {
        try (Connection con = DBUtil.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {

            StringBuilder json = new StringBuilder("[");
            while (rs.next()) {
                json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"title\":\"").append(rs.getString("title")).append("\",")
                        .append("\"author\":\"").append(rs.getString("author")).append("\",")
                        .append("\"available\":").append(rs.getBoolean("available"))
                        .append("},");
            }
            if (json.length() > 1 && json.charAt(json.length() - 1) == ',') {
                json.deleteCharAt(json.length() - 1);
            }
            json.append("]");
            return json.toString();
        }
    }

    public void deleteBook(int id) throws Exception {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM books WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void registerUser(String username) throws Exception {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO users(name) VALUES(?)")) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    public String getUsers() throws Exception {
        try (Connection con = DBUtil.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

            StringBuilder json = new StringBuilder("[");
            while (rs.next()) {
                json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"name\":\"").append(rs.getString("name")).append("\"")
                        .append("},");
            }
            if (json.length() > 1 && json.charAt(json.length() - 1) == ',') {
                json.deleteCharAt(json.length() - 1);
            }
            json.append("]");
            return json.toString();
        }
    }

    public String issueBook(int bookId, int userId) throws Exception {
        try (Connection con = DBUtil.getConnection()) {
            try (PreparedStatement checkUser = con.prepareStatement("SELECT id FROM users WHERE id=?")) {
                checkUser.setInt(1, userId);
                try (ResultSet userRs = checkUser.executeQuery()) {
                    if (!userRs.next()) {
                        return "❌ User not found!";
                    }
                }
            }

            try (PreparedStatement checkBook = con.prepareStatement("SELECT id FROM books WHERE id=? AND available=true")) {
                checkBook.setInt(1, bookId);
                try (ResultSet bookRs = checkBook.executeQuery()) {
                    if (!bookRs.next()) {
                        return " Book not available!";
                    }
                }
            }

            LocalDate issueDate = LocalDate.now();
            LocalDate dueDate = issueDate.plusDays(7);

            try (PreparedStatement ps1 = con.prepareStatement(
                    "INSERT INTO issues(book_id,user,issue_date,due_date) VALUES(?,?,?,?)")) {
                ps1.setInt(1, bookId);
                ps1.setInt(2, userId);
                ps1.setDate(3, Date.valueOf(issueDate));
                ps1.setDate(4, Date.valueOf(dueDate));
                ps1.executeUpdate();
            }

            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate("UPDATE books SET available=false WHERE id=" + bookId);
            }
            return " Book issued (Due in 7 days)";
        }
    }

    public String returnBook(int bookId) throws Exception {
        try (Connection con = DBUtil.getConnection()) {
            LocalDate due;
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT due_date FROM issues WHERE book_id=" + bookId
                         + " ORDER BY id DESC LIMIT 1")) {
                if (!rs.next()) {
                    return " No issue record found!";
                }
                due = rs.getDate("due_date").toLocalDate();
            }

            LocalDate today = LocalDate.now();
            long daysLate = ChronoUnit.DAYS.between(due, today);
            int fine = daysLate > 0 ? (int) daysLate * 10 : 0;

            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate("UPDATE books SET available=true WHERE id=" + bookId);
            }
            return "Returned. Fine = ₹" + fine;
        }
    }

    public String searchBooks(String key) throws Exception {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM books WHERE title LIKE ? OR author LIKE ?")) {

            ps.setString(1, "%" + key + "%");
            ps.setString(2, "%" + key + "%");

            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder json = new StringBuilder("[");
                while (rs.next()) {
                    json.append("{")
                            .append("\"id\":").append(rs.getInt("id")).append(",")
                            .append("\"title\":\"").append(rs.getString("title")).append("\",")
                            .append("\"author\":\"").append(rs.getString("author")).append("\"")
                            .append("},");
                }
                if (json.length() > 1 && json.charAt(json.length() - 1) == ',') {
                    json.deleteCharAt(json.length() - 1);
                }
                json.append("]");
                return json.toString();
            }
        }
    }
}