import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for BorrowRecord entity.
 * Manages circulation records, return processing, active loans, and fine aggregations.
 */
public class BorrowDAO {

    /**
     * Creates a new borrowing record using a new database connection.
     */
    public boolean addBorrowRecord(BorrowRecord record) throws SQLException {
        try (Connection con = Database_connection.getConnection()) {
            return addBorrowRecord(con, record);
        }
    }

    /**
     * Creates a new borrowing record using an existing transactional connection.
     */
    public boolean addBorrowRecord(Connection con, BorrowRecord record) throws SQLException {
        String sql = "INSERT INTO borrow_records " +
                "(member_id, book_id, issue_date, return_date, days_kept, fine) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, record.getMemberId().trim());
            ps.setString(2, record.getBookId().trim());

            if (record.getIssueDate() == null) {
                ps.setNull(3, java.sql.Types.DATE);
            } else {
                ps.setDate(3, Date.valueOf(record.getIssueDate()));
            }

            if (record.getReturnDate() == null) {
                ps.setNull(4, java.sql.Types.DATE);
            } else {
                ps.setDate(4, Date.valueOf(record.getReturnDate()));
            }

            if (record.getDaysKept() == null) {
                ps.setInt(5, 0);
            } else {
                ps.setInt(5, record.getDaysKept());
            }

            ps.setDouble(6, record.getFine());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all borrowing records ordered by record_id.
     */
    public List<BorrowRecord> getAllBorrowRecords() throws SQLException {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM borrow_records ORDER BY record_id ASC";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date issueSqlDate = rs.getDate("issue_date");
                Date returnSqlDate = rs.getDate("return_date");

                int days = rs.getInt("days_kept");
                Integer daysKept = rs.wasNull() ? null : days;

                records.add(new BorrowRecord(
                        rs.getInt("record_id"),
                        rs.getString("member_id"),
                        rs.getString("book_id"),
                        issueSqlDate == null ? null : issueSqlDate.toLocalDate(),
                        returnSqlDate == null ? null : returnSqlDate.toLocalDate(),
                        daysKept,
                        rs.getDouble("fine")
                ));
            }
        }
        return records;
    }

    /**
     * Updates an active borrowing record on return using a new connection.
     */
    public boolean returnBook(int recordId, LocalDate returnDate, int daysKept, double fine) throws SQLException {
        try (Connection con = Database_connection.getConnection()) {
            return returnBook(con, recordId, returnDate, daysKept, fine);
        }
    }

    /**
     * Updates an active borrowing record on return using an existing transactional connection.
     */
    public boolean returnBook(Connection con, int recordId, LocalDate returnDate, int daysKept, double fine) throws SQLException {
        String sql = "UPDATE borrow_records " +
                "SET return_date = ?, days_kept = ?, fine = ? " +
                "WHERE record_id = ? AND return_date IS NULL";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(returnDate));
            ps.setInt(2, daysKept);
            ps.setDouble(3, fine);
            ps.setInt(4, recordId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all borrowing records joined with member name and book title for rich presentation.
     */
    public List<BorrowRecordDetail> getDetailedBorrowRecords() throws SQLException {
        List<BorrowRecordDetail> list = new ArrayList<>();
        String sql = "SELECT br.record_id, br.member_id, m.name AS member_name, " +
                     "       br.book_id, b.title AS book_title, " +
                     "       br.issue_date, br.return_date, br.days_kept, br.fine " +
                     "FROM borrow_records br " +
                     "LEFT JOIN members m ON br.member_id = m.member_id " +
                     "LEFT JOIN books b ON br.book_id = b.book_id " +
                     "ORDER BY br.record_id DESC";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date issueSql = rs.getDate("issue_date");
                Date returnSql = rs.getDate("return_date");
                int days = rs.getInt("days_kept");
                Integer daysKept = rs.wasNull() ? null : days;

                list.add(new BorrowRecordDetail(
                        rs.getInt("record_id"),
                        rs.getString("member_id"),
                        rs.getString("member_name"),
                        rs.getString("book_id"),
                        rs.getString("book_title"),
                        issueSql == null ? null : issueSql.toLocalDate(),
                        returnSql == null ? null : returnSql.toLocalDate(),
                        daysKept,
                        rs.getDouble("fine")
                ));
            }
        }
        return list;
    }

    /**
     * Retrieves only currently active (unreturned) borrowing records joined with details.
     */
    public List<BorrowRecordDetail> getActiveDetailedBorrowRecords() throws SQLException {
        List<BorrowRecordDetail> list = new ArrayList<>();
        String sql = "SELECT br.record_id, br.member_id, m.name AS member_name, " +
                     "       br.book_id, b.title AS book_title, " +
                     "       br.issue_date, br.return_date, br.days_kept, br.fine " +
                     "FROM borrow_records br " +
                     "LEFT JOIN members m ON br.member_id = m.member_id " +
                     "LEFT JOIN books b ON br.book_id = b.book_id " +
                     "WHERE br.return_date IS NULL " +
                     "ORDER BY br.record_id DESC";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date issueSql = rs.getDate("issue_date");
                int days = rs.getInt("days_kept");
                Integer daysKept = rs.wasNull() ? null : days;

                list.add(new BorrowRecordDetail(
                        rs.getInt("record_id"),
                        rs.getString("member_id"),
                        rs.getString("member_name"),
                        rs.getString("book_id"),
                        rs.getString("book_title"),
                        issueSql == null ? null : issueSql.toLocalDate(),
                        null,
                        daysKept,
                        rs.getDouble("fine")
                ));
            }
        }
        return list;
    }

    /**
     * Returns the count of currently outstanding active loans (return_date IS NULL).
     */
    public int getActiveLoansCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE return_date IS NULL";
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
     * Returns total fine revenue collected from returned books.
     */
    public double getTotalFinesCollected() throws SQLException {
        String sql = "SELECT COALESCE(SUM(fine), 0.0) FROM borrow_records WHERE return_date IS NOT NULL";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    /**
     * Returns count of active loans for a specific member.
     */
    public int getActiveLoanCountForMember(String memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE member_id = ? AND return_date IS NULL";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Returns the lifetime count of all borrowing transactions recorded in the database.
     */
    public int getTotalLifetimeBorrowCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrow_records";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
