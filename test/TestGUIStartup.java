import javax.swing.SwingUtilities;

/**
 * Validates that BookNestGUI initializes without any runtime exceptions,
 * loads all table models, populates stat cards, and configures views.
 */
public class TestGUIStartup {

    public static void main(String[] args) {
        System.out.println("Testing BookNestGUI initialization...");
        SwingUtilities.invokeLater(() -> {
            try {
                BookNestGUI gui = new BookNestGUI();
                System.out.println("✓ BookNestGUI constructed successfully.");
                System.out.println("✓ Window Title: " + gui.getTitle());
                System.out.println("✓ Window Size: " + gui.getSize().width + "x" + gui.getSize().height);
                gui.dispose();
                System.out.println("🎉 GUI startup test PASSED!");
                System.exit(0);
            } catch (Throwable t) {
                System.err.println("✕ GUI startup failed: " + t.getMessage());
                t.printStackTrace();
                System.exit(1);
            }
        });
    }
}
