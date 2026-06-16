import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * SettingsController — profile overview, password change, CSV export.
 */
public class SettingsController {

    private final DataStore store;
    private final User      user;
    private final Scanner   sc;

    public SettingsController(DataStore store, User user, Scanner sc) {
        this.store = store;
        this.user  = user;
        this.sc    = sc;
    }

    public void run() {
        while (true) {
            Terminal.clear();
            Terminal.header("Settings");
            showProfile();
            System.out.println();
            System.out.println(Terminal.accent("  [1]") + " Change Password");
            System.out.println(Terminal.accent("  [2]") + " Export Sessions to CSV");
            System.out.println(Terminal.muted("  [0]") + " Back");
            System.out.println();
            String choice = Terminal.prompt(sc, "Choice:");
            switch (choice) {
                case "1" -> changePassword();
                case "2" -> exportCSV();
                case "0" -> { return; }
                default  -> Terminal.error("Invalid option.");
            }
        }
    }

    private void showProfile() {
        int W = 52;
        Terminal.box("Profile — " + user.getAvatarInitials(), W);
        Terminal.row(Terminal.label("Username:   ") + Terminal.bold(user.getUsername()), W);
        Terminal.row(Terminal.label("Email:      ") + Terminal.bold(user.getEmail()), W);
        Terminal.row(Terminal.label("Role:       ") + Terminal.muted(user.getRole()), W);
        Terminal.divider(W);
        Terminal.row(Terminal.label("Level:      ") + Terminal.bold(Terminal.FG_GREEN + "Lv " + user.getLevel() + Terminal.RESET), W);
        Terminal.row(Terminal.label("Total XP:   ") + Terminal.bold(user.getXp() + " XP"), W);
        Terminal.row(Terminal.label("To next Lv: ") + Terminal.muted(user.getXpToNextLevel() + " XP"), W);
        Terminal.row(Terminal.label("Streak:     ") + Terminal.bold(user.getStreak() + " 🔥"), W);
        Terminal.row(Terminal.label("XP bar:     ") + Terminal.progressBar(user.getXp() % 100, 100, 18), W);
        Terminal.boxEnd(W);
    }

    private void changePassword() {
        Terminal.header("Change Password");
        String oldPw = Terminal.promptPassword(sc, "Current password:");
        if (!oldPw.equals(user.getPassword())) {
            Terminal.error("Current password is incorrect.");
            Terminal.pause(sc);
            return;
        }
        String newPw = Terminal.promptPassword(sc, "New password (min 6 chars):");
        if (newPw.length() < 6) {
            Terminal.error("Password must be at least 6 characters.");
            Terminal.pause(sc);
            return;
        }
        String confirm = Terminal.promptPassword(sc, "Confirm new password:");
        if (!newPw.equals(confirm)) {
            Terminal.error("Passwords do not match.");
            Terminal.pause(sc);
            return;
        }
        user.setPassword(newPw);
        store.saveUser(user);
        Terminal.ok("Password updated successfully!");
        Terminal.pause(sc);
    }

    private void exportCSV() {
        Terminal.header("Export Sessions");
        String path = Terminal.prompt(sc, "Save to file (e.g. export.csv):");
        if (path.isEmpty()) path = "studytrack_export.csv";
        List<StudySession> sessions = store.getSessionsForUser(user.getUsername());
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("Subject,Duration(min),Type,Date,StartTime,Notes");
            for (StudySession s : sessions)
                pw.println(s.getSubject() + "," + s.getDurationMinutes() + "," +
                    s.getType() + "," + s.getDate() + "," + s.getStartTime() + "," +
                    s.getNotes().replace(",", ";"));
            Terminal.ok("Exported " + sessions.size() + " sessions to " + path);
        } catch (IOException e) {
            Terminal.error("Export failed: " + e.getMessage());
        }
        Terminal.pause(sc);
    }
}
