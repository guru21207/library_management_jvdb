import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Book entity.
 * Handles database operations including CRUD, availability updates, and overloaded searches.
 */
public class BookDAO {

    /**
     * Adds a new book to the database with strict validation.
     * Prevents empty values, invalid copy counts, and duplicate Book IDs.
     *
     * @param book The Book object to persist
     * @return true if added successfully, false otherwise
     * @throws IllegalArgumentException if input parameters violate business constraints
     * @throws SQLException if a database error occurs
     */
    public boolean addBook(Book book) throws SQLException {
        if (book == null) {
            throw new IllegalArgumentException("Book details cannot be null.");
        }
        if (book.getBookId() == null || book.getBookId().trim().isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be empty.");
        }
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty.");
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Author name cannot be empty.");
        }
        if (book.getTotalCopies() <= 0) {
            throw new IllegalArgumentException("Total copies must be a positive number greater than zero.");
        }
        if (book.getAvailableCopies() < 0) {
            throw new IllegalArgumentException("Available copies cannot be negative.");
        }
        if (book.getAvailableCopies() > book.getTotalCopies()) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies.");
        }

        // Check for existing duplicate Book ID before insert
        if (searchBook(book.getBookId().trim()) != null) {
            throw new SQLException("A book with ID '" + book.getBookId().trim() + "' already exists in the catalog.");
        }

        String sql = "INSERT INTO books (book_id, title, author, total_copies, available_copies) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, book.getBookId().trim());
            ps.setString(2, book.getTitle().trim());
            ps.setString(3, book.getAuthor().trim());
            ps.setInt(4, book.getTotalCopies());
            ps.setInt(5, book.getAvailableCopies());

            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLException("A book with ID '" + book.getBookId() + "' already exists in the catalog.", e);
        }
    }

    /**
     * Retrieves all books currently stored in the library catalog.
     *
     * @return List of all books ordered by book_id
     * @throws SQLException if database read fails
     */
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY book_id ASC";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                books.add(new Book(
                        rs.getString("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies")
                ));
            }
        }
        return books;
    }

    // =========================================================================
    // METHOD OVERLOADING DEMONSTRATION (Search Operations)
    // =========================================================================

    /**
     * Overload 1: Exact Book ID lookup.
     * Demonstrates method overloading by searching for a single entity by unique identifier.
     *
     * @param bookId Exact unique Book ID (e.g., "B001")
     * @return Book object if found, null otherwise
     * @throws SQLException if database read fails
     */
    public Book searchBook(String bookId) throws SQLException {
        if (bookId == null || bookId.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT * FROM books WHERE book_id = ?";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bookId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                            rs.getString("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("total_copies"),
                            rs.getInt("available_copies")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Alias for searchBook(String bookId) to preserve compatibility with existing callers.
     */
    public Book getBookById(String bookId) throws SQLException {
        return searchBook(bookId);
    }

    /**
     * Overload 2: Keyword search with title/author filter flag.
     * Demonstrates method overloading with signature: searchBooks(String, boolean).
     *
     * @param keyword Substring to search for
     * @param searchByTitle If true, searches title; if false, searches author
     * @return List of matching books
     * @throws SQLException if database query fails
     */
    public List<Book> searchBooks(String keyword, boolean searchByTitle) throws SQLException {
        List<Book> books = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }

        String column = searchByTitle ? "title" : "author";
        String sql = "SELECT * FROM books WHERE " + column + " LIKE ? ORDER BY book_id ASC";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword.trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(
                            rs.getString("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("total_copies"),
                            rs.getInt("available_copies")
                    ));
                }
            }
        }
        return books;
    }

    /**
     * Overload 3: Unified multi-field search across Book ID, Title, and Author.
     * Allows the user to type a Book ID, a title keyword, or an author name into the same search bar.
     *
     * @param query Search query text
     * @return List of matching books matching either ID, title, or author
     * @throws SQLException if database query fails
     */
    public List<Book> searchBooks(String query) throws SQLException {
        List<Book> books = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks();
        }

        String pattern = "%" + query.trim() + "%";
        String sql = "SELECT * FROM books WHERE book_id LIKE ? OR title LIKE ? OR author LIKE ? ORDER BY book_id ASC";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(
                            rs.getString("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("total_copies"),
                            rs.getInt("available_copies")
                    ));
                }
            }
        }
        return books;
    }

    /**
     * Searches books by title keyword.
     * Delegates to the overloaded searchBooks(keyword, true) method.
     */
    public List<Book> searchBooksByTitle(String keyword) throws SQLException {
        return searchBooks(keyword, true);
    }

    /**
     * Searches books by author keyword.
     * Delegates to the overloaded searchBooks(keyword, false) method.
     */
    public List<Book> searchBooksByAuthor(String keyword) throws SQLException {
        return searchBooks(keyword, false);
    }

    // =========================================================================
    // INVENTORY & AVAILABILITY UPDATES
    // =========================================================================

    /**
     * Updates available copies using a standalone new connection.
     */
    public boolean updateAvailableCopies(String bookId, int copies) throws SQLException {
        try (Connection con = Database_connection.getConnection()) {
            return updateAvailableCopies(con, bookId, copies);
        }
    }

    /**
     * Updates available copies inside an active database transaction.
     */
    public boolean updateAvailableCopies(Connection con, String bookId, int copies) throws SQLException {
        String sql = "UPDATE books SET available_copies = ? WHERE book_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, copies);
            ps.setString(2, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Count of distinct book titles in catalog.
     */
    public int getTotalBooksCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Total physical inventory copies across all titles.
     */
    public int getTotalCopiesCount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_copies), 0) FROM books";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Total available (un-borrowed) copies currently on shelves.
     */
    public int getAvailableCopiesCount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(available_copies), 0) FROM books";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Safely deletes a book from the catalog.
     * Prohibits deletion if active borrowing records or historical transaction logs reference this book.
     *
     * @param bookId Book ID to delete
     * @return true if deleted, false if not found
     * @throws SQLException if active or historical loans prevent deletion
     */
    public boolean deleteBook(String bookId) throws SQLException {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be empty.");
        }

        // 1. Check for active unreturned loans
        String checkActiveSql = "SELECT COUNT(*) FROM borrow_records WHERE book_id = ? AND return_date IS NULL";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement psCheck = con.prepareStatement(checkActiveSql)) {
            psCheck.setString(1, bookId.trim());
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete book '" + bookId + "': There are active loans currently checked out for this book.");
                }
            }
        }

        // 2. Check for historical borrow records (referential integrity)
        String historySql = "SELECT COUNT(*) FROM borrow_records WHERE book_id = ?";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement psHist = con.prepareStatement(historySql)) {
            psHist.setString(1, bookId.trim());
            try (ResultSet rs = psHist.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete book '" + bookId + "': Historical borrowing transactions exist for this book. Books with past loans are preserved for audit records.");
                }
            }
        }

        String deleteSql = "DELETE FROM books WHERE book_id = ?";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(deleteSql)) {
            ps.setString(1, bookId.trim());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates an existing book's metadata.
     */
    public boolean updateBook(Book book) throws SQLException {
        if (book == null) throw new IllegalArgumentException("Book cannot be null.");
        String sql = "UPDATE books SET title = ?, author = ?, total_copies = ?, available_copies = ? WHERE book_id = ?";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle().trim());
            ps.setString(2, book.getAuthor().trim());
            ps.setInt(3, book.getTotalCopies());
            ps.setInt(4, book.getAvailableCopies());
            ps.setString(5, book.getBookId().trim());
            return ps.executeUpdate() > 0;
        }
    }
}
