import java.util.Scanner;

/**
 * Dashboard — main menu hub after login.
 * Mirrors the SideNav of the GUI version.
 */
public class Dashboard {

    private final DataStore store;
    private User            user;
    private final Scanner   sc;

    public Dashboard(DataStore store, User user, Scanner sc) {
        this.store = store;
        this.user  = user;
        this.sc    = sc;
    }

    public void run() {
        while (true) {
            Terminal.clear();
            printSideNav();

            String choice = Terminal.prompt(sc, "Choice:");
            switch (choice) {
                case "1" -> new WeeklyPlannerController(store, user, sc).run();
                case "2" -> new DayViewController(store, user, sc).run();
                case "3" -> new FocusTimerController(store, user, sc).run();
                case "4" -> new StatsController(store, user, sc).run();
                case "5" -> new FriendsController(store, user, sc).run();
                case "6" -> new DeadlinesController(store, user, sc).run();
                case "7" -> new TimetableController(store, user, sc).run();
                case "8" -> new SettingsController(store, user, sc).run();
                case "0" -> {
                    Terminal.ok("Logged out. Goodbye!");
                    return;
                }
                default -> Terminal.error("Invalid option.");
            }
        }
    }

    private void printSideNav() {
        System.out.println();
        System.out.println(Terminal.FG_CYAN + Terminal.BOLD +
            "  ╔══════════════════════════════════════╗" + Terminal.RESET);
        System.out.println(Terminal.FG_CYAN + Terminal.BOLD +
            "  ║            StudyTrack CLI            ║" + Terminal.RESET);
        System.out.println(Terminal.FG_CYAN +
            "  ║  " + Terminal.RESET + Terminal.FG_GRAY + "Stop guessing. Start tracking.   " +
            Terminal.FG_CYAN + "  ║" + Terminal.RESET);
        System.out.println(Terminal.FG_CYAN + Terminal.BOLD +
            "  ╚══════════════════════════════════════╝" + Terminal.RESET);
        System.out.println();

        // User badge
        System.out.println("  " + Terminal.accent("[" + user.getAvatarInitials() + "]") +
            "  " + Terminal.bold(user.getUsername()) +
            "  " + Terminal.muted("Lv " + user.getLevel() + " · " + user.getStreak() + " 🔥"));
        System.out.println("  " + Terminal.progressBar(user.getXp() % 100, 100, 20) +
            "  " + Terminal.muted(user.getXp() + " XP"));
        System.out.println();
        Terminal.separator(44);
        System.out.println();

        System.out.println(Terminal.accent("  [1]") + "  📅  Week View");
        System.out.println(Terminal.accent("  [2]") + "  📆  Day View");
        System.out.println(Terminal.accent("  [3]") + "  ⏱   Focus Timer");
        System.out.println(Terminal.accent("  [4]") + "  📊  Statistics");
        System.out.println(Terminal.accent("  [5]") + "  👥  Friends Leaderboard");
        System.out.println(Terminal.accent("  [6]") + "  ⚠   Deadlines");
        System.out.println(Terminal.accent("  [7]") + "  📚  Timetable");
        System.out.println(Terminal.accent("  [8]") + "  ⚙   Settings");
        System.out.println(Terminal.muted("  [0]") + "  ⏻   Log Out");
        System.out.println();
    }
}
