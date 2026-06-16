import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * StatsController — study analytics: bar chart, subject breakdown, totals.
 */
public class StatsController {

    private final DataStore store;
    private final User      user;
    private final java.util.Scanner sc;

    public StatsController(DataStore store, User user, java.util.Scanner sc) {
        this.store = store;
        this.user  = user;
        this.sc    = sc;
    }

    public void run() {
        while (true) {
            Terminal.clear();
            Terminal.header("Statistics & Progress");
            int[] options = {7, 14, 30, 90};
            System.out.println(Terminal.accent("  [1]") + " Last 7 days");
            System.out.println(Terminal.accent("  [2]") + " Last 14 days");
            System.out.println(Terminal.accent("  [3]") + " Last 30 days");
            System.out.println(Terminal.accent("  [4]") + " Last 90 days");
            System.out.println(Terminal.muted("  [0]") + " Back");
            System.out.println();
            String choice = Terminal.prompt(sc, "Choice:");
            switch (choice) {
                case "1" -> { showStats(7);  Terminal.pause(sc); }
                case "2" -> { showStats(14); Terminal.pause(sc); }
                case "3" -> { showStats(30); Terminal.pause(sc); }
                case "4" -> { showStats(90); Terminal.pause(sc); }
                case "0" -> { return; }
                default  -> Terminal.error("Invalid option.");
            }
        }
    }

    private void showStats(int rangeDays) {
        Terminal.clear();
        Terminal.header("Statistics — Last " + rangeDays + " Days");

        List<StudySession> sessions = store.getSessionsForUser(user.getUsername());
        LocalDate today      = LocalDate.now();
        LocalDate rangeStart = today.minusDays(rangeDays - 1);

        List<StudySession> inRange = sessions.stream()
            .filter(s -> !s.getDate().isBefore(rangeStart))
            .collect(Collectors.toList());

        int totalMin = inRange.stream().mapToInt(StudySession::getDurationMinutes).sum();
        int avgMin   = rangeDays > 0 ? totalMin / rangeDays : 0;

        // ── Summary cards ────────────────────────────────────────────────────
        int W = 60;
        Terminal.box("Summary", W);
        Terminal.row(Terminal.label("Total study time:  ") + Terminal.bold(Terminal.FG_CYAN + Terminal.formatMin(totalMin) + Terminal.RESET), W);
        Terminal.row(Terminal.label("Daily average:     ") + Terminal.bold(avgMin + " min"), W);
        Terminal.row(Terminal.label("Day streak:        ") + Terminal.bold(Terminal.FG_YELLOW + user.getStreak() + " 🔥" + Terminal.RESET), W);
        Terminal.row(Terminal.label("Level:             ") + Terminal.bold(Terminal.FG_GREEN + "Lv " + user.getLevel() + Terminal.RESET), W);
        Terminal.row(Terminal.label("Total XP:          ") + Terminal.bold(user.getXp() + " XP"), W);
        Terminal.row(Terminal.label("XP to next level:  ") + Terminal.bold(user.getXpToNextLevel() + " XP"), W);
        int xpPct = user.getXp() % 100;
        Terminal.row(Terminal.label("XP bar:            ") + Terminal.progressBar(xpPct, 100, 20), W);
        Terminal.boxEnd(W);

        // ── Daily bar chart ───────────────────────────────────────────────────
        System.out.println();
        System.out.println(Terminal.bold("  Study Hours — Daily Chart"));
        System.out.println();

        Map<LocalDate, Integer> dailyMap = new LinkedHashMap<>();
        for (int i = rangeDays - 1; i >= 0; i--)
            dailyMap.put(today.minusDays(i), 0);
        for (StudySession s : inRange)
            dailyMap.merge(s.getDate(), s.getDurationMinutes(), Integer::sum);

        int maxMin = dailyMap.values().stream().mapToInt(Integer::intValue).max().orElse(60);
        if (maxMin == 0) maxMin = 60;

        // Show every Nth day to fit terminal
        int step = rangeDays <= 14 ? 1 : rangeDays <= 30 ? 2 : 5;
        int dayIdx = 0;
        for (Map.Entry<LocalDate, Integer> e : dailyMap.entrySet()) {
            if (dayIdx % step == 0 || dayIdx == rangeDays - 1) {
                boolean isToday = e.getKey().equals(today);
                String dayLabel = rangeDays <= 14
                    ? e.getKey().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                    : e.getKey().getMonthValue() + "/" + e.getKey().getDayOfMonth();
                String colour = isToday ? Terminal.FG_CYAN : Terminal.FG_BLUE;
                int barLen = maxMin > 0 ? (int)(30.0 * e.getValue() / maxMin) : 0;
                String bar = colour + "█".repeat(barLen) + Terminal.RESET +
                             Terminal.FG_GRAY + "░".repeat(30 - barLen) + Terminal.RESET;
                System.out.printf("  %-5s %s %s%n",
                    isToday ? Terminal.accent(dayLabel) : Terminal.muted(dayLabel),
                    bar,
                    Terminal.muted(Terminal.formatMin(e.getValue())));
            }
            dayIdx++;
        }

        // ── Subject breakdown ─────────────────────────────────────────────────
        System.out.println();
        System.out.println(Terminal.bold("  Time by Subject"));
        System.out.println();

        Map<String, Integer> subMap = new LinkedHashMap<>();
        for (StudySession s : inRange)
            subMap.merge(s.getSubject(), s.getDurationMinutes(), Integer::sum);

        if (subMap.isEmpty()) {
            Terminal.info("No sessions in this period yet.");
        } else {
            int maxSubMin = subMap.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            subMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> System.out.println("  " +
                    Terminal.barRow(e.getKey(), e.getValue(), maxSubMin, 24)));
        }

        // ── Recent sessions ───────────────────────────────────────────────────
        System.out.println();
        System.out.println(Terminal.bold("  Recent Sessions (last 10)"));
        System.out.println();
        inRange.stream()
            .sorted(Comparator.comparing(StudySession::getDate).reversed()
                .thenComparing(Comparator.comparing(StudySession::getStartTime).reversed()))
            .limit(10)
            .forEach(s -> System.out.println("  " + Terminal.muted(s.getDate().toString()) +
                "  " + Terminal.accent(s.getSubject()) +
                "  " + Terminal.bold(s.getDurationMinutes() + " min") +
                "  " + Terminal.muted("[" + s.getType() + "]")));
    }
}
