# BookNest – Library Management System (Phase 1 & Phase 2)

A robust Library Management System developed using **Java**, **JDBC**, **MySQL 8**, and **Java Swing**. BookNest provides an automated solution for managing books, members, and book borrowing/returning transactions with fine calculation, real-time inventory tracking, and a modern desktop graphical user interface.

---

## 📌 Project Overview

- **Project Name:** BookNest Library Management System
- **Phase:** Phase 1 (Core Logic & Console Interface) & Phase 2 (Modern Java Swing Desktop GUI)
- **Technology Stack:** Java SE, Java Swing (Modern Custom Slate Theme), JDBC, MySQL 8
- **IDE & Platform:** Visual Studio Code / Windows PowerShell
- **Architecture:** Layered Architecture (Model-DAO-Service-GUI/Controller)

---

## 🏛️ Architecture & Design Pattern

BookNest follows a clean, modular architecture adhering to separation of concerns:

```
+-------------------------------------------------------------+
|                     Presentation Layer                      |
|                         (Main.java)                         |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                        Service Layer                        |
|                    (LibraryService.java)                    |
|    - Business Rules, Borrow Validation, Fine Calculation   |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                   Data Access Layer (DAO)                   |
|          (BookDAO, MemberDAO, BorrowDAO)                    |
|    - Prepared Statements, SQL Queries, CRUD Operations      |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                      Database Layer                         |
|         (Database_connection.java -> MySQL 8.0)             |
+-------------------------------------------------------------+
```

### Component Roles:
1. **Model Layer (`Book.java`, `Member.java`, `BorrowRecord.java`):** Encapsulates core business entities and their state.
2. **Data Access Object (DAO) Layer (`BookDAO.java`, `MemberDAO.java`, `BorrowDAO.java`):** Handles all low-level SQL database transactions using parameterized `PreparedStatement` to ensure security against SQL injection.
3. **Service Layer (`LibraryService.java`):** Implements business workflows such as book issuance verification, inventory deduction, return processing, days kept calculation, and fine assessment.
4. **Presentation Layer (`Main.java`):** Interactive console interface with robust input validation and menu navigation.
5. **Database Connection (`Database_connection.java`):** Centralized JDBC connection management using `DriverManager`.

---

## ✨ Features

- **Book Management:**
  - Add new books with ID, title, author, and initial total copy count.
  - View all registered books with total and available copy counts.
  - Search books by title or keyword.
- **Member Management:**
  - Register new library members with unique member IDs.
  - View all registered members.
- **Circulation & Transactions:**
  - **Issue Book:** Verifies book availability and member existence; decrements available copies upon issuance.
  - **Return Book:** Computes elapsed days, applies fine if returned after the due date, and increments available copies.
  - **Borrowing Records:** View historical and active borrowing logs.

---

## 📂 Project Structure

```text
library_management_jvdb/
│
├── .gitignore                  # Git ignore rules for compiled classes & local configs
├── .vscode/
│   └── settings.json           # VS Code Java classpath & source path settings
│
├── database/
│   └── booknest.sql            # Database schema, tables, and integrity constraints
│
├── lib/
│   └── mysql-connector-j-26.7.0.jar  # MySQL JDBC Driver library
│
├── out/                        # Compiled bytecode (.class files)
│   ├── Book.class
│   ├── BookDAO.class
│   ├── BorrowDAO.class
│   ├── BorrowRecord.class
│   ├── Database_connection.class
│   ├── LibraryService.class
│   ├── Main.class
│   ├── Member.class
│   ├── MemberDAO.class
│   ├── TestBookDAO.class
│   ├── TestBorrowDAO.class
│   ├── TestConnection.class
│   ├── TestLibraryService.class
│   ├── TestMemberDAO.class
│   └── TestReturnBook.class
│
├── src/                        # Production Java source files
│   ├── Book.java               # Book entity model
│   ├── BookDAO.java            # DAO for book CRUD operations
│   ├── BorrowDAO.java          # DAO for borrowing transactions
│   ├── BorrowRecord.java       # Borrowing record entity model
│   ├── Database_connection.java# Central JDBC connection utility
│   ├── LibraryService.java     # Business logic & transaction service
│   ├── Main.java               # Main entry point & console UI
│   ├── Member.java             # Member entity model
│   └── MemberDAO.java          # DAO for member CRUD operations
│
├── test/                       # Standalone integration test programs
│   ├── TestBookDAO.java        # Validates BookDAO operations
│   ├── TestBorrowDAO.java      # Validates BorrowDAO operations
│   ├── TestConnection.java     # Validates MySQL database connectivity
│   ├── TestLibraryService.java # Validates business service routines
│   ├── TestMemberDAO.java      # Validates MemberDAO operations
│   └── TestReturnBook.java     # Validates return flow and fine logic
│
└── README.md                   # Project documentation
```

---

## ⚙️ Prerequisites

1. **Java Development Kit (JDK):**
   - JDK 17 or higher (tested with JDK 26).
   - Ensure `java` and `javac` are available in your system `PATH`.
2. **MySQL Server:**
   - MySQL Server 8.0 or higher running on `localhost:3306`.
3. **MySQL Connector/J:**
   - Bundled inside the `lib/` directory (`mysql-connector-j-26.7.0.jar`).
4. **Visual Studio Code (Optional):**
   - Recommended extension: *Extension Pack for Java* (by Microsoft).

---

## 🗄️ Database Setup

1. Open your terminal or MySQL Command Line Client.
2. Log in to MySQL as `root`:
   ```powershell
   mysql -u root -p
   ```
3. Execute the SQL schema script provided in `database/booknest.sql`:
   ```sql
   SOURCE C:/Users/manim/Downloads/PROJECTS/BN/library_management_jvdb/database/booknest.sql;
   ```
   *(Or import via PowerShell: `Get-Content database\booknest.sql | mysql -u root -p`)*

### Database Schema Overview:
- **`books`:**
  - `book_id` (VARCHAR(20), Primary Key)
  - `title` (VARCHAR(150), Not Null)
  - `author` (VARCHAR(100), Not Null)
  - `total_copies` (INT, CHECK >= 0)
  - `available_copies` (INT, CHECK >= 0, CHECK <= total_copies)
- **`members`:**
  - `member_id` (VARCHAR(20), Primary Key)
  - `name` (VARCHAR(100), Not Null)
- **`borrow_records`:**
  - `record_id` (INT, Auto Increment Primary Key)
  - `member_id` (VARCHAR(20), Foreign Key -> `members.member_id`)
  - `book_id` (VARCHAR(20), Foreign Key -> `books.book_id`)
  - `issue_date` (DATE, Not Null)
  - `return_date` (DATE, Nullable)
  - `days_kept` (INT, Default 0)
  - `fine` (DECIMAL(10,2), Default 0.00)

---

## 🔑 Database Configuration

Verify your MySQL credentials inside [`src/Database_connection.java`](file:///c:/Users/manim/Downloads/PROJECTS/BN/library_management_jvdb/src/Database_connection.java):

```java
private static final String URL = "jdbc:mysql://localhost:3306/booknest";
private static final String USERNAME = "root";
private static final String PASSWORD = "your_mysql_password";
```

---

## 🚀 How to Compile and Run

Always run these commands from the **project root directory** (`library_management_jvdb`):

### 1. Compile Production Code
Compile all source files from `src/` and output `.class` files into `out/`:
```powershell
javac -cp ".;lib\*" -d out src\*.java
```

### 2. Run the Application

#### Launch Modern Desktop GUI (Default):
```powershell
java -cp "out;lib\*" Main
```
*(Or directly via `java -cp "out;lib\*" BookNestGUI`)*

#### Launch Classic Console Interface:
```powershell
java -cp "out;lib\*" Main --console
```

### 3. Compile Standalone Tests (Optional)
To compile test scripts into `out/`:
```powershell
javac -cp ".;out;lib\*" -d out test\*.java
```

### 4. Run Connectivity Test (Safe / Non-mutating)
Verify database connection without modifying any database records:
```powershell
java -cp "out;lib\*" TestConnection
```

---

## 🎨 Phase 2 – Modern Graphical User Interface (GUI)

BookNest now features a desktop GUI built with **Java Swing** and custom components:

- **📊 Live Dashboard:** Real-time stat cards (Total Titles, Shelf Copies, Active Members, Active Borrowings, Total Fines Collected) and quick action shortcuts.
- **📚 Book Inventory:** Searchable catalog with instant real-time filtering, stock status pills (Available, Low Stock, Out of Stock), modal dialog for registering books, and safe deletion.
- **👥 Member Directory:** Searchable directory with real-time tracking of active loans per member (enforcing the 3-book limit) and membership status badges.
- **🔄 Issue Book Circulation:** Intuitive book checkout workflow with member and book dropdowns, active loan limit validation, and live member borrowing previews.
- **📥 Return & Fine Assessment:** Active loan tracker with instant 1-click return, automatic overdue calculations (7 days grace period, ₹2.00/day fine), and instant inventory restocking.
- **📜 Transaction Audit History:** Filterable circulation records (All, Active Loans, Returned) with member names, book titles, dates, days kept, and fine amounts.

