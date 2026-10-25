# BookNest Entity-Relationship (ER) Diagram & Schema Documentation

This document describes the database design, entity specifications, integrity constraints, and relational cardinalities for the **BookNest Library Management System** MySQL database (`booknest`).

---

## 📊 Relational Entity-Relationship Diagram

```mermaid
erDiagram
    MEMBERS ||--o{ BORROW_RECORDS : "places / borrows (1 to 0..*)"
    BOOKS ||--o{ BORROW_RECORDS : "circulates in (1 to 0..*)"

    MEMBERS {
        varchar(20) member_id PK "Unique Member Identifier"
        varchar(100) name "Full Patron Name (NOT NULL)"
    }

    BOOKS {
        varchar(20) book_id PK "Unique Book Identifier"
        varchar(150) title "Book Title (NOT NULL)"
        varchar(100) author "Author Name (NOT NULL)"
        int total_copies "Total Physical Copies (CHECK >= 0)"
        int available_copies "Available Copies (CHECK >= 0, <= total_copies)"
    }

    BORROW_RECORDS {
        int record_id PK "Auto Increment Primary Key"
        varchar(20) member_id FK "References MEMBERS.member_id"
        varchar(20) book_id FK "References BOOKS.book_id"
        date issue_date "Date Issued (NOT NULL)"
        date return_date "Date Returned (NULL when actively borrowed)"
        int days_kept "Days Held by Member (DEFAULT 0, CHECK >= 0)"
        decimal(10,2) fine "Overdue Fine in INR (DEFAULT 0.00, CHECK >= 0)"
    }
```

---

## 🗄️ Database Entities & Field Specifications

### 1. `books` Entity
Stores bibliographic details and real-time physical inventory counts for every library title.

| Column | Data Type | Nullable | Key | Constraints & Defaults | Description |
| :--- | :--- | :---: | :---: | :--- | :--- |
| `book_id` | `VARCHAR(20)` | No | **PK** | Primary Key | Unique alphanumeric book catalog code (e.g., `B001`, `B002`). |
| `title` | `VARCHAR(150)` | No | — | `NOT NULL` | Full title of the publication. |
| `author` | `VARCHAR(100)` | No | — | `NOT NULL` | Author(s) or creator(s) of the book. |
| `total_copies` | `INT` | No | — | `CHECK (total_copies >= 0)` | Total physical copies owned by the library. Must be > 0 on creation. |
| `available_copies`| `INT` | No | — | `CHECK (available_copies >= 0)`<br/>`CHECK (available_copies <= total_copies)` | Current copies remaining on shelf to borrow. Automatically decremented on issue and incremented on return. |

### 2. `members` Entity
Stores registered patron records authorized to borrow materials.

| Column | Data Type | Nullable | Key | Constraints & Defaults | Description |
| :--- | :--- | :---: | :---: | :--- | :--- |
| `member_id` | `VARCHAR(20)` | No | **PK** | Primary Key | Unique patron card identifier (e.g., `M001`, `M002`). |
| `name` | `VARCHAR(100)` | No | — | `NOT NULL` | Patron's full legal name. |

### 3. `borrow_records` Entity (Associative / Junction Entity)
Connects `members` and `books` to track circulation transactions over time. Maintains both active and historical loans.

| Column | Data Type | Nullable | Key | Constraints & Defaults | Description |
| :--- | :--- | :---: | :---: | :--- | :--- |
| `record_id` | `INT` | No | **PK** | `AUTO_INCREMENT`, Primary Key | Unique serial identifier for each circulation transaction. |
| `member_id` | `VARCHAR(20)` | No | **FK** | Foreign Key -> `members(member_id)` | Identifies the borrowing patron. Referential integrity enforced. |
| `book_id` | `VARCHAR(20)` | No | **FK** | Foreign Key -> `books(book_id)` | Identifies the circulated book copy. Referential integrity enforced. |
| `issue_date` | `DATE` | No | — | `NOT NULL` | Date on which the book was checked out. |
| `return_date` | `DATE` | **Yes** | — | `NULL` for active loans | Date returned. `NULL` indicates an unreturned, currently active loan. |
| `days_kept` | `INT` | No | — | `DEFAULT 0`, `CHECK (days_kept >= 0)` | Number of elapsed days held (inclusive convention: day 1 to day N). |
| `fine` | `DECIMAL(10,2)` | No | — | `DEFAULT 0.00`, `CHECK (fine >= 0)` | Overdue fine assessed in Indian Rupees (₹). |

---

## 🔗 Relationships and Cardinality

### 1. `MEMBERS` (1) ─── (0..*) `BORROW_RECORDS`
- **Notation:** Crow's foot (`||--o{`)
- **Semantics:**
  - One member can borrow zero books (e.g., a newly registered patron who hasn't checked out any book yet) or multiple books over their lifetime (`0..*`).
  - Each individual borrowing transaction record must be associated with **exactly one** registered member (`1`).
- **Enforcement:** Foreign key constraint `fk_borrow_member` (`FOREIGN KEY (member_id) REFERENCES members(member_id)`).

### 2. `BOOKS` (1) ─── (0..*) `BORROW_RECORDS`
- **Notation:** Crow's foot (`||--o{`)
- **Semantics:**
  - One book title can appear in zero borrowing records (never checked out) or many borrowing records over time (`0..*`).
  - Each borrowing record refers to **exactly one** book title (`1`).
- **Enforcement:** Foreign key constraint `fk_borrow_book` (`FOREIGN KEY (book_id) REFERENCES books(book_id)`).

### 3. Associative Entity Role of `BORROW_RECORDS`
Conceptually, Members and Books have a **Many-to-Many (M:N)** relationship (a member can borrow multiple books; a book title can be borrowed by multiple members over time).

Because an M:N relationship cannot be directly represented in relational normalization without redundancy, **`BORROW_RECORDS` acts as the associative entity** (also known as a junction table or transaction table). In addition to resolving the M:N relationship, it stores essential temporal transaction attributes:
- When the checkout happened (`issue_date`)
- When the book was returned (`return_date`)
- How long it was held (`days_kept`)
- Any overdue penalty incurred (`fine`)

---

## ⚖️ Business Rules & Database Integrity Constraints

1. **Active vs. Completed Loan Distinction:**
   - An **active loan** is defined by `return_date IS NULL`.
   - A **completed return** is defined by `return_date IS NOT NULL`.
2. **Quota Constraint (Maximum 3 Active Loans):**
   - A member cannot have more than 3 active loans simultaneously (`COUNT(*) WHERE member_id = ? AND return_date IS NULL < 3`).
   - Enforced transactionally in `LibraryService.issueBook()`.
3. **Inventory Conservation:**
   - Available copies cannot drop below 0: `chk_available_copies CHECK (available_copies >= 0)`.
   - Available copies cannot exceed total copies: `chk_available_limit CHECK (available_copies <= total_copies)`.
   - On issue: `available_copies = available_copies - 1`.
   - On return: `available_copies = available_copies + 1`.
4. **Fine Calculation Policy:**
   - **Grace Period:** 7 free inclusive days.
   - **Overdue Penalty:** ₹2.00 per day beyond 7 days.
   - **Formula:** `fine = max(0, daysKept - 7) * 2.0`.
   - **Non-negative Fine Constraint:** `chk_fine CHECK (fine >= 0)`.
5. **Referential Integrity & Safe Deletion:**
   - Foreign key constraints prevent deleting a book or member if active borrowing transactions exist.
   - Historical audit preservation ensures records with past transactions remain intact for accounting.
