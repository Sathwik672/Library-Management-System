package com.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBUtil {

    public static Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");

            Connection con = DriverManager.getConnection("jdbc:sqlite:library.db");

            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR(255), author VARCHAR(255), available BOOLEAN DEFAULT true)");
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(255))");
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS issues (id INTEGER PRIMARY KEY AUTOINCREMENT, book_id INTEGER, user VARCHAR(255), issue_date DATE, due_date DATE)");
            }

            return con;

        } catch (Exception e) {
            System.out.println(" DATABASE CONNECTION FAILED!");
            e.printStackTrace();
            return null;
        }
    }
}