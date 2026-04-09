package com.library.ui;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.library.util.DBUtil;

import io.javalin.Javalin;

public class LibraryApp {

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(8080);

        app.get("/", ctx -> ctx.redirect("/index.html"));

        // ================= BOOK =================

        app.post("/add-book", ctx -> {
            String title = ctx.formParam("title");
            String author = ctx.formParam("author");

            try (Connection con = DBUtil.getConnection();
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO books(title,author,available) VALUES(?,?,true)")) {

                ps.setString(1, title);
                ps.setString(2, author);
                ps.executeUpdate();
                ctx.result("Book added");
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error: " + e.getMessage());
            }
        });

        app.get("/books", ctx -> {
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
                ctx.contentType("application/json");
                ctx.result(json.toString());
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("[]");
            }
        });

        app.post("/delete-book", ctx -> {
            try {
                int id = Integer.parseInt(ctx.queryParam("id"));
                try (Connection con = DBUtil.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM books WHERE id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    ctx.result("Deleted");
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error deleting book");
            }
        });

        // ================= USER =================

        app.post("/register", ctx -> {
            String user = ctx.formParam("username");

            try (Connection con = DBUtil.getConnection();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO users(name) VALUES(?)")) {
                ps.setString(1, user);
                ps.executeUpdate();
                ctx.result("User registered");
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error registering user");
            }
        });

        app.get("/users", ctx -> {
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
                ctx.contentType("application/json");
                ctx.result(json.toString());
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("[]");
            }
        });

        // ================= ISSUE =================

        app.post("/issue", ctx -> {
            try {
                int bookId = Integer.parseInt(ctx.formParam("bookId"));
                int userId = Integer.parseInt(ctx.formParam("userId"));

                try (Connection con = DBUtil.getConnection()) {
                    // CHECK USER EXISTS
                    try (PreparedStatement checkUser = con.prepareStatement("SELECT id FROM users WHERE id=?")) {
                        checkUser.setInt(1, userId);
                        try (ResultSet userRs = checkUser.executeQuery()) {
                            if (!userRs.next()) {
                                ctx.result("❌ User not found!");
                                return;
                            }
                        }
                    }

                    // CHECK BOOK AVAILABLE
                    try (PreparedStatement checkBook = con
                            .prepareStatement("SELECT id FROM books WHERE id=? AND available=true")) {
                        checkBook.setInt(1, bookId);
                        try (ResultSet bookRs = checkBook.executeQuery()) {
                            if (!bookRs.next()) {
                                ctx.result("❌ Book not available!");
                                return;
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

                    ctx.result("✅ Book issued (Due in 7 days)");
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error issuing book");
            }
        });

        // ================= RETURN =================

        app.post("/return", ctx -> {
            try {
                int bookId = Integer.parseInt(ctx.queryParam("bookId"));

                try (Connection con = DBUtil.getConnection()) {
                    LocalDate due;
                    try (Statement stmt = con.createStatement();
                            ResultSet rs = stmt.executeQuery("SELECT due_date FROM issues WHERE book_id=" + bookId
                                    + " ORDER BY id DESC LIMIT 1")) {
                        if (!rs.next()) {
                            ctx.result("❌ No issue record found!");
                            return;
                        }
                        due = rs.getDate("due_date").toLocalDate();
                    }

                    LocalDate today = LocalDate.now();
                    long daysLate = ChronoUnit.DAYS.between(due, today);
                    int fine = daysLate > 0 ? (int) daysLate * 10 : 0;

                    try (Statement stmt = con.createStatement()) {
                        stmt.executeUpdate("UPDATE books SET available=true WHERE id=" + bookId);
                    }

                    ctx.result("Returned. Fine = ₹" + fine);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error returning book");
            }
        });

        // ================= SEARCH =================

        app.get("/search", ctx -> {
            try {
                String key = ctx.queryParam("key");

                try (Connection con = DBUtil.getConnection();
                        PreparedStatement ps = con
                                .prepareStatement("SELECT * FROM books WHERE title LIKE ? OR author LIKE ?")) {

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
                        ctx.contentType("application/json");
                        ctx.result(json.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("[]");
            }
        });

        System.out.println("✅ Server running at http://localhost:8080");
    }
}