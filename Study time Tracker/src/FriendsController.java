import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FriendsController — leaderboard comparing all registered users by study hours.
 */
public class FriendsController {

    private final DataStore store;
    private final User      user;
    private final Scanner   sc;

    public FriendsController(DataStore store, User user, Scanner sc) {
        this.store = store;
        this.user  = user;
        this.sc    = sc;
    }

    public void run() {
        while (true) {
            Terminal.clear();
            Terminal.header("Friends Leaderboard 🏆");
            System.out.println(Terminal.accent("  [1]") + " Last 7 days");
            System.out.println(Terminal.accent("  [2]") + " Last 14 days");
            System.out.println(Terminal.accent("  [3]") + " Last 30 days");
            System.out.println(Terminal.accent("  [4]") + " Last 90 days");
            System.out.println(Terminal.muted("  [0]") + " Back");
            System.out.println();
            String choice = Terminal.prompt(sc, "Choice:");
            switch (choice) {
                case "1" -> { showLeaderboard(7);  Terminal.pause(sc); }
                case "2" -> { showLeaderboard(14); Terminal.pause(sc); }
                case "3" -> { showLeaderboard(30); Terminal.pause(sc); }
                case "4" -> { showLeaderboard(90); Terminal.pause(sc); }
                case "0" -> { return; }
                default  -> Terminal.error("Invalid option.");
            }
        }
    }

    private void showLeaderboard(int days) {
        Terminal.clear();
        Terminal.header("Leaderboard — Last " + days + " Days");

        LocalDate today = LocalDate.now();
        LocalDate from  = today.minusDays(days - 1);

        Collection<User> allUsers = store.getAllUsers();
        List<UserScore> scores = new ArrayList<>();
        for (User u : allUsers) {
            int min = store.getSessionsForUser(u.getUsername()).stream()
                .filter(s -> !s.getDate().isBefore(from))
                .mapToInt(StudySession::getDurationMinutes).sum();
            scores.add(new UserScore(u, min));
        }
        scores.sort(Comparator.comparingInt(UserScore::minutes).reversed());

        String[] medals = {"🥇", "🥈", "🥉"};
        int W = 62;
        Terminal.box("Rankings", W);

        for (int i = 0; i < scores.size(); i++) {
            UserScore us = scores.get(i);
            boolean isMe = us.user.getUsername().equals(user.getUsername());
            String medal = i < 3 ? medals[i] : String.format("#%d", i + 1);
            int barLen   = scores.get(0).minutes > 0
                ? (int)(20.0 * us.minutes / scores.get(0).minutes) : 0;

            String bar   = Terminal.progressBar(us.minutes,
                scores.isEmpty() || scores.get(0).minutes == 0 ? 1 : scores.get(0).minutes, 20);
            String name  = isMe ? Terminal.accent(us.user.getUsername() + " (you)") : Terminal.bold(us.user.getUsername());
            String meta  = Terminal.muted("Lv" + us.user.getLevel() + " · " + us.user.getStreak() + "🔥");
            String time  = Terminal.bold(Terminal.formatMin(us.minutes));

            String row = String.format("%-4s %-22s %-14s %s  %s",
                medal, name, meta, bar, time);
            Terminal.row(row, W);
        }

        if (scores.isEmpty()) Terminal.row(Terminal.muted("  No users yet."), W);
        Terminal.boxEnd(W);

        // My rank summary
        int myRank = scores.stream()
            .map(UserScore::user)
            .map(User::getUsername)
            .toList().indexOf(user.getUsername()) + 1;
        System.out.println();
        System.out.println("  " + Terminal.label("Your rank: ") + Terminal.accent("#" + myRank + " of " + scores.size()));
    }

    private record UserScore(User user, int minutes) {}
}
