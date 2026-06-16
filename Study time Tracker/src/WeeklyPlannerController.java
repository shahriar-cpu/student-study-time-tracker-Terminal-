import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.*;

/**
 * WeeklyPlannerController — text-based weekly grid view.
 * Shows Mon–Sun with class blocks and study sessions side-by-side.
 */
public class WeeklyPlannerController {

    private final DataStore store;
    private final User      user;
    private final Scanner   sc;

    public WeeklyPlannerController(DataStore store, User user, Scanner sc) {
        this.store = store;
        this.user  = user;
        this.sc    = sc;
    }

    public void run() {
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        while (true) {
            Terminal.clear();
            Terminal.header("Weekly Planner");
            showWeek(weekStart);
            System.out.println();
            System.out.println(Terminal.accent("  [p]") + " Previous week");
            System.out.println(Terminal.accent("  [n]") + " Next week");
            System.out.println(Terminal.accent("  [t]") + " This week");
            System.out.println(Terminal.muted("  [0]") + " Back");
            System.out.println();
            String choice = Terminal.prompt(sc, "Choice:");
            switch (choice.toLowerCase()) {
                case "p"  -> weekStart = weekStart.minusWeeks(1);
                case "n"  -> weekStart = weekStart.plusWeeks(1);
                case "t"  -> weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
                case "0"  -> { return; }
                default   -> Terminal.error("Invalid option.");
            }
        }
    }

    private void showWeek(LocalDate weekStart) {
        LocalDate today = LocalDate.now();
        LocalDate weekEnd = weekStart.plusDays(6);

        // Week header
        String weekLabel = weekStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) +
            " " + weekStart.getDayOfMonth() + " – " +
            weekEnd.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) +
            " " + weekEnd.getDayOfMonth() + ", " + weekStart.getYear();
        System.out.println("  " + Terminal.accent(weekLabel));
        System.out.println();

        // Day headers
        DayOfWeek[] days = DayOfWeek.values(); // MON..SUN
        System.out.print("  ");
        for (int d = 0; d < 7; d++) {
            LocalDate date = weekStart.plusDays(d);
            boolean isToday = date.equals(today);
            String dayLabel = days[d].getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase()
                + " " + date.getDayOfMonth();
            String cell = isToday ? Terminal.accent(dayLabel) : Terminal.muted(dayLabel);
            System.out.print(Terminal.padRight(cell, 14));
        }
        System.out.println();
        Terminal.separator(100);

        // Collect all entries per day
        List<TimetableEntry> allTT = store.getTimetableForUser(user.getUsername());
        List<StudySession>   allSS = store.getSessionsForUser(user.getUsername());

        // Build lists per day column
        List<List<String>> columns = new ArrayList<>();
        for (int d = 0; d < 7; d++) {
            LocalDate date = weekStart.plusDays(d);
            DayOfWeek dow  = days[d];
            List<String> items = new ArrayList<>();

            allTT.stream()
                .filter(e -> e.getDayOfWeek() == dow)
                .sorted(Comparator.comparing(TimetableEntry::getStartTime))
                .forEach(e -> items.add(Terminal.FG_CYAN + "● " + e.getSubject() + Terminal.RESET +
                    "\n    " + Terminal.muted(e.getStartTime() + "–" + e.getEndTime())));

            allSS.stream()
                .filter(s -> s.getDate().equals(date))
                .sorted(Comparator.comparing(StudySession::getStartTime))
                .forEach(s -> items.add(Terminal.FG_BLUE + "◈ " + s.getSubject() + Terminal.RESET +
                    "\n    " + Terminal.muted(s.getDurationMinutes() + " min")));

            columns.add(items);
        }

        // Determine max rows
        int maxRows = columns.stream().mapToInt(List::size).max().orElse(0);
        if (maxRows == 0) {
            System.out.println("  " + Terminal.muted("No events this week."));
            return;
        }

        // Print rows
        for (int row = 0; row < maxRows; row++) {
            // Each entry may have 2 display lines (name + time)
            for (int line = 0; line < 2; line++) {
                System.out.print("  ");
                for (int d = 0; d < 7; d++) {
                    List<String> col = columns.get(d);
                    String cell = "";
                    if (row < col.size()) {
                        String[] parts = col.get(row).split("\n", 2);
                        cell = (line < parts.length) ? parts[line].trim() : "";
                    }
                    System.out.print(Terminal.padRight(cell, 14));
                }
                System.out.println();
            }
            System.out.println();
        }

        // Weekly totals
        int totalMin = allSS.stream()
            .filter(s -> !s.getDate().isBefore(weekStart) && !s.getDate().isAfter(weekEnd))
            .mapToInt(StudySession::getDurationMinutes).sum();
        Terminal.separator(100);
        System.out.println("  " + Terminal.label("Week total study: ") + Terminal.bold(Terminal.formatMin(totalMin)));
    }
}
