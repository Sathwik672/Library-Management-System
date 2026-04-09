# Library Management System

This project is a web-based library management system designed to manage books, members, and borrowing activities in a simple and efficient way. It helps automate common library tasks such as issuing books, returning them, and keeping track of records.

Demo
Watch the demo video here : https://drive.google.com/file/d/1IZRGRqGce-kPzU56OkGLyZoz1X7QwQPU/view?usp=sharing

---

## Features

* **User Management**
  Add and manage library members easily.

* **Book Management**
  Add, update, and view books available in the library.

* **Issue & Return System**

  * Issue books to members
  * Return issued books
  * Track borrowed books

* **Availability Tracking**
  Keeps track of available and issued books.

* **Data Validation**

  * Prevents issuing unavailable books
  * Ensures valid member and book records

---

## Technology Stack

* **Backend:** Java 21 with Javalin
* **Frontend:** HTML, CSS, JavaScript
* **Database:** MySQL
* **Build Tool:** Maven

---

## Prerequisites

* Java Development Kit (JDK) 21 or higher
* Apache Maven
* MySQL Server
* MySQL Workbench (optional)

---

## Installation

1. Clone the repository to your local system
2. Ensure your MySQL server is running
3. Create a database
4. Execute the provided SQL file to create tables
5. Update database credentials in:
   `src/main/java/com/library/util/DBUtil.java`

---

## Running the Application

Run the following commands in the project root directory:

mvn clean compile

mvn exec:java "-Dexec.mainClass=com.library.ui.LibraryApp"

After running, the application will start (check console for the localhost port).

---

## Project Structure

* `src/main/java` – Backend logic, services, and database operations
* `src/main/resources/static` – Frontend files (HTML, CSS, JS)
* `pom.xml` – Project dependencies and build configuration
* `library.sql` – Database schema and table creation scripts

---

## Final Note

This project demonstrates the basic working of a library management system, including book handling, user management, and transaction tracking. It is useful for understanding full-stack development using Java and MySQL.
