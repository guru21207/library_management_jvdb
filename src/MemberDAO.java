import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Member entity.
 * Handles database operations including member registration, validation, lookups, and deletion.
 */
public class MemberDAO {

    /**
     * Registers a new member using a new database connection.
     *
     * @param member Member object to register
     * @return true if registered successfully, false otherwise
     * @throws IllegalArgumentException if member ID or name is empty
     * @throws SQLException if a database constraint or connection error occurs
     */
    public boolean addMember(Member member) throws SQLException {
        try (Connection con = Database_connection.getConnection()) {
            return addMember(con, member);
        }
    }

    /**
     * Registers a new member using an existing database connection.
     *
     * @param con Active database connection
     * @param member Member object to register
     * @return true if registered, false otherwise
     * @throws SQLException if insert fails
     */
    public boolean addMember(Connection con, Member member) throws SQLException {
        if (member == null) {
            throw new IllegalArgumentException("Member details cannot be null.");
        }
        if (member.getMemberId() == null || member.getMemberId().trim().isEmpty()) {
            throw new IllegalArgumentException("Member ID cannot be empty.");
        }
        if (member.getName() == null || member.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Member name cannot be empty.");
        }

        // Check if member ID is already registered
        if (getMemberById(con, member.getMemberId().trim()) != null) {
            throw new SQLException("A member with ID '" + member.getMemberId().trim() + "' is already registered.");
        }

        String sql = "INSERT INTO members (member_id, name) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, member.getMemberId().trim());
            ps.setString(2, member.getName().trim());

            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLException("A member with ID '" + member.getMemberId() + "' is already registered.", e);
        }
    }

    /**
     * Retrieves all registered library members.
     *
     * @return List of all members
     * @throws SQLException if query fails
     */
    public List<Member> getAllMembers() throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY member_id ASC";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                members.add(new Member(
                        rs.getString("member_id"),
                        rs.getString("name")
                ));
            }
        }
        return members;
    }

    /**
     * Searches for a member by unique ID using a new connection.
     */
    public Member getMemberById(String memberId) throws SQLException {
        try (Connection con = Database_connection.getConnection()) {
            return getMemberById(con, memberId);
        }
    }

    /**
     * Searches for a member by unique ID using an existing connection.
     */
    public Member getMemberById(Connection con, String memberId) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT * FROM members WHERE member_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, memberId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Member(
                            rs.getString("member_id"),
                            rs.getString("name")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Searches for members by keyword (matching member ID or name).
     *
     * @param keyword Substring to match
     * @return List of matching members
     * @throws SQLException if query fails
     */
    public List<Member> searchMembers(String keyword) throws SQLException {
        List<Member> members = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllMembers();
        }

        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT * FROM members WHERE member_id LIKE ? OR name LIKE ? ORDER BY member_id ASC";

        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(new Member(
                            rs.getString("member_id"),
                            rs.getString("name")
                    ));
                }
            }
        }
        return members;
    }

    /**
     * Total number of registered members.
     */
    public int getTotalMembersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM members";
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
     * Safely deletes a member from the directory.
     * Rejects deletion if the member has active loans or borrowing history.
     *
     * @param memberId Member ID to remove
     * @return true if deleted, false if not found
     * @throws SQLException if active or historical loans prevent deletion
     */
    public boolean deleteMember(String memberId) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new IllegalArgumentException("Member ID cannot be empty.");
        }

        // 1. Check active loans
        String checkSql = "SELECT COUNT(*) FROM borrow_records WHERE member_id = ? AND return_date IS NULL";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setString(1, memberId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete member '" + memberId + "': Member currently has unreturned borrowed books.");
                }
            }
        }

        // 2. Check historical records
        String histSql = "SELECT COUNT(*) FROM borrow_records WHERE member_id = ?";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(histSql)) {
            ps.setString(1, memberId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete member '" + memberId + "': Member has historical borrowing transactions on record.");
                }
            }
        }

        String deleteSql = "DELETE FROM members WHERE member_id = ?";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(deleteSql)) {
            ps.setString(1, memberId.trim());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates an existing member's information.
     */
    public boolean updateMember(Member member) throws SQLException {
        if (member == null) throw new IllegalArgumentException("Member cannot be null.");
        String sql = "UPDATE members SET name = ? WHERE member_id = ?";
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, member.getName().trim());
            ps.setString(2, member.getMemberId().trim());
            return ps.executeUpdate() > 0;
        }
    }
}
