import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Generates a high-resolution 300 DPI PNG diagram for BookNest ER Diagram (docs/ER_Diagram.png).
 * Uses Java2D anti-aliased vector rendering.
 */
public class GenerateERDiagramPNG {

    public static void main(String[] args) {
        int width = 2400;
        int height = 1700;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // High quality rendering hints
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // Scale 2x for retina / high-res presentation
        g.scale(2.0, 2.0);

        // Colors
        Color cBg = new Color(15, 23, 42);          // #0F172A
        Color cCard = new Color(30, 41, 59);        // #1E293B
        Color cCardAlt = new Color(23, 32, 51);     // #172033
        Color cBorder = new Color(51, 65, 85);      // #334155
        Color cText = new Color(248, 250, 252);     // #F8FAFC
        Color cMuted = new Color(148, 163, 184);    // #94A3B8
        Color cPrimary = new Color(99, 102, 241);   // #6366F1
        Color cSuccess = new Color(16, 185, 129);   // #10B981
        Color cWarning = new Color(245, 158, 11);   // #F59E0B
        Color cCyan = new Color(56, 189, 248);      // #38BDF8
        Color cPurple = new Color(168, 85, 247);    // #A855F7

        // Canvas Background
        g.setColor(cBg);
        g.fillRect(0, 0, 1200, 850);

        // Title Header Block
        g.setColor(cCard);
        g.fillRoundRect(50, 30, 1100, 80, 14, 14);
        g.setColor(cBorder);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(50, 30, 1100, 80, 14, 14);

        g.setColor(cText);
        g.setFont(new Font("Segoe UI", Font.BOLD, 24));
        g.drawString("BookNest • Entity-Relationship (ER) Schema Diagram", 75, 65);

        g.setColor(cMuted);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.drawString("Relational Conceptual & Physical Schema for MySQL 8.0 • Layered Architecture (books, members, borrow_records)", 75, 90);

        // ==========================================
        // ENTITY 1: MEMBERS (Top Left)
        // ==========================================
        int memX = 60, memY = 150, memW = 340, memH = 240;
        drawCard(g, memX, memY, memW, memH, cCard, cBorder);

        // Header
        g.setColor(cSuccess);
        g.fillRoundRect(memX, memY, memW, 54, 12, 12);
        g.fillRect(memX, memY + 30, memW, 24); // flatten bottom corners of header
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 17));
        g.drawString("MEMBERS", memX + 20, memY + 28);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("Patron master directory", memX + 20, memY + 46);

        // Column Subheader
        g.setColor(new Color(24, 34, 52));
        g.fillRect(memX, memY + 54, memW, 28);
        g.setColor(cMuted);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.drawString("KEY", memX + 16, memY + 72);
        g.drawString("FIELD", memX + 65, memY + 72);
        g.drawString("DATA TYPE", memX + 205, memY + 72);

        // Row 1: member_id
        g.setColor(cCardAlt);
        g.fillRect(memX, memY + 82, memW, 36);
        drawBadge(g, "PK", memX + 16, memY + 92, 28, 16, cWarning);
        g.setColor(cText);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.drawString("member_id", memX + 65, memY + 104);
        g.setColor(cMuted);
        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        g.drawString("VARCHAR(20)", memX + 205, memY + 104);

        g.setColor(cBorder);
        g.drawLine(memX, memY + 118, memX + memW, memY + 118);

        // Row 2: name
        g.setColor(cCard);
        g.fillRect(memX, memY + 118, memW, 36);
        g.setColor(cText);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.drawString("name", memX + 65, memY + 140);
        g.setColor(cMuted);
        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        g.drawString("VARCHAR(100)", memX + 205, memY + 140);

        // Bottom rule & footer
        g.setColor(cBorder);
        g.drawLine(memX, memY + 160, memX + memW, memY + 160);
        g.setColor(cMuted);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.drawString("Primary Key: member_id (Unique patron ID)", memX + 16, memY + 185);
        g.setColor(cSuccess);
        g.drawString("● Constraint: Max 3 active loans simultaneously", memX + 16, memY + 210);

        // ==========================================
        // ENTITY 2: BOOKS (Bottom Left)
        // ==========================================
        int bkX = 60, bkY = 430, bkW = 340, bkH = 370;
        drawCard(g, bkX, bkY, bkW, bkH, cCard, cBorder);

        // Header
        g.setColor(cPrimary);
        g.fillRoundRect(bkX, bkY, bkW, 54, 12, 12);
        g.fillRect(bkX, bkY + 30, bkW, 24);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 17));
        g.drawString("BOOKS", bkX + 20, bkY + 28);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("Bibliographic catalog & physical copies", bkX + 20, bkY + 46);

        // Column Subheader
        g.setColor(new Color(24, 34, 52));
        g.fillRect(bkX, bkY + 54, bkW, 28);
        g.setColor(cMuted);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.drawString("KEY", bkX + 16, bkY + 72);
        g.drawString("FIELD", bkX + 65, bkY + 72);
        g.drawString("DATA TYPE", bkX + 205, bkY + 72);

        // Row 1: book_id
        g.setColor(cCardAlt);
        g.fillRect(bkX, bkY + 82, bkW, 34);
        drawBadge(g, "PK", bkX + 16, bkY + 91, 28, 16, cWarning);
        g.setColor(cText);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.drawString("book_id", bkX + 65, bkY + 103);
        g.setColor(cMuted);
        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        g.drawString("VARCHAR(20)", bkX + 205, bkY + 103);

        g.setColor(cBorder);
        g.drawLine(bkX, bkY + 116, bkX + bkW, bkY + 116);

        // Row 2: title
        g.setColor(cCard);
        g.fillRect(bkX, bkY + 116, bkW, 34);
        g.setColor(cText);
        g.drawString("title", bkX + 65, bkY + 137);
        g.setColor(cMuted);
        g.drawString("VARCHAR(150)", bkX + 205, bkY + 137);

        g.setColor(cBorder);
        g.drawLine(bkX, bkY + 150, bkX + bkW, bkY + 150);

        // Row 3: author
        g.setColor(cCardAlt);
        g.fillRect(bkX, bkY + 150, bkW, 34);
        g.setColor(cText);
        g.drawString("author", bkX + 65, bkY + 171);
        g.setColor(cMuted);
        g.drawString("VARCHAR(100)", bkX + 205, bkY + 171);

        g.setColor(cBorder);
        g.drawLine(bkX, bkY + 184, bkX + bkW, bkY + 184);

        // Row 4: total_copies
        g.setColor(cCard);
        g.fillRect(bkX, bkY + 184, bkW, 34);
        g.setColor(cText);
        g.drawString("total_copies", bkX + 65, bkY + 205);
        g.setColor(cMuted);
        g.drawString("INT (>= 0)", bkX + 205, bkY + 205);

        g.setColor(cBorder);
        g.drawLine(bkX, bkY + 218, bkX + bkW, bkY + 218);

        // Row 5: available_copies
        g.setColor(cCardAlt);
        g.fillRect(bkX, bkY + 218, bkW, 34);
        g.setColor(cText);
        g.drawString("available_copies", bkX + 65, bkY + 239);
        g.setColor(cMuted);
        g.drawString("INT (<= total)", bkX + 205, bkY + 239);

        // Footer constraints
        g.setColor(cBorder);
        g.drawLine(bkX, bkY + 252, bkX + bkW, bkY + 252);
        g.setColor(cMuted);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.drawString("• chk_total_copies: CHECK (total_copies >= 0)", bkX + 16, bkY + 278);
        g.drawString("• chk_available_copies: CHECK (available_copies >= 0)", bkX + 16, bkY + 300);
        g.setColor(cPrimary);
        g.drawString("• chk_available_limit: available_copies <= total_copies", bkX + 16, bkY + 322);
        g.setColor(cMuted);
        g.drawString("• On issue: copies - 1 | On return: copies + 1", bkX + 16, bkY + 344);

        // ==========================================
        // ENTITY 3: BORROW_RECORDS (Associative Junction Entity)
        // ==========================================
        int brX = 660, brY = 170, brW = 490, brH = 510;
        drawCard(g, brX, brY, brW, brH, cCard, cBorder);

        // Header
        g.setColor(cWarning);
        g.fillRoundRect(brX, brY, brW, 54, 12, 12);
        g.fillRect(brX, brY + 30, brW, 24);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 17));
        g.drawString("BORROW_RECORDS", brX + 20, brY + 28);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("Associative Junction Entity • Circulation Audit Logs", brX + 20, brY + 46);

        // Column Subheader
        g.setColor(new Color(24, 34, 52));
        g.fillRect(brX, brY + 54, brW, 28);
        g.setColor(cMuted);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.drawString("KEY", brX + 16, brY + 72);
        g.drawString("FIELD", brX + 65, brY + 72);
        g.drawString("DATA TYPE", brX + 200, brY + 72);
        g.drawString("ATTRIBUTES", brX + 330, brY + 72);

        // Row 1: record_id
        g.setColor(cCardAlt);
        g.fillRect(brX, brY + 82, brW, 34);
        drawBadge(g, "PK", brX + 16, brY + 91, 28, 16, cWarning);
        g.setColor(cText);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.drawString("record_id", brX + 65, brY + 103);
        g.setColor(cMuted);
        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        g.drawString("INT", brX + 200, brY + 103);
        g.drawString("AUTO_INCREMENT", brX + 330, brY + 103);
        g.setColor(cBorder);
        g.drawLine(brX, brY + 116, brX + brW, brY + 116);

        // Row 2: member_id
        g.setColor(cCard);
        g.fillRect(brX, brY + 116, brW, 34);
        drawBadge(g, "FK", brX + 16, brY + 125, 28, 16, cCyan);
        g.setColor(cText);
        g.drawString("member_id", brX + 65, brY + 137);
        g.setColor(cMuted);
        g.drawString("VARCHAR(20)", brX + 200, brY + 137);
        g.drawString("-> members.id", brX + 330, brY + 137);
        g.setColor(cBorder);
        g.drawLine(brX, brY + 150, brX + brW, brY + 150);

        // Row 3: book_id
        g.setColor(cCardAlt);
        g.fillRect(brX, brY + 150, brW, 34);
        drawBadge(g, "FK", brX + 16, brY + 159, 28, 16, cCyan);
        g.setColor(cText);
        g.drawString("book_id", brX + 65, brY + 171);
        g.setColor(cMuted);
        g.drawString("VARCHAR(20)", brX + 200, brY + 171);
        g.drawString("-> books.id", brX + 330, brY + 171);
        g.setColor(cBorder);
        g.drawLine(brX, brY + 184, brX + brW, brY + 184);

        // Row 4: issue_date
        g.setColor(cCard);
        g.fillRect(brX, brY + 184, brW, 34);
        g.setColor(cText);
        g.drawString("issue_date", brX + 65, brY + 205);
        g.setColor(cMuted);
        g.drawString("DATE", brX + 200, brY + 205);
        g.drawString("NOT NULL", brX + 330, brY + 205);
        g.setColor(cBorder);
        g.drawLine(brX, brY + 218, brX + brW, brY + 218);

        // Row 5: return_date
        g.setColor(cCardAlt);
        g.fillRect(brX, brY + 218, brW, 34);
        drawBadge(g, "NULL", brX + 16, brY + 227, 36, 16, cPurple);
        g.setColor(cText);
        g.drawString("return_date", brX + 65, brY + 239);
        g.setColor(cMuted);
        g.drawString("DATE", brX + 200, brY + 239);
        g.setColor(cWarning);
        g.drawString("NULL = Active Loan", brX + 330, brY + 239);
        g.setColor(cBorder);
        g.drawLine(brX, brY + 252, brX + brW, brY + 252);

        // Row 6: days_kept
        g.setColor(cCard);
        g.fillRect(brX, brY + 252, brW, 34);
        g.setColor(cText);
        g.drawString("days_kept", brX + 65, brY + 273);
        g.setColor(cMuted);
        g.drawString("INT", brX + 200, brY + 273);
        g.drawString("DEFAULT 0, >= 0", brX + 330, brY + 273);
        g.setColor(cBorder);
        g.drawLine(brX, brY + 286, brX + brW, brY + 286);

        // Row 7: fine
        g.setColor(cCardAlt);
        g.fillRect(brX, brY + 286, brW, 34);
        g.setColor(cText);
        g.drawString("fine", brX + 65, brY + 307);
        g.setColor(cMuted);
        g.drawString("DECIMAL(10,2)", brX + 200, brY + 307);
        g.drawString("DEFAULT 0.00, >= 0", brX + 330, brY + 307);

        // Footer constraints
        g.setColor(cBorder);
        g.drawLine(brX, brY + 320, brX + brW, brY + 320);
        g.setColor(cMuted);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.drawString("• Foreign Keys: fk_borrow_member (member_id), fk_borrow_book (book_id)", brX + 16, brY + 348);
        g.setColor(cWarning);
        g.drawString("• Circulation Policy: 7 Days Free grace period, ₹2.00/day overdue fine", brX + 16, brY + 372);
        g.setColor(cCyan);
        g.drawString("• Shared Formula: fine = max(0, days_kept - 7) * 2.0 (e.g. 12 days = ₹10.00)", brX + 16, brY + 396);
        g.setColor(cMuted);
        g.drawString("• Concurrency Safety: Row locking with SELECT ... FOR UPDATE & ACID Rollback", brX + 16, brY + 420);
        g.drawString("• Resolves M:N relationship between MEMBERS and BOOKS as an associative entity", brX + 16, brY + 444);
        g.drawString("• Static Lifetime Issued Counter: Synchronized with COUNT(*) on system startup", brX + 16, brY + 468);

        // ==========================================
        // RELATIONSHIP CONNECTORS & CARDINALITIES
        // ==========================================

        // Connector 1: MEMBERS (1) -> BORROW_RECORDS (0..*)
        g.setColor(cSuccess);
        g.setStroke(new BasicStroke(2.5f));
        g.drawLine(400, 270, 520, 270);
        g.drawLine(520, 270, 520, 310);
        g.drawLine(520, 310, 660, 310);

        // 1 on Members side
        g.drawLine(412, 260, 412, 280);
        g.drawLine(420, 260, 420, 280);
        g.setColor(cText);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.drawString("1", 412, 252);

        // 0..* Crow's foot on Borrow Records side
        g.setColor(cSuccess);
        g.drawOval(625, 305, 10, 10); // optional 0
        g.drawLine(660, 310, 642, 298);
        g.drawLine(660, 310, 642, 322);
        g.setColor(cText);
        g.drawString("0..*", 620, 294);

        // Relationship badge 1
        g.setColor(new Color(24, 34, 52));
        g.fillRoundRect(460, 275, 120, 24, 6, 6);
        g.setColor(cSuccess);
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(460, 275, 120, 24, 6, 6);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.drawString("places / borrows", 472, 291);

        // Connector 2: BOOKS (1) -> BORROW_RECORDS (0..*)
        g.setColor(cPrimary);
        g.setStroke(new BasicStroke(2.5f));
        g.drawLine(400, 560, 520, 560);
        g.drawLine(520, 560, 520, 420);
        g.drawLine(520, 420, 660, 420);

        // 1 on Books side
        g.drawLine(412, 550, 412, 570);
        g.drawLine(420, 550, 420, 570);
        g.setColor(cText);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.drawString("1", 412, 542);

        // 0..* Crow's foot on Borrow Records side
        g.setColor(cPrimary);
        g.drawOval(625, 415, 10, 10); // optional 0
        g.drawLine(660, 420, 642, 408);
        g.drawLine(660, 420, 642, 432);
        g.setColor(cText);
        g.drawString("0..*", 620, 404);

        // Relationship badge 2
        g.setColor(new Color(24, 34, 52));
        g.fillRoundRect(465, 475, 110, 24, 6, 6);
        g.setColor(cPrimary);
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(465, 475, 110, 24, 6, 6);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.drawString("circulates in", 485, 491);

        // ==========================================
        // LEGEND BOX (Bottom Right)
        // ==========================================
        int legX = 660, legY = 705, legW = 490, legH = 95;
        g.setColor(new Color(24, 34, 52));
        g.fillRoundRect(legX, legY, legW, legH, 10, 10);
        g.setColor(cBorder);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(legX, legY, legW, legH, 10, 10);

        g.setColor(cText);
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.drawString("Crow's Foot Cardinality & Notation Guide", legX + 20, legY + 28);

        g.setColor(cMuted);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("• 1 = Exactly One (Mandatory)  |  0..* = Zero or Many (Optional Many)", legX + 20, legY + 52);
        g.drawString("• [PK] Primary Key  |  [FK] Foreign Key  |  [NULL] Nullable when active loan", legX + 20, legY + 74);

        g.dispose();

        // Write output PNG
        try {
            File outFile = new File("docs/ER_Diagram.png");
            ImageIO.write(image, "png", outFile);
            System.out.println("✓ High-resolution ER Diagram PNG generated successfully at: " + outFile.getAbsolutePath());
            System.out.println("  Dimensions: " + width + "x" + height + " (Retina 300 DPI)");
        } catch (Exception e) {
            System.err.println("Failed to write ER_Diagram.png: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void drawCard(Graphics2D g, int x, int y, int w, int h, Color fill, Color stroke) {
        g.setColor(fill);
        g.fillRoundRect(x, y, w, h, 14, 14);
        g.setColor(stroke);
        g.setStroke(new BasicStroke(2.0f));
        g.drawRoundRect(x, y, w, h, 14, 14);
    }

    private static void drawBadge(Graphics2D g, String text, int x, int y, int w, int h, Color color) {
        Color tint = new Color(color.getRed(), color.getGreen(), color.getBlue(), 35);
        g.setColor(tint);
        g.fillRoundRect(x, y, w, h, 4, 4);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(x, y, w, h, 4, 4);
        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g.drawString(text, x + (text.length() <= 2 ? 6 : 4), y + 12);
    }
}
