import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Main application entry point for BookNest Library Management System.
 *
 * Supports two presentation modes:
 * 1. Modern Desktop GUI (Default): Java Swing graphical user interface
 * 2. Classic Console CLI (--console or -c): Interactive command-line menu
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static final BookDAO bookDAO = new BookDAO();
    private static final MemberDAO memberDAO = new MemberDAO();
    private static final BorrowDAO borrowDAO = new BorrowDAO();
    private static final LibraryService libraryService = new LibraryService();

    public static void main(String[] args) {
        if (args.length > 0 && ("--console".equalsIgnoreCase(args[0]) || "-c".equalsIgnoreCase(args[0]))) {
            runConsole();
        } else {
            // Enable text anti-aliasing for high-DPI displays
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

            System.out.println("Starting BookNest Library Management System (GUI Mode)...");
            System.out.println("Tip: To run the console interface instead, launch with: java -cp \"out;lib\\*\" Main --console");

            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    // Try to use system look and feel
                    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }
                BookNestGUI gui = new BookNestGUI();
                gui.setVisible(true);
            });
        }
    }

    public static void runConsole() {
        boolean running = true;

        while (running) {
            System.out.println("\n========================================================");
            System.out.println("               BOOKNEST LIBRARY SYSTEM");
            System.out.println("========================================================");
            System.out.println(" Lifetime Total Issued (Static Counter): " + LibraryService.getTotalLifetimeIssued());
            System.out.println("--------------------------------------------------------");
            System.out.println(" 1. Add Book");
            System.out.println(" 2. Display All Books");
            System.out.println(" 3. Search Books (Demonstrating Method Overloading)");
            System.out.println(" 4. Register Member");
            System.out.println(" 5. Display All Members");
            System.out.println(" 6. Search Members");
            System.out.println(" 7. Issue Book (Max 3 Active Loans Allowed)");
            System.out.println(" 8. Return Book (Automated Fine Assessment)");
            System.out.println(" 9. View Borrowing Transaction Records");
            System.out.println(" 10. View System Metrics & Static Counter");
            System.out.println(" 11. Exit");
            System.out.println("========================================================");

            int choice = readInt("Enter your choice (1-11): ");

            try {
                switch (choice) {
                    case 1:
                        addBook();
                        break;
                    case 2:
                        displayBooks();
                        break;
                    case 3:
                        searchBookMenu();
                        break;
                    case 4:
                        registerMember();
                        break;
                    case 5:
                        displayMembers();
                        break;
                    case 6:
                        searchMembers();
                        break;
                    case 7:
                        issueBook();
                        break;
                    case 8:
                        returnBook();
                        break;
                    case 9:
                        displayBorrowRecords();
                        break;
                    case 10:
                        displayMetrics();
                        break;
                    case 11:
                        running = false;
                        System.out.println("Thank you for using BookNest!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please select 1-11.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Validation error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void addBook() throws SQLException {
        System.out.println("\n--- Add New Book ---");

        System.out.print("Enter Book ID (e.g. B016): ");
        String id = scanner.nextLine().trim();

        System.out.print("Enter Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Enter Author: ");
        String author = scanner.nextLine().trim();

        int copies = readInt("Enter Total Copies: ");

        Book book = new Book(id, title, author, copies, copies);

        if (bookDAO.addBook(book)) {
            System.out.println("✓ Book '" + title + "' added successfully!");
        } else {
            System.out.println("✕ Book could not be added.");
        }
    }

    private static void displayBooks() throws SQLException {
        System.out.println("\n--- Catalog Books ---");

        List<Book> books = bookDAO.getAllBooks();

        if (books.isEmpty()) {
            System.out.println("No books found in catalog.");
            return;
        }

        for (Book book : books) {
            System.out.println(book);
        }
    }

    /**
     * Demonstrates Java Method Overloading by invoking different signatures of searchBooks / searchBook.
     */
    private static void searchBookMenu() throws SQLException {
        System.out.println("\n--- Search Books (Method Overloading) ---");
        System.out.println("1. Unified Search (matches Book ID, Title, or Author) -> searchBooks(String)");
        System.out.println("2. Search by Title Keyword -> searchBooks(String, true)");
        System.out.println("3. Search by Author Keyword -> searchBooks(String, false)");
        System.out.println("4. Exact Search by Book ID -> searchBook(String)");

        int subChoice = readInt("Select search type (1-4): ");
        System.out.print("Enter search query: ");
        String query = scanner.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println("Search query cannot be empty.");
            return;
        }

        switch (subChoice) {
            case 1: {
                // Overload: searchBooks(String query)
                List<Book> results = libraryService.searchBooks(query);
                printBookResults(results);
                break;
            }
            case 2: {
                // Overload: searchBooks(String keyword, boolean searchByTitle)
                List<Book> results = libraryService.searchBooks(query, true);
                printBookResults(results);
                break;
            }
            case 3: {
                // Overload: searchBooks(String keyword, boolean searchByTitle)
                List<Book> results = libraryService.searchBooks(query, false);
                printBookResults(results);
                break;
            }
            case 4: {
                // Overload: searchBook(String bookId)
                Book b = libraryService.searchBook(query);
                if (b != null) {
                    System.out.println("✓ Found book: " + b);
                } else {
                    System.out.println("✕ No book found with ID: " + query);
                }
                break;
            }
            default:
                System.out.println("Invalid search option.");
        }
    }

    private static void printBookResults(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("✕ No matching books found.");
        } else {
            System.out.println("✓ Found " + books.size() + " matching book(s):");
            for (Book b : books) {
                System.out.println("  " + b);
            }
        }
    }

    private static void registerMember() throws SQLException {
        System.out.println("\n--- Register Member ---");

        System.out.print("Enter Member ID (e.g. M008): ");
        String id = scanner.nextLine().trim();

        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim();

        Member member = new Member(id, name);

        if (memberDAO.addMember(member)) {
            System.out.println("✓ Member '" + name + "' registered successfully!");
        } else {
            System.out.println("✕ Member could not be registered.");
        }
    }

    private static void displayMembers() throws SQLException {
        System.out.println("\n--- Registered Members ---");

        List<Member> members = memberDAO.getAllMembers();

        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        for (Member member : members) {
            int active = borrowDAO.getActiveLoanCountForMember(member.getMemberId());
            System.out.println(member + " | Active Loans: " + active + "/3");
        }
    }

    private static void searchMembers() throws SQLException {
        System.out.println("\n--- Search Members ---");
        System.out.print("Enter member name or ID: ");
        String query = scanner.nextLine().trim();

        List<Member> list = memberDAO.searchMembers(query);
        if (list.isEmpty()) {
            System.out.println("No matching members found.");
        } else {
            for (Member m : list) {
                int active = borrowDAO.getActiveLoanCountForMember(m.getMemberId());
                System.out.println(m + " | Active Loans: " + active + "/3");
            }
        }
    }

    private static void issueBook() throws SQLException {
        System.out.println("\n--- Issue Book (Max 3 Active Loans) ---");

        System.out.print("Enter Member ID: ");
        String memberId = scanner.nextLine().trim();

        System.out.print("Enter Book ID: ");
        String bookId = scanner.nextLine().trim();

        String result = libraryService.issueBook(memberId, bookId);
        System.out.println(result);
    }

    private static void returnBook() throws SQLException {
        System.out.println("\n--- Return Book ---");

        int recordId = readInt("Enter Borrow Record ID: ");

        String result = libraryService.returnBook(recordId);
        System.out.println(result);
    }

    private static void displayBorrowRecords() throws SQLException {
        System.out.println("\n--- Circulation Audit History ---");

        List<BorrowRecordDetail> records = borrowDAO.getDetailedBorrowRecords();

        if (records.isEmpty()) {
            System.out.println("No borrowing transactions found.");
            return;
        }

        for (BorrowRecordDetail r : records) {
            System.out.println(r);
        }
    }

    private static void displayMetrics() throws SQLException {
        System.out.println("\n--- System Metrics & Static Counter ---");
        System.out.println("• Lifetime Total Books Issued (Java Static Counter): " + LibraryService.getTotalLifetimeIssued());
        System.out.println("• Total Distinct Titles: " + bookDAO.getTotalBooksCount());
        System.out.println("• Total Physical Copies: " + bookDAO.getTotalCopiesCount());
        System.out.println("• Available Copies on Shelves: " + bookDAO.getAvailableCopiesCount());
        System.out.println("• Registered Patrons: " + memberDAO.getTotalMembersCount());
        System.out.println("• Currently Active Outstanding Loans: " + borrowDAO.getActiveLoansCount());
        System.out.println("• Total Fines Collected: ₹" + String.format("%.2f", borrowDAO.getTotalFinesCollected()));
    }

    private static int readInt(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }
}
