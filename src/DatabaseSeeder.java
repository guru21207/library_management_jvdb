import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Database Seeder for BookNest Library Management System.
 *
 * SAFETY GUARANTEES:
 * - Default execution performs safe, non-destructive insert-only seeding.
 * - Never deletes or overwrites existing user borrow records during standard execution.
 * - Destructive reset requires an explicit '--reset' or '--force' command-line flag.
 */
public class DatabaseSeeder {

    public static void main(String[] args) {
        boolean allowReset = false;
        if (args.length > 0 && ("--reset".equalsIgnoreCase(args[0]) || "--force".equalsIgnoreCase(args[0]))) {
            allowReset = true;
            System.out.println("⚠️  WARNING: Explicit '--reset' flag detected. Performing full seed reset on development database.");
        } else {
            System.out.println("ℹ️  Running safe insert-only seeder. Existing user data and loan history will be preserved.");
            System.out.println("   (Tip: To perform a complete reset in a disposable test environment, run with: --reset)");
        }

        try (Connection con = Database_connection.getConnection();
             Statement stmt = con.createStatement()) {

            // 1. Books (Safe Upsert)
            stmt.executeUpdate("INSERT INTO books (book_id, title, author, total_copies, available_copies) VALUES " +
                    "('B001', 'Wings of Fire', 'A. P. J. Abdul Kalam', 5, 4), " +
                    "('B002', 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', 4, 3), " +
                    "('B003', 'The Pragmatic Programmer: Your Journey to Mastery', 'David Thomas, Andrew Hunt', 3, 3), " +
                    "('B004', 'Atomic Habits', 'James Clear', 6, 5), " +
                    "('B005', 'Introduction to Algorithms (4th Edition)', 'Thomas H. Cormen, Charles E. Leiserson', 4, 3), " +
                    "('B006', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma et al.', 3, 2), " +
                    "('B007', 'To Kill a Mockingbird', 'Harper Lee', 5, 5), " +
                    "('B008', '1984', 'George Orwell', 4, 3), " +
                    "('B009', 'The Great Gatsby', 'F. Scott Fitzgerald', 3, 3), " +
                    "('B010', 'Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', 5, 5), " +
                    "('B011', 'Deep Work: Rules for Focused Success', 'Cal Newport', 4, 4), " +
                    "('B012', 'The Psychology of Money', 'Morgan Housel', 5, 4), " +
                    "('B013', 'Head First Java (3rd Edition)', 'Kathy Sierra, Bert Bates', 4, 0), " +
                    "('B014', 'Effective Java (3rd Edition)', 'Joshua Bloch', 3, 1), " +
                    "('B015', 'Computer Networking: A Top-Down Approach', 'James F. Kurose', 4, 4) " +
                    "ON DUPLICATE KEY UPDATE title=VALUES(title), author=VALUES(author), total_copies=VALUES(total_copies), available_copies=VALUES(available_copies)");

            System.out.println("✓ Books seeded/updated successfully (15 titles)");

            // 2. Members (Safe Upsert)
            stmt.executeUpdate("INSERT INTO members (member_id, name) VALUES " +
                    "('M001', 'Arun Kumar'), " +
                    "('M002', 'Priya Sharma'), " +
                    "('M003', 'Rahul Verma'), " +
                    "('M004', 'Ananya Iyer'), " +
                    "('M005', 'Vikram Singh'), " +
                    "('M006', 'Sneha Patel'), " +
                    "('M007', 'Rohan Das') " +
                    "ON DUPLICATE KEY UPDATE name=VALUES(name)");

            System.out.println("✓ Members seeded/updated successfully (7 members)");

            // 3. Borrow Records
            int existingRecords = 0;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM borrow_records")) {
                if (rs.next()) {
                    existingRecords = rs.getInt(1);
                }
            }

            if (existingRecords > 0 && !allowReset) {
                System.out.println("✓ Preserved " + existingRecords + " existing borrow transaction records (safe mode active).");
            } else {
                if (allowReset && existingRecords > 0) {
                    stmt.executeUpdate("DELETE FROM borrow_records");
                    System.out.println("✓ Cleared previous borrow records per --reset request.");
                }

                // Historical completed loans
                stmt.executeUpdate("INSERT INTO borrow_records (member_id, book_id, issue_date, return_date, days_kept, fine) VALUES " +
                        "('M001', 'B004', DATE_SUB(CURDATE(), INTERVAL 40 DAY), DATE_SUB(CURDATE(), INTERVAL 34 DAY), 6, 0.00), " +
                        "('M002', 'B001', DATE_SUB(CURDATE(), INTERVAL 35 DAY), DATE_SUB(CURDATE(), INTERVAL 22 DAY), 13, 12.00), " +
                        "('M005', 'B007', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 23 DAY), 7, 0.00), " +
                        "('M007', 'B010', DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 13 DAY), 12, 10.00), " +
                        "('M003', 'B011', DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 11 DAY), 9, 4.00)");

                // Active outstanding loans
                stmt.executeUpdate("INSERT INTO borrow_records (member_id, book_id, issue_date, return_date, days_kept, fine) VALUES " +
                        "('M001', 'B002', DATE_SUB(CURDATE(), INTERVAL 3 DAY), NULL, 0, 0.00), " +
                        "('M002', 'B005', DATE_SUB(CURDATE(), INTERVAL 11 DAY), NULL, 0, 0.00), " +
                        "('M003', 'B006', DATE_SUB(CURDATE(), INTERVAL 5 DAY), NULL, 0, 0.00), " +
                        "('M004', 'B008', DATE_SUB(CURDATE(), INTERVAL 4 DAY), NULL, 0, 0.00), " +
                        "('M005', 'B012', DATE_SUB(CURDATE(), INTERVAL 2 DAY), NULL, 0, 0.00), " +
                        "('M006', 'B014', DATE_SUB(CURDATE(), INTERVAL 15 DAY), NULL, 0, 0.00), " +
                        "('M006', 'B001', DATE_SUB(CURDATE(), INTERVAL 1 DAY), NULL, 0, 0.00), " +
                        "('M007', 'B013', DATE_SUB(CURDATE(), INTERVAL 6 DAY), NULL, 0, 0.00), " +
                        "('M002', 'B013', DATE_SUB(CURDATE(), INTERVAL 9 DAY), NULL, 0, 0.00), " +
                        "('M003', 'B013', DATE_SUB(CURDATE(), INTERVAL 4 DAY), NULL, 0, 0.00), " +
                        "('M004', 'B013', DATE_SUB(CURDATE(), INTERVAL 2 DAY), NULL, 0, 0.00)");

                System.out.println("✓ Borrow records seeded (5 historical returned + 11 active loans)");
            }

            System.out.println("🎉 Database seeding finished successfully!");

        } catch (Exception e) {
            System.err.println("❌ Database seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
