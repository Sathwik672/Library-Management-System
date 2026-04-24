package com.library.ui;

import com.library.dao.LibraryDao;

import io.javalin.Javalin;

public class LibraryApp {

    public static void main(String[] args) {

        LibraryDao dao = new LibraryDao();

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(8080);

        app.get("/", ctx -> ctx.redirect("/index.html"));

        app.post("/add-book", ctx -> {
            try {
                String title = ctx.formParam("title");
                String author = ctx.formParam("author");
                dao.addBook(title, author);
                ctx.result("Book added");
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error: " + e.getMessage());
            }
        });

        app.get("/books", ctx -> {
            try {
                ctx.json(dao.getBooks());
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("[]");
            }
        });

        app.post("/delete-book", ctx -> {
            try {
                int id = Integer.parseInt(ctx.queryParam("id"));
                dao.deleteBook(id);
                ctx.result("Deleted");
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error deleting book");
            }
        });

        app.post("/register", ctx -> {
            try {
                String user = ctx.formParam("username");
                dao.registerUser(user);
                ctx.result("User registered");
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error registering user");
            }
        });

        app.get("/users", ctx -> {
            try {
                String json = dao.getUsers();
                ctx.contentType("application/json");
                ctx.result(json);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("[]");
            }
        });

        app.post("/issue", ctx -> {
            try {
                int bookId = Integer.parseInt(ctx.formParam("bookId"));
                int userId = Integer.parseInt(ctx.formParam("userId"));
                String result = dao.issueBook(bookId, userId);
                ctx.result(result);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error issuing book");
            }
        });

        app.post("/return", ctx -> {
            try {
                int bookId = Integer.parseInt(ctx.queryParam("bookId"));
                String result = dao.returnBook(bookId);
                ctx.result(result);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error returning book");
            }
        });

        app.get("/search", ctx -> {
            try {
                String key = ctx.queryParam("key");
                ctx.json(dao.searchBooks(key));
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("[]");
            }
        });

        System.out.println(" Server running at http://localhost:8080");
    }
}