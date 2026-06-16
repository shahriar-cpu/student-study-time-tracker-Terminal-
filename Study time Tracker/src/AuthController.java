import java.util.Scanner;

/**
 * AuthController — handles login and registration in the terminal.
 */
public class AuthController {

    private final DataStore store;
    private final Scanner   sc;

    public AuthController(DataStore store, Scanner sc) {
        this.store = store;
        this.sc    = sc;
    }

    /**
     * Interactive auth loop. Returns the logged-in User, or null to quit.
     */
    public User run() {
        while (true) {
            printBanner();
            System.out.println(Terminal.accent("  [1]") + " Log In");
            System.out.println(Terminal.accent("  [2]") + " Create Account");
            System.out.println(Terminal.muted("  [0]") + " Quit");
            System.out.println();
            String choice = Terminal.prompt(sc, "Choice:");

            switch (choice) {
                case "1" -> {
                    User u = login();
                    if (u != null) return u;
                }
                case "2" -> register();
                case "0" -> { return null; }
                default  -> Terminal.error("Invalid option.");
            }
        }
    }

    private User login() {
        Terminal.header("Log In");
        String username = Terminal.prompt(sc, "Username:");
        String password = Terminal.promptPassword(sc, "Password:");
        User u = store.loginUser(username, password);
        if (u == null) {
            Terminal.error("Invalid username or password.");
            return null;
        }
        Terminal.ok("Welcome back, " + u.getUsername() + "!");
        return u;
    }

    private void register() {
        Terminal.header("Create Account");
        String username = Terminal.prompt(sc, "Username:");
        if (username.isEmpty()) { Terminal.error("Username required."); return; }
        String email    = Terminal.prompt(sc, "Email:");
        if (email.isEmpty()) { Terminal.error("Email required."); return; }
        String password = Terminal.promptPassword(sc, "Password (min 6 chars):");
        if (password.length() < 6) { Terminal.error("Password must be at least 6 characters."); return; }
        String confirm  = Terminal.promptPassword(sc, "Confirm Password:");
        if (!password.equals(confirm)) { Terminal.error("Passwords do not match."); return; }

        boolean ok = store.registerUser(username, password, email);
        if (ok) Terminal.ok("Account created! You can now log in.");
        else    Terminal.error("Username already taken. Please choose another.");
    }

    private void printBanner() {
        Terminal.clear();
        System.out.println();
        System.out.println(Terminal.FG_CYAN + Terminal.BOLD +
            "  ╔═══════════════════════════════════╗" + Terminal.RESET);
        System.out.println(Terminal.FG_CYAN + Terminal.BOLD +
            "  ║          STUDYTRACK CLI           ║" + Terminal.RESET);
        System.out.println(Terminal.FG_CYAN + Terminal.BOLD +
            "  ║    Stop guessing. Start tracking. ║" + Terminal.RESET);
        System.out.println(Terminal.FG_CYAN + Terminal.BOLD +
            "  ╚═══════════════════════════════════╝" + Terminal.RESET);
        System.out.println();
    }
}
