import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.*;

/**
 * DayViewController — shows today's (or any day's) schedule:
 * timetable classes, study sessions, and deadlines due.
 */
public class DayViewController {

    private final DataStore store;
    private final User      user;
    private final Scanner   sc;

    public DayViewController(DataStore store, User user, Scanner sc) {
        this.store = store;
        this.user  = user;
        this.sc    = sc;
    }

    public void run() {
        LocalDate viewDate = LocalDate.now();
        while (true) {
            Terminal.clear();
            Terminal.header("Day View — " + formatDate(viewDate));
            showDay(viewDate);
            System.out.println();
            System.out.println(Terminal.accent("  [p]") + " Previous day");
            System.out.println(Terminal.accent("  [n]") + " Next day");
            System.out.println(Terminal.accent("  [t]") + " Today");
            System.out.println(Terminal.accent("  [g]") + " Go to date");
            System.out.println(Terminal.accent("  [a]") + " Add manual study session");
            System.out.println(Terminal.muted("  [0]") + " Back");
            System.out.println();
            String choice = Terminal.prompt(sc, "Choice:");
            switch (choice.toLowerCase()) {
                case "p"  -> viewDate = viewDate.minusDays(1);
                case "n"  -> viewDate = viewDate.plusDays(1);
                case "t"  -> viewDate = LocalDate.now();
                case "g"  -> {
                    String d = Terminal.prompt(sc, "Enter date (YYYY-MM-DD):");
                    try { viewDate = LocalDate.parse(d); }
                    catch (Exception e) { Terminal.error("Invalid date format."); }
                }
                case "a"  -> { addSession(viewDate); }
                case "0"  -> { return; }
                default   -> Terminal.error("Invalid option.");
            }
        }
    }

    private void showDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        boolean isToday = date.equals(LocalDate.now());

        // ── Timetable classes ─────────────────────────────────────────────────
        List<TimetableEntry> classes = store.getTimetableForUser(user.getUsername())
            .stream().filter(e -> e.getDayOfWeek() == dow)
            .sorted(Comparator.comparing(TimetableEntry::getStartTime))
            .toList();

        if (!classes.isEmpty()) {
            System.out.println(Terminal.bold("  📚 Classes"));
            System.out.println();
            for (TimetableEntry c : classes) {
                String loc = c.getLocation().isBlank() ? "" : "  @ " + c.getLocation();
                System.out.println("  " + Terminal.FG_CYAN + "▌" + Terminal.RESET +
                    "  " + Terminal.bold(c.getSubject()) +
                    "  " + Terminal.muted(c.getStartTime() + " – " + c.getEndTime()) +
                    "  " + Terminal.muted(c.getDurationMinutes() + " min") + loc);
            }
            System.out.println();
        }

        // ── Study sessions ────────────────────────────────────────────────────
        List<StudySession> daySessions = store.getSessionsForUser(user.getUsername())
            .stream().filter(s -> s.getDate().equals(date))
            .sorted(Comparator.comparing(StudySession::getStartTime))
            .toList();

        if (!daySessions.isEmpty()) {
            System.out.println(Terminal.bold("  ⏱ Study Sessions"));
            System.out.println();
            for (StudySession s : daySessions) {
                System.out.println("  " + Terminal.FG_BLUE + "▌" + Terminal.RESET +
                    "  " + Terminal.accent(s.getSubject()) +
                    "  " + Terminal.muted(s.getStartTime().toString().substring(0, 5)) +
                    "  " + Terminal.bold(s.getDurationMinutes() + " min") +
                    "  " + Terminal.muted("[" + s.getType() + "]"));
            }
            int totalMin = daySessions.stream().mapToInt(StudySession::getDurationMinutes).sum();
            System.out.println();
            System.out.println("  " + Terminal.success("Total study: " + Terminal.formatMin(totalMin)));
            System.out.println();
        }

        // ── Deadlines due today ───────────────────────────────────────────────
        List<Deadline> dueToday = store.getDeadlinesForUser(user.getUsername())
            .stream().filter(d -> d.getDueDate().equals(date) && !d.isCompleted())
            .toList();

        if (!dueToday.isEmpty()) {
            System.out.println(Terminal.bold("  ⚠ Due Today"));
            System.out.println();
            for (Deadline d : dueToday) {
                System.out.println("  " + Terminal.FG_YELLOW + "▌" + Terminal.RESET +
                    "  " + Terminal.warn(d.getTitle()) +
                    "  " + Terminal.muted("[" + d.getType() + "]") +
                    "  " + Terminal.label(d.getSubject()));
            }
            System.out.println();
        }

        if (classes.isEmpty() && daySessions.isEmpty() && dueToday.isEmpty()) {
            Terminal.info("Nothing scheduled for this day. Enjoy your break! 🎉");
        }
    }

    private void addSession(LocalDate date) {
        Terminal.header("Add Study Session");
        String subject = Terminal.prompt(sc, "Subject:");
        if (subject.isEmpty()) { Terminal.error("Subject required."); return; }
        String startStr = Terminal.prompt(sc, "Start time (HH:MM):");
        int duration    = Terminal.promptInt(sc, "Duration (minutes):", 1, 600);

        java.time.LocalTime startTime;
        try { startTime = java.time.LocalTime.parse(startStr); }
        catch (Exception e) { Terminal.error("Invalid time format."); return; }

        StudySession session = new StudySession(user.getUsername(), subject,
            duration, StudySession.Type.STUDY, date, startTime, "");
        store.addSession(session);
        user.recordStudy(duration);
        store.saveUser(user);
        Terminal.ok("Session logged: " + subject + " — " + duration + " min.");
        Terminal.pause(sc);
    }

    private String formatDate(LocalDate d) {
        boolean isToday = d.equals(LocalDate.now());
        String dow = d.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String mon = d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return dow + ", " + mon + " " + d.getDayOfMonth() + ", " + d.getYear() +
               (isToday ? "  (Today)" : "");
    }
}
