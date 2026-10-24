import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

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
            System.out.println("\n================================");
            System.out.println("       BOOKNEST LIBRARY");
            System.out.println("================================");
            System.out.println("1. Add Book");
            System.out.println("2. Display All Books");
            System.out.println("3. Search Book by Title");
            System.out.println("4. Register Member");
            System.out.println("5. Display All Members");
            System.out.println("6. Issue Book");
            System.out.println("7. Return Book");
            System.out.println("8. View Borrowing Records");
            System.out.println("9. Exit");
            System.out.println("================================");

            int choice = readInt("Enter your choice: ");

            try {
                switch (choice) {
                    case 1:
                        addBook();
                        break;
                    case 2:
                        displayBooks();
                        break;
                    case 3:
                        searchBook();
                        break;
                    case 4:
                        registerMember();
                        break;
                    case 5:
                        displayMembers();
                        break;
                    case 6:
                        issueBook();
                        break;
                    case 7:
                        returnBook();
                        break;
                    case 8:
                        displayBorrowRecords();
                        break;
                    case 9:
                        running = false;
                        System.out.println("Thank you for using BookNest!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please select 1-9.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }


    private static void addBook() throws SQLException {
        System.out.println("\n--- Add Book ---");

        System.out.print("Enter Book ID: ");
        String id = scanner.nextLine().trim();

        System.out.print("Enter Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Enter Author: ");
        String author = scanner.nextLine().trim();

        int copies = readInt("Enter Total Copies: ");

        if (id.isEmpty() || title.isEmpty() || author.isEmpty()) {
            System.out.println("Book ID, title and author cannot be empty.");
            return;
        }

        if (copies <= 0) {
            System.out.println("Total copies must be greater than zero.");
            return;
        }

        Book book = new Book(id, title, author, copies, copies);

        if (bookDAO.addBook(book)) {
            System.out.println("Book added successfully!");
        } else {
            System.out.println("Book could not be added.");
        }
    }

    private static void displayBooks() throws SQLException {
        System.out.println("\n--- All Books ---");

        List<Book> books = bookDAO.getAllBooks();

        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        for (Book book : books) {
            System.out.println(book);
        }
    }

    private static void searchBook() throws SQLException {
        System.out.println("\n--- Search Book ---");

        System.out.print("Enter title or keyword: ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("Please enter a search keyword.");
            return;
        }

        List<Book> books = bookDAO.searchBooksByTitle(keyword);

        if (books.isEmpty()) {
            System.out.println("No matching books found.");
            return;
        }

        for (Book book : books) {
            System.out.println(book);
        }
    }

    private static void registerMember() throws SQLException {
        System.out.println("\n--- Register Member ---");

        System.out.print("Enter Member ID: ");
        String id = scanner.nextLine().trim();

        System.out.print("Enter Member Name: ");
        String name = scanner.nextLine().trim();

        if (id.isEmpty() || name.isEmpty()) {
            System.out.println("Member ID and name cannot be empty.");
            return;
        }

        Member member = new Member(id, name);

        if (memberDAO.addMember(member)) {
            System.out.println("Member registered successfully!");
        } else {
            System.out.println("Member could not be registered.");
        }
    }

    private static void displayMembers() throws SQLException {
        System.out.println("\n--- All Members ---");

        List<Member> members = memberDAO.getAllMembers();

        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        for (Member member : members) {
            System.out.println(member);
        }
    }

    private static void issueBook() throws SQLException {
        System.out.println("\n--- Issue Book ---");

        System.out.print("Enter Member ID: ");
        String memberId = scanner.nextLine().trim();

        System.out.print("Enter Book ID: ");
        String bookId = scanner.nextLine().trim();

        System.out.println(libraryService.issueBook(memberId, bookId));
    }

    private static void returnBook() throws SQLException {
        System.out.println("\n--- Return Book ---");

        int recordId = readInt("Enter Borrow Record ID: ");

        System.out.println(libraryService.returnBook(recordId));
    }

    private static void displayBorrowRecords() throws SQLException {
        System.out.println("\n--- Borrowing Records ---");

        List<BorrowRecord> records = borrowDAO.getAllBorrowRecords();

        if (records.isEmpty()) {
            System.out.println("No borrowing records found.");
            return;
        }

        for (BorrowRecord record : records) {
            System.out.println(record);
        }
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
