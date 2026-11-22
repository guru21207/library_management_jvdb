import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * End-to-End Integration Test Suite for BookNest with live MySQL Database.
 *
 * Validates:
 * 1. Book CRUD & Duplicate ID Rejection
 * 2. Member CRUD & Duplicate Member ID Rejection
 * 3. Overloaded Search Methods (Book ID, Title, Author, Unified)
 * 4. Issue Book Workflow & Copy Decrement
 * 5. Strict 3-Active-Loan Limit Enforcement (4th loan rejection)
 * 6. Return Book Workflow & Inventory Restocking
 * 7. Duplicate Return Rejection
 * 8. Static Lifetime Counter Consistency
 *
 * Non-destructive: Uses isolated test identifiers (TEST_B901.., TEST_M901) and cleans
 * only those test artifacts upon completion.
 */
public class TestIntegrationSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static final BookDAO bookDAO = new BookDAO();
    private static final MemberDAO memberDAO = new MemberDAO();
    private static final BorrowDAO borrowDAO = new BorrowDAO();
    private static final LibraryService libraryService = new LibraryService();

    private static final String TEST_MEMBER = "TEST_M99";
    private static final String TEST_BOOK_1 = "TEST_B91";
    private static final String TEST_BOOK_2 = "TEST_B92";
    private static final String TEST_BOOK_3 = "TEST_B93";
    private static final String TEST_BOOK_4 = "TEST_B94";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   BOOKNEST LIVE MYSQL INTEGRATION TEST SUITE");
        System.out.println("=================================================");

        // Verify MySQL connectivity before running suite
        if (!Database_connection.testConnection()) {
            System.err.println("⚠️ MySQL connection is offline. Skipping live integration tests.");
            System.err.println("   (Configure db.properties or set environment variables to enable)");
            return;
        }

        System.out.println("Connected to MySQL: " + Database_connection.getConnectionInfo());

        try {
            cleanupTestArtifacts();

            testBookManagementAndDuplicates();
            testMemberManagementAndDuplicates();
            testOverloadedSearchMethods();
            testIssueWorkflowAndStaticCounter();
            testThreeActiveLoansLimit();
            testReturnWorkflowAndDuplicateReturn();

        } catch (Exception e) {
            System.err.println("Unexpected integration test exception: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        } finally {
            cleanupTestArtifacts();
        }

        System.out.println("\n-------------------------------------------------");
        System.out.println("INTEGRATION SUITE SUMMARY: " + testsPassed + " Passed, " + testsFailed + " Failed.");
        if (testsFailed == 0) {
            System.out.println("🎉 All MySQL integration tests PASSED successfully!");
        } else {
            System.err.println("❌ Some integration tests FAILED.");
            System.exit(1);
        }
        System.out.println("=================================================");
    }

    private static void testBookManagementAndDuplicates() throws SQLException {
        System.out.println("\n[1] Testing Book Creation & Duplicate ID Prevention:");

        // Add valid book
        Book b1 = new Book(TEST_BOOK_1, "Algorithms in Java", "Robert Sedgewick", 3, 3);
        boolean added = bookDAO.addBook(b1);
        assertTrue("Add valid book", added);

        // Verify inserted
        Book found = bookDAO.getBookById(TEST_BOOK_1);
        assertNotNull("Find book by ID", found);
        assertEquals("Available copies match", 3, found.getAvailableCopies());

        // Attempt duplicate Book ID -> Should fail
        try {
            bookDAO.addBook(new Book(TEST_BOOK_1, "Duplicate Title", "Author", 2, 2));
            fail("Expected duplicate Book ID rejection");
        } catch (SQLException e) {
            pass("Properly rejected duplicate Book ID: " + e.getMessage());
        }
    }

    private static void testMemberManagementAndDuplicates() throws SQLException {
        System.out.println("\n[2] Testing Member Registration & Duplicate ID Prevention:");

        // Add valid member
        Member m = new Member(TEST_MEMBER, "Test User");
        boolean added = memberDAO.addMember(m);
        assertTrue("Register valid member", added);

        // Verify inserted
        Member found = memberDAO.getMemberById(TEST_MEMBER);
        assertNotNull("Find member by ID", found);
        assertEquals("Member name matches", "Test User", found.getName());

        // Attempt duplicate Member ID -> Should fail
        try {
            memberDAO.addMember(new Member(TEST_MEMBER, "Another Name"));
            fail("Expected duplicate Member ID rejection");
        } catch (SQLException e) {
            pass("Properly rejected duplicate Member ID: " + e.getMessage());
        }
    }

    private static void testOverloadedSearchMethods() throws SQLException {
        System.out.println("\n[3] Testing Overloaded searchBook / searchBooks Methods:");

        // 1. Exact ID search: searchBook(String)
        Book byId = libraryService.searchBook(TEST_BOOK_1);
        assertNotNull("Overload 1: searchBook(bookId)", byId);
        assertEquals("Found correct book", "Algorithms in Java", byId.getTitle());

        // 2. Keyword search by Title: searchBooks(String, true)
        List<Book> byTitle = libraryService.searchBooks("Algorithms", true);
        assertTrue("Overload 2: searchBooks(keyword, true) for Title", !byTitle.isEmpty());
        pass("Found " + byTitle.size() + " title matches");

        // 3. Keyword search by Author: searchBooks(String, false)
        List<Book> byAuthor = libraryService.searchBooks("Sedgewick", false);
        assertTrue("Overload 3: searchBooks(keyword, false) for Author", !byAuthor.isEmpty());
        pass("Found " + byAuthor.size() + " author matches");

        // 4. Unified multi-field search: searchBooks(String)
        List<Book> byUnified = libraryService.searchBooks(TEST_BOOK_1);
        assertTrue("Overload 4: searchBooks(query) unified lookup", !byUnified.isEmpty());
        pass("Unified search successfully matched by ID");
    }

    private static void testIssueWorkflowAndStaticCounter() throws SQLException {
        System.out.println("\n[4] Testing Book Issue Workflow & Static Counter:");

        int initialIssuedCount = LibraryService.getTotalLifetimeIssued();

        // Issue TEST_BOOK_1 to TEST_MEMBER
        String res = libraryService.issueBook(TEST_MEMBER, TEST_BOOK_1);
        assertTrue("Issue book returns success message", res.contains("successfully"));

        // Verify available copies decremented by exactly 1 (from 3 to 2)
        Book b = bookDAO.getBookById(TEST_BOOK_1);
        assertEquals("Available copies decremented", 2, b.getAvailableCopies());

        // Verify static lifetime counter incremented by 1
        int newIssuedCount = LibraryService.getTotalLifetimeIssued();
        assertEquals("Static lifetime issued counter incremented by 1", initialIssuedCount + 1, newIssuedCount);

        // Verify member has 1 active loan
        int active = borrowDAO.getActiveLoanCountForMember(TEST_MEMBER);
        assertEquals("Active loan count for member is 1", 1, active);
    }

    private static void testThreeActiveLoansLimit() throws SQLException {
        System.out.println("\n[5] Testing 3-Active-Loan Limit Enforcement (Max 3 Allowed):");

        // Add 3 more books
        bookDAO.addBook(new Book(TEST_BOOK_2, "Data Structures", "Mark Allen", 2, 2));
        bookDAO.addBook(new Book(TEST_BOOK_3, "Operating Systems", "Silberschatz", 2, 2));
        bookDAO.addBook(new Book(TEST_BOOK_4, "Database Systems", "Navathe", 2, 2));

        // Member already has 1 loan (TEST_BOOK_1)
        // Issue 2nd book
        String r2 = libraryService.issueBook(TEST_MEMBER, TEST_BOOK_2);
        assertTrue("Issue 2nd book", r2.contains("successfully"));

        // Issue 3rd book
        String r3 = libraryService.issueBook(TEST_MEMBER, TEST_BOOK_3);
        assertTrue("Issue 3rd book (Limit reached: 3/3)", r3.contains("successfully"));

        int active = borrowDAO.getActiveLoanCountForMember(TEST_MEMBER);
        assertEquals("Member now has exactly 3 active loans", 3, active);

        // Attempt to issue 4th book -> MUST BE REJECTED!
        String r4 = libraryService.issueBook(TEST_MEMBER, TEST_BOOK_4);
        assertTrue("4th loan attempt is strictly rejected", r4.contains("already has 3 active loans"));
        pass("Service layer enforced 3-active-loans restriction: " + r4);

        // Verify TEST_BOOK_4 copies were NOT decremented (rollback confirmed)
        Book b4 = bookDAO.getBookById(TEST_BOOK_4);
        assertEquals("Book 4 availability preserved after rollback", 2, b4.getAvailableCopies());
    }

    private static void testReturnWorkflowAndDuplicateReturn() throws SQLException {
        System.out.println("\n[6] Testing Return Workflow & Duplicate Return Prevention:");

        // Find one of the active borrow records for TEST_MEMBER
        int recordId = -1;
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT record_id FROM borrow_records WHERE member_id = ? AND book_id = ? AND return_date IS NULL")) {
            ps.setString(1, TEST_MEMBER);
            ps.setString(2, TEST_BOOK_1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    recordId = rs.getInt(1);
                }
            }
        }

        assertTrue("Active borrow record found for return", recordId > 0);

        // Return the book
        String returnRes = libraryService.returnBook(recordId);
        assertTrue("Book returned successfully", returnRes.contains("successfully"));

        // Verify available copies restored (+1, from 2 back to 3)
        Book b1 = bookDAO.getBookById(TEST_BOOK_1);
        assertEquals("Available copies restored to 3", 3, b1.getAvailableCopies());

        // Verify record is marked returned in database
        try (Connection con = Database_connection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT return_date FROM borrow_records WHERE record_id = ?")) {
            ps.setInt(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue("Record exists", rs.next());
                assertNotNull("return_date is no longer NULL", rs.getDate("return_date"));
            }
        }

        // Attempt to return the same record a 2nd time -> MUST BE REJECTED!
        String doubleReturn = libraryService.returnBook(recordId);
        assertTrue("Second return is rejected", doubleReturn.contains("already been returned"));
        pass("Prevented duplicate return: " + doubleReturn);
    }

    private static void cleanupTestArtifacts() {
        try (Connection con = Database_connection.getConnection()) {
            // Delete test borrow records
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM borrow_records WHERE member_id = ? OR book_id LIKE 'TEST_%'")) {
                ps.setString(1, TEST_MEMBER);
                ps.executeUpdate();
            }
            // Delete test books
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM books WHERE book_id LIKE 'TEST_%'")) {
                ps.executeUpdate();
            }
            // Delete test member
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM members WHERE member_id = ?")) {
                ps.setString(1, TEST_MEMBER);
                ps.executeUpdate();
            }
            LibraryService.syncLifetimeIssuedCounter();
        } catch (Exception ignored) {
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            pass(testName);
        } else {
            fail(testName);
        }
    }

    private static void assertNotNull(String testName, Object obj) {
        if (obj != null) {
            pass(testName + " (Not null)");
        } else {
            fail(testName + " (Unexpected null)");
        }
    }

    private static void assertEquals(String testName, int expected, int actual) {
        if (expected == actual) {
            pass(testName + " -> " + actual);
        } else {
            fail(testName + " -> FAILED! Expected: " + expected + ", Got: " + actual);
        }
    }

    private static void assertEquals(String testName, String expected, String actual) {
        if (expected != null && expected.equals(actual)) {
            pass(testName + " -> " + actual);
        } else {
            fail(testName + " -> FAILED! Expected: " + expected + ", Got: " + actual);
        }
    }

    private static void pass(String msg) {
        System.out.println("  ✓ " + msg);
        testsPassed++;
    }

    private static void fail(String msg) {
        System.err.println("  ✕ " + msg);
        testsFailed++;
    }
}
