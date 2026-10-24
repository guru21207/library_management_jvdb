-- ========================================================
-- BookNest Library Management System - Sample Seed Data
-- ========================================================

USE booknest;

-- 1. Insert Sample Books
INSERT INTO books (book_id, title, author, total_copies, available_copies) VALUES
('B001', 'Wings of Fire', 'A. P. J. Abdul Kalam', 5, 4),
('B002', 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', 4, 3),
('B003', 'The Pragmatic Programmer: Your Journey to Mastery', 'David Thomas, Andrew Hunt', 3, 3),
('B004', 'Atomic Habits', 'James Clear', 6, 5),
('B005', 'Introduction to Algorithms (4th Edition)', 'Thomas H. Cormen, Charles E. Leiserson', 4, 3),
('B006', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', 3, 2),
('B007', 'To Kill a Mockingbird', 'Harper Lee', 5, 5),
('B008', '1984', 'George Orwell', 4, 3),
('B009', 'The Great Gatsby', 'F. Scott Fitzgerald', 3, 3),
('B010', 'Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', 5, 5),
('B011', 'Deep Work: Rules for Focused Success in a Distracted World', 'Cal Newport', 4, 4),
('B012', 'The Psychology of Money', 'Morgan Housel', 5, 4),
('B013', 'Head First Java (3rd Edition)', 'Kathy Sierra, Bert Bates, Trisha Gee', 4, 0),
('B014', 'Effective Java (3rd Edition)', 'Joshua Bloch', 3, 1),
('B015', 'Computer Networking: A Top-Down Approach', 'James F. Kurose, Keith W. Ross', 4, 4)
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    author = VALUES(author),
    total_copies = VALUES(total_copies),
    available_copies = VALUES(available_copies);

-- 2. Insert Sample Members
INSERT INTO members (member_id, name) VALUES
('M001', 'Arun Kumar'),
('M002', 'Priya Sharma'),
('M003', 'Rahul Verma'),
('M004', 'Ananya Iyer'),
('M005', 'Vikram Singh'),
('M006', 'Sneha Patel'),
('M007', 'Rohan Das')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name);

-- 3. Insert Historical Completed Borrow Records
INSERT INTO borrow_records (member_id, book_id, issue_date, return_date, days_kept, fine) VALUES
('M001', 'B004', '2026-09-01', '2026-09-06', 6, 0.00),
('M002', 'B001', '2026-09-10', '2026-09-22', 13, 12.00),
('M005', 'B007', '2026-09-15', '2026-09-21', 7, 0.00),
('M007', 'B010', '2026-09-25', '2026-10-06', 12, 10.00),
('M003', 'B011', '2026-10-01', '2026-10-09', 9, 4.00);

-- 4. Insert Active (Unreturned) Borrow Records
-- Note: Issue dates relative to current date (October 2026)
INSERT INTO borrow_records (member_id, book_id, issue_date, return_date, days_kept, fine) VALUES
('M001', 'B002', DATE_SUB(CURDATE(), INTERVAL 3 DAY), NULL, 0, 0.00),
('M002', 'B005', DATE_SUB(CURDATE(), INTERVAL 11 DAY), NULL, 0, 0.00),
('M003', 'B006', DATE_SUB(CURDATE(), INTERVAL 5 DAY), NULL, 0, 0.00),
('M004', 'B008', DATE_SUB(CURDATE(), INTERVAL 4 DAY), NULL, 0, 0.00),
('M005', 'B012', DATE_SUB(CURDATE(), INTERVAL 2 DAY), NULL, 0, 0.00),
('M006', 'B014', DATE_SUB(CURDATE(), INTERVAL 15 DAY), NULL, 0, 0.00),
('M006', 'B001', DATE_SUB(CURDATE(), INTERVAL 1 DAY), NULL, 0, 0.00),
('M007', 'B013', DATE_SUB(CURDATE(), INTERVAL 6 DAY), NULL, 0, 0.00),
('M002', 'B013', DATE_SUB(CURDATE(), INTERVAL 9 DAY), NULL, 0, 0.00),
('M003', 'B013', DATE_SUB(CURDATE(), INTERVAL 4 DAY), NULL, 0, 0.00),
('M004', 'B013', DATE_SUB(CURDATE(), INTERVAL 2 DAY), NULL, 0, 0.00);
