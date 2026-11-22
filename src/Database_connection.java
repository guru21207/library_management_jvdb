import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Centralized JDBC Database Connection Manager for BookNest.
 *
 * Implements secure credential loading:
 * 1. Checks environment variables (BOOKNEST_DB_URL, BOOKNEST_DB_USER, BOOKNEST_DB_PASSWORD)
 * 2. Checks local 'db.properties' configuration file (excluded from version control)
 * 3. Falls back to default local settings if neither is supplied
 *
 * Never prints or exposes database passwords in application logs or GUI labels.
 */
public class Database_connection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/booknest";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static String cachedUrl = null;
    private static String cachedUser = null;
    private static String cachedPassword = null;

    static {
        loadConfiguration();
    }

    /**
     * Loads database configuration safely from environment variables or local db.properties.
     */
    private static synchronized void loadConfiguration() {
        // Default values
        String url = DEFAULT_URL;
        String user = DEFAULT_USER;
        String password = DEFAULT_PASSWORD;

        // 1. Try loading from local db.properties file
        File configFile = new File("db.properties");
        if (configFile.exists() && configFile.isFile()) {
            try (InputStream input = new FileInputStream(configFile)) {
                Properties props = new Properties();
                props.load(input);
                if (props.containsKey("db.url")) url = props.getProperty("db.url").trim();
                if (props.containsKey("db.user")) user = props.getProperty("db.user").trim();
                if (props.containsKey("db.password")) password = props.getProperty("db.password").trim();
            } catch (Exception e) {
                System.err.println("Notice: Could not read local db.properties file: " + e.getMessage());
            }
        }

        // 2. Environment variables override local properties if set
        String envUrl = System.getenv("BOOKNEST_DB_URL");
        if (envUrl == null || envUrl.isEmpty()) envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isEmpty()) url = envUrl;

        String envUser = System.getenv("BOOKNEST_DB_USER");
        if (envUser == null || envUser.isEmpty()) envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.isEmpty()) user = envUser;

        String envPass = System.getenv("BOOKNEST_DB_PASSWORD");
        if (envPass == null) envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) password = envPass;

        cachedUrl = url;
        cachedUser = user;
        cachedPassword = password;
    }

    /**
     * Returns a new active JDBC Connection to the MySQL database.
     *
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (cachedUrl == null) {
            loadConfiguration();
        }
        return DriverManager.getConnection(cachedUrl, cachedUser, cachedPassword);
    }

    /**
     * Non-destructive connectivity probe used for GUI health indicators and startup checks.
     *
     * @return true if database connection can be established, false otherwise.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Returns a sanitized connection summary suitable for GUI display or logs.
     * Strictly avoids exposing passwords.
     *
     * @return Sanitized connection info string
     */
    public static String getConnectionInfo() {
        if (cachedUrl == null) {
            loadConfiguration();
        }
        String host = "localhost:3306";
        String db = "booknest";
        try {
            if (cachedUrl.contains("//")) {
                String sub = cachedUrl.substring(cachedUrl.indexOf("//") + 2);
                host = sub;
            }
        } catch (Exception ignored) {
        }
        return host + " (User: " + cachedUser + ")";
    }
}
