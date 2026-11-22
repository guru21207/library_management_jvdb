import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service Layer for BookNest Library Management System.
 *
 * Implements core business workflows:
 * 1. Book circulation (Issuance with max 3-loan restriction and concurrency row-locking)
 * 2. Book returns and standardized fine calculation (7 free days, ₹2/day overdue)
 * 3. Static counter tracking total lifetime issued books
 * 4. Overloaded book search operations demonstrating Java OOP polymorphism
 */
public class LibraryService {

    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();

    // =========================================================================
    // STATIC MEMBER DEMONSTRATION (Requirement E)
    // =========================================================================
    // A Java static field belongs to the class itself rather than individual instances.
    // Here, totalLifetimeIssued tracks the aggregate count of successful borrowing
    // transactions processed by the system.
    //
    // To ensure consistency across application restarts, it synchronizes with the
    // persistent MySQL database upon initialization, and increments strictly after
    // an issue transaction is committed successfully.
    // =========================================================================
    private static int totalLifetimeIssued = 0;
    private static boolean counterInitialized = false;

    /**
     * Gets the total lifetime number of books issued.
     * Thread-safe access to static counter.
     *
     * @return Total lifetime books issued
     */
    public static synchronized int getTotalLifetimeIssued() {
        if (!counterInitialized) {
            syncLifetimeIssuedCounter();
        }
        return totalLifetimeIssued;
    }

    /**
     * Synchronizes the static counter with the total records in the database.
     * Called at application startup or when rebuilding metrics.
     */
    public static synchronized void syncLifetimeIssuedCounter() {
        try (Connection con = Database_connection.getConnection()) {
            syncLifetimeIssuedCounter(con);
        } catch (SQLException e) {
            System.err.println("Notice: Could not synchronize lifetime issued counter: " + e.getMessage());
        }
    }

    /**
     * Synchronizes the static counter using an existing database connection.
     */
    public static synchronized void syncLifetimeIssuedCounter(Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrow_records";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                totalLifetimeIssued = rs.getInt(1);
                counterInitialized = true;
            }
        }
    }

    /**
     * Increments the static counter by 1.
     * Strictly called ONLY after an issue transaction has committed successfully.
     * Never called if transaction fails, rolls back, or is rejected.
     */
    private static synchronized void incrementLifetimeIssued() {
        totalLifetimeIssued++;
    }

    /**
     * Resets or sets the static counter directly (useful for isolated unit testing).
     */
    public static synchronized void setStaticCounterForTesting(int value) {
        totalLifetimeIssued = value;
        counterInitialized = true;
    }

    // =========================================================================
    // FINE CALCULATION LOGIC (Requirement D - Shared Single Calculation Method)
    // =========================================================================
    // Circulation Policy:
    // - 7 Days Free grace period (inclusive calculation: day 1 to day 7 has ₹0.00 fine)
    // - ₹2.00 per day charged for each overdue day beyond day 7
    // Formula: fine = max(0, daysKept - 7) * 2
    // =========================================================================

    /**
     * Computes the number of days a book was kept using the project's established
     * inclusive-day convention (Issue date is counted as day 1).
     *
     * @param issueDate Date the book was borrowed
     * @param returnDate Date the book is returned
     * @return Total inclusive days kept (e.g., same day = 1 day)
     * @throws IllegalArgumentException if returnDate is before issueDate
     */
    public static int calculateDaysKept(LocalDate issueDate, LocalDate returnDate) {
        if (issueDate == null || returnDate == null) {
            throw new IllegalArgumentException("Issue date and return date cannot be null.");
        }
        long daysBetween = ChronoUnit.DAYS.between(issueDate, returnDate);
        if (daysBetween < 0) {
            throw new IllegalArgumentException("Return date (" + returnDate + ") cannot be before issue date (" + issueDate + ").");
        }
        return Math.toIntExact(daysBetween + 1);
    }

    /**
     * Standard fine calculation formula:
     * fine = max(0, daysKept - 7) * 2.0
     *
     * Examples:
     * - daysKept = 6  -> ₹0.00
     * - daysKept = 7  -> ₹0.00
     * - daysKept = 8  -> (8 - 7) * ₹2 = ₹2.00
     * - daysKept = 12 -> (12 - 7) * ₹2 = ₹10.00
     *
     * @param daysKept Number of inclusive days the book was held
     * @return Fine amount in Indian Rupees (₹)
     */
    public static double calculateFine(int daysKept) {
        if (daysKept < 0) {
            throw new IllegalArgumentException("Days kept cannot be negative.");
        }
        return Math.max(0, daysKept - 7) * 2.0;
    }

    // =========================================================================
    // METHOD OVERLOADING DEMONSTRATION (Requirement A & F)
    // =========================================================================

    /**
     * Overload 1: Unified multi-field book search.
     * Matches query against Book ID, Title, or Author.
     *
     * @param query Search query text
     * @return List of matching books
     * @throws SQLException if database error occurs
     */
    public List<Book> searchBooks(String query) throws SQLException {
        return bookDAO.searchBooks(query);
    }

    /**
     * Overload 2: Keyword search with specific title or author filter.
     * Demonstrates method overloading with signature: searchBooks(String, boolean).
     *
     * @param keyword Substring to find
     * @param searchByTitle true to search titles; false to search author names
     * @return List of matching books
     * @throws SQLException if database error occurs
     */
    public List<Book> searchBooks(String keyword, boolean searchByTitle) throws SQLException {
        return bookDAO.searchBooks(keyword, searchByTitle);
    }

    /**
     * Overload 3: Exact Book ID lookup returning a single Book object.
     * Demonstrates method overloading with signature: searchBook(String).
     *
     * @param bookId Unique Book ID
     * @return Book object if found, null otherwise
     * @throws SQLException if database error occurs
     */
    public Book searchBook(String bookId) throws SQLException {
        return bookDAO.searchBook(bookId);
    }

    // =========================================================================
    // ISSUE BOOK WORKFLOW (Requirement C)
    // =========================================================================
    /**
     * Issues a book to a member with full transaction management:
     * 1. Validates Member existence
     * 2. Enforces maximum 3 active loans per member rule
     * 3. Locks Book row for update to prevent race conditions on last available copy
     * 4. Checks available copy count (> 0)
     * 5. Decrements book available copy count by exactly 1
     * 6. Creates a new borrowing record
     * 7. Commits transaction atomically (or rolls back on any error)
     * 8. Increments the static lifetime issued counter strictly on success
     *
     * @param memberId Target member ID
     * @param bookId Target book ID
     * @return Human-readable status message
     * @throws SQLException if an unrecoverable database error occurs
     */
    public String issueBook(String memberId, String bookId) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            return "Member ID cannot be empty.";
        }
        if (bookId == null || bookId.trim().isEmpty()) {
            return "Book ID cannot be empty.";
        }

        memberId = memberId.trim();
        bookId = bookId.trim();

        try (Connection con = Database_connection.getConnection()) {
            con.setAutoCommit(false); // Begin database transaction

            try {
                // 1. Check member exists using transactional connection
                Member member = memberDAO.getMemberById(con, memberId);
                if (member == null) {
                    con.rollback();
                    return "Member '" + memberId + "' not found in directory.";
                }

                // 2. Check active loans (Enforce: Member must have fewer than 3 active loans)
                String countActiveSql = "SELECT COUNT(*) FROM borrow_records " +
                                        "WHERE member_id = ? AND return_date IS NULL";
                try (PreparedStatement ps = con.prepareStatement(countActiveSql)) {
                    ps.setString(1, memberId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        int activeLoans = rs.getInt(1);
                        if (activeLoans >= 3) {
                            con.rollback();
                            return "Member already has 3 active loans. Maximum borrowing quota reached (Max 3).";
                        }
                    }
                }

                // 3. Concurrency-Safe Row Locking: Select book with FOR UPDATE
                // Prevents two simultaneous threads/users from checking out the last remaining copy.
                Book book;
                String bookSql = "SELECT * FROM books WHERE book_id = ? FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(bookSql)) {
                    ps.setString(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return "Book '" + bookId + "' not found in catalog.";
                        }
                        book = new Book(
                                rs.getString("book_id"),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getInt("total_copies"),
                                rs.getInt("available_copies")
                        );
                    }
                }

                // 4. Verify book copy availability
                if (book.getAvailableCopies() <= 0) {
                    con.rollback();
                    return "No copies available for '" + book.getTitle() + "'. Book is currently out of stock.";
                }

                // 5. Create new borrowing record
                BorrowRecord record = new BorrowRecord(
                        0, memberId, bookId,
                        LocalDate.now(), null, 0, 0.0
                );

                if (!borrowDAO.addBorrowRecord(con, record)) {
                    con.rollback();
                    return "Could not create borrowing record in database.";
                }

                // 6. Reduce book's available copies by exactly one
                if (!bookDAO.updateAvailableCopies(con, bookId, book.getAvailableCopies() - 1)) {
                    con.rollback();
                    return "Could not update book availability count.";
                }

                // 7. Commit transaction
                con.commit();

                // 8. Increment static lifetime issued counter strictly AFTER successful commit
                incrementLifetimeIssued();

                return "Book issued successfully! '" + book.getTitle() + "' loaned to " + member.getName() + ".";

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // =========================================================================
    // RETURN BOOK & FINE WORKFLOW (Requirement D)
    // =========================================================================
    /**
     * Processes book return with transaction integrity:
     * 1. Locks the borrowing record row with FOR UPDATE
     * 2. Rejects nonexistent records or records that are already returned
     * 3. Calculates inclusive days kept (issue date is day 1)
     * 4. Calculates overdue fine using shared calculateFine() formula
     * 5. Updates borrowing record with return_date, days_kept, and fine
     * 6. Restores book's available copies count (+1)
     * 7. Commits transaction atomically
     *
     * @param recordId Borrowing record ID to return
     * @return Human-readable status message with formatted fine
     * @throws SQLException if a database error occurs
     */
    public String returnBook(int recordId) throws SQLException {
        if (recordId <= 0) {
            return "Invalid borrowing record ID: " + recordId;
        }

        try (Connection con = Database_connection.getConnection()) {
            con.setAutoCommit(false); // Begin transaction

            try {
                // 1. Lock the borrowing record for update
                String recordSql = "SELECT * FROM borrow_records WHERE record_id = ? FOR UPDATE";
                String bookId;
                LocalDate issueDate;
                LocalDate existingReturnDate;

                try (PreparedStatement ps = con.prepareStatement(recordSql)) {
                    ps.setInt(1, recordId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return "Borrowing record #" + recordId + " not found.";
                        }

                        bookId = rs.getString("book_id");
                        Date issueSqlDate = rs.getDate("issue_date");
                        Date returnSqlDate = rs.getDate("return_date");

                        issueDate = issueSqlDate == null ? null : issueSqlDate.toLocalDate();
                        existingReturnDate = returnSqlDate == null ? null : returnSqlDate.toLocalDate();
                    }
                }

                // 2. Reject if already returned
                if (existingReturnDate != null) {
                    con.rollback();
                    return "This book has already been returned on " + existingReturnDate + ".";
                }

                if (issueDate == null) {
                    con.rollback();
                    return "Issue date is missing for borrowing record #" + recordId + ".";
                }

                // 3. Lock Book row for update
                Book book;
                String bookSql = "SELECT * FROM books WHERE book_id = ? FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(bookSql)) {
                    ps.setString(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return "Referenced book '" + bookId + "' not found in catalog.";
                        }
                        book = new Book(
                                rs.getString("book_id"),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getInt("total_copies"),
                                rs.getInt("available_copies")
                        );
                    }
                }

                // 4. Calculate inclusive days kept and fine
                LocalDate returnDate = LocalDate.now();
                int daysKept;
                try {
                    daysKept = calculateDaysKept(issueDate, returnDate);
                } catch (IllegalArgumentException ex) {
                    con.rollback();
                    return ex.getMessage();
                }

                double fine = calculateFine(daysKept);

                // 5. Update borrowing record
                if (!borrowDAO.returnBook(con, recordId, returnDate, daysKept, fine)) {
                    con.rollback();
                    return "Could not update borrowing record #" + recordId + ".";
                }

                // 6. Restore available copy count (+1)
                if (book.getAvailableCopies() >= book.getTotalCopies()) {
                    con.rollback();
                    return "Available copies are already at total copies limit (" + book.getTotalCopies() + ").";
                }

                if (!bookDAO.updateAvailableCopies(con, bookId, book.getAvailableCopies() + 1)) {
                    con.rollback();
                    return "Could not restock book availability.";
                }

                // 7. Commit transaction
                con.commit();

                return "Book returned successfully! Title: '" + book.getTitle() +
                        "', Days Kept: " + daysKept +
                        " (Grace: 7 days), Fine: ₹" + String.format("%.2f", fine);

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
