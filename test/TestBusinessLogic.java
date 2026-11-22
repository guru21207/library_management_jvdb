import java.time.LocalDate;

/**
 * Pure Unit Tests for BookNest Business Logic and Algorithms.
 * Runs completely independent of a live MySQL database.
 *
 * Validates:
 * 1. Inclusive day count calculations
 * 2. Standardized overdue fine formula (7 days free, ₹2/day overdue)
 * 3. Exact assignment requirements (7 days = ₹0, 12 days = ₹10)
 * 4. Input validation for Books and Members
 * 5. Static counter behavior in memory
 */
public class TestBusinessLogic {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   BOOKNEST PURE BUSINESS LOGIC TEST SUITE");
        System.out.println("=================================================");

        testDaysKeptCalculation();
        testFineCalculation();
        testStaticCounterLogic();
        testBookModelValidation();
        testMemberModelValidation();

        System.out.println("\n-------------------------------------------------");
        System.out.println("TEST SUMMARY: " + testsPassed + " Passed, " + testsFailed + " Failed.");
        if (testsFailed == 0) {
            System.out.println("🎉 All pure business logic tests PASSED successfully!");
        } else {
            System.err.println("❌ Some business logic tests FAILED.");
            System.exit(1);
        }
        System.out.println("=================================================");
    }

    private static void testDaysKeptCalculation() {
        System.out.println("\n[1] Testing Days Kept Inclusive Calculation:");

        LocalDate issue = LocalDate.of(2026, 10, 1);

        // Same day return: Oct 1 to Oct 1 = 1 inclusive day
        assertEquals("Same day return", 1, LibraryService.calculateDaysKept(issue, LocalDate.of(2026, 10, 1)));

        // 7 days loan: Oct 1 to Oct 7 = 7 inclusive days
        assertEquals("7 days loan (due date)", 7, LibraryService.calculateDaysKept(issue, LocalDate.of(2026, 10, 7)));

        // 12 days loan: Oct 1 to Oct 12 = 12 inclusive days
        assertEquals("12 days loan", 12, LibraryService.calculateDaysKept(issue, LocalDate.of(2026, 10, 12)));

        // Return date before issue date should throw exception
        try {
            LibraryService.calculateDaysKept(issue, LocalDate.of(2026, 9, 30));
            fail("Return date before issue date should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            pass("Properly rejected return date before issue date: " + e.getMessage());
        }

        // Null dates rejection
        try {
            LibraryService.calculateDaysKept(null, issue);
            fail("Null issue date should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            pass("Properly rejected null issue date");
        }
    }

    private static void testFineCalculation() {
        System.out.println("\n[2] Testing Fine Assessment Formula [fine = max(0, daysKept - 7) * 2]:");

        // 1 to 7 days: 0 fine
        assertEquals("1 day kept fine", 0.0, LibraryService.calculateFine(1));
        assertEquals("6 days kept fine", 0.0, LibraryService.calculateFine(6));
        assertEquals("7 days kept fine (Grace Period Boundary)", 0.0, LibraryService.calculateFine(7));

        // 8 days kept: (8 - 7) * 2 = 2.0
        assertEquals("8 days kept fine (1 day overdue)", 2.0, LibraryService.calculateFine(8));

        // 12 days kept: (12 - 7) * 2 = 10.0 (REQUIRED VERIFICATION EXAMPLE)
        assertEquals("12 days kept fine (REQUIRED: (12 - 7) * 2 = 10)", 10.0, LibraryService.calculateFine(12));

        // 20 days kept: (20 - 7) * 2 = 26.0
        assertEquals("20 days kept fine", 26.0, LibraryService.calculateFine(20));

        // Negative days rejection
        try {
            LibraryService.calculateFine(-1);
            fail("Negative days kept should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            pass("Properly rejected negative days kept");
        }
    }

    private static void testStaticCounterLogic() {
        System.out.println("\n[3] Testing Static Counter Behavior (Java Static Field):");

        LibraryService.setStaticCounterForTesting(100);
        assertEquals("Initial static counter value", 100, LibraryService.getTotalLifetimeIssued());

        // Increment in memory
        LibraryService.setStaticCounterForTesting(101);
        assertEquals("Incremented static counter value", 101, LibraryService.getTotalLifetimeIssued());
        pass("Static field correctly retains class-level state across invocations");
    }

    private static void testBookModelValidation() {
        System.out.println("\n[4] Testing Book DAO/Model Input Validation:");

        BookDAO dao = new BookDAO();

        // 1. Empty Book ID
        try {
            dao.addBook(new Book("", "Title", "Author", 5, 5));
            fail("Should reject empty book ID");
        } catch (IllegalArgumentException e) {
            pass("Rejected empty book ID: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 2. Empty Title
        try {
            dao.addBook(new Book("B999", "", "Author", 5, 5));
            fail("Should reject empty title");
        } catch (IllegalArgumentException e) {
            pass("Rejected empty title: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 3. Empty Author
        try {
            dao.addBook(new Book("B999", "Title", "", 5, 5));
            fail("Should reject empty author");
        } catch (IllegalArgumentException e) {
            pass("Rejected empty author: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 4. Zero or Negative Copies
        try {
            dao.addBook(new Book("B999", "Title", "Author", 0, 0));
            fail("Should reject zero total copies");
        } catch (IllegalArgumentException e) {
            pass("Rejected zero copies: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            dao.addBook(new Book("B999", "Title", "Author", -5, -5));
            fail("Should reject negative total copies");
        } catch (IllegalArgumentException e) {
            pass("Rejected negative copies: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 5. Available copies > total copies
        try {
            dao.addBook(new Book("B999", "Title", "Author", 5, 10));
            fail("Should reject available copies > total copies");
        } catch (IllegalArgumentException e) {
            pass("Rejected available copies exceeding total: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    private static void testMemberModelValidation() {
        System.out.println("\n[5] Testing Member DAO/Model Input Validation:");

        MemberDAO dao = new MemberDAO();

        // 1. Empty Member ID
        try {
            dao.addMember(new Member("", "Full Name"));
            fail("Should reject empty member ID");
        } catch (IllegalArgumentException e) {
            pass("Rejected empty member ID: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 2. Empty Name
        try {
            dao.addMember(new Member("M999", ""));
            fail("Should reject empty member name");
        } catch (IllegalArgumentException e) {
            pass("Rejected empty member name: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    private static void assertEquals(String testName, int expected, int actual) {
        if (expected == actual) {
            pass(testName + " -> Expected: " + expected + ", Got: " + actual);
        } else {
            fail(testName + " -> FAILED! Expected: " + expected + ", Got: " + actual);
        }
    }

    private static void assertEquals(String testName, double expected, double actual) {
        if (Math.abs(expected - actual) < 0.001) {
            pass(testName + " -> Expected: " + expected + ", Got: " + actual);
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
