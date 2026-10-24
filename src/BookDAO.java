
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // Add a new book
    public boolean addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books " +
                "(book_id, title, author, total_copies, available_copies) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, book.getBookId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setInt(4, book.getTotalCopies());
            ps.setInt(5, book.getAvailableCopies());

            return ps.executeUpdate() > 0;
        }
    }

    // Get all books
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";

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

    // Find a book by ID
    public Book getBookById(String bookId) throws SQLException {
        String sql = "SELECT * FROM books WHERE book_id = ?";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bookId);

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

    // Search books by title keyword
    public List<Book> searchBooksByTitle(String keyword)
            throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ?";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

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

    // Update available copies using a new connection
    public boolean updateAvailableCopies(String bookId, int copies)
            throws SQLException {
        String sql = "UPDATE books SET available_copies = ? " +
                "WHERE book_id = ?";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, copies);
            ps.setString(2, bookId);

            return ps.executeUpdate() > 0;
        }
    }

    // Update available copies using an existing connection.
    // The service can use this method inside a transaction.
    public boolean updateAvailableCopies(
            Connection con, String bookId, int copies)
            throws SQLException {

        String sql = "UPDATE books SET available_copies = ? " +
                "WHERE book_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, copies);
            ps.setString(2, bookId);

            return ps.executeUpdate() > 0;
        }
    }

    // Total distinct book titles
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

    // Total physical book copies
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

    // Total available copies across library
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

    // Safely delete a book if no active or historical loans prevent it
    public boolean deleteBook(String bookId) throws SQLException {
        // Check if there are active loans
        String checkSql = "SELECT COUNT(*) FROM borrow_records WHERE book_id = ? AND return_date IS NULL";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement psCheck = con.prepareStatement(checkSql)) {
            psCheck.setString(1, bookId);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete book: There are active loans currently checked out for this book.");
                }
            }
        }

        // Check if there are historical borrow records (foreign key constraint)
        String historySql = "SELECT COUNT(*) FROM borrow_records WHERE book_id = ?";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement psHist = con.prepareStatement(historySql)) {
            psHist.setString(1, bookId);
            try (ResultSet rs = psHist.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete book: It has borrowing records in history. To preserve audit logs, books with past loans cannot be removed.");
                }
            }
        }

        String deleteSql = "DELETE FROM books WHERE book_id = ?";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(deleteSql)) {
            ps.setString(1, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    // Update existing book details
    public boolean updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, total_copies = ?, available_copies = ? WHERE book_id = ?";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setInt(3, book.getTotalCopies());
            ps.setInt(4, book.getAvailableCopies());
            ps.setString(5, book.getBookId());
            return ps.executeUpdate() > 0;
        }
    }
}

