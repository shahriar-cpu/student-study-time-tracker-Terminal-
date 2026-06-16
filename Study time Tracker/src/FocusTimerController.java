import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * FocusTimerController — interactive terminal Pomodoro timer.
 * Counts down in real-time, auto-logs sessions, awards XP.
 */
public class FocusTimerController {

    private final DataStore store;
    private final User      user;
    private final Scanner   sc;

    // Config (minutes)
    private int focusMins      = 25;
    private int shortBreakMins = 5;
    private int longBreakMins  = 15;
    private int pomodoroCount  = 0;

    public FocusTimerController(DataStore store, User user, Scanner sc) {
        this.store = store;
        this.user  = user;
        this.sc    = sc;
    }

    public void run() {
        while (true) {
            Terminal.clear();
            Terminal.header("Focus Timer  🍅");
            printStats();
            System.out.println();
            System.out.println(Terminal.accent("  [1]") + " Start Focus Session (" + focusMins + " min)");
            System.out.println(Terminal.accent("  [2]") + " Start Short Break   (" + shortBreakMins + " min)");
            System.out.println(Terminal.accent("  [3]") + " Start Long Break    (" + longBreakMins + " min)");
            System.out.println(Terminal.accent("  [4]") + " Configure durations");
            System.out.println(Terminal.muted("  [0]") + " Back");
            System.out.println();
            System.out.println(Terminal.muted("  🍅 " + pomodoroCount + " pomodoro" + (pomodoroCount != 1 ? "s" : "") + " this session"));
            System.out.println();
            String choice = Terminal.prompt(sc, "Choice:");
            switch (choice) {
                case "1" -> startFocus();
                case "2" -> startBreak(shortBreakMins, "Short Break");
                case "3" -> startBreak(longBreakMins, "Long Break");
                case "4" -> configure();
                case "0" -> { return; }
                default  -> Terminal.error("Invalid option.");
            }
        }
    }

    private void startFocus() {
        Terminal.clear();
        Terminal.header("Focus Session");

        // Subject selection
        String[] subjects = getSubjectList();
        System.out.println(Terminal.muted("  Available subjects:"));
        for (int i = 0; i < subjects.length; i++)
            System.out.println(Terminal.accent("  [" + (i + 1) + "]") + " " + subjects[i]);
        System.out.println(Terminal.muted("  [0]") + " Enter manually");
        System.out.println();
        String subChoice = Terminal.prompt(sc, "Subject:");
        String subject;
        try {
            int idx = Integer.parseInt(subChoice);
            subject = (idx >= 1 && idx <= subjects.length) ? subjects[idx - 1] : "General";
        } catch (NumberFormatException e) {
            subject = subChoice.isEmpty() ? "General" : subChoice;
        }

        countdown(focusMins * 60, "FOCUS — " + subject, Terminal.FG_CYAN);

        // Session complete — log it
        pomodoroCount++;
        StudySession session = new StudySession(user.getUsername(), subject,
            focusMins, StudySession.Type.POMODORO);
        store.addSession(session);
        user.recordStudy(focusMins);
        store.saveUser(user);

        System.out.println();
        Terminal.ok("Session complete! 🍅 #" + pomodoroCount + "  +" + (focusMins / 5) + " XP");
        printStats();

        if (pomodoroCount % 4 == 0) {
            System.out.println(Terminal.success("  Great work! Time for a long break."));
        } else {
            System.out.println(Terminal.muted("  Take a short break — you earned it!"));
        }
        Terminal.pause(sc);
    }

    private void startBreak(int minutes, String label) {
        Terminal.clear();
        Terminal.header(label + " 🧘");
        countdown(minutes * 60, label, Terminal.FG_GREEN);
        System.out.println();
        Terminal.ok("Break over — back to it!");
        Terminal.pause(sc);
    }

    /**
     * Real-time countdown loop.
     * Prints a live progress bar, refreshing every second.
     * Press Enter to skip/stop.
     */
    private void countdown(int totalSeconds, String label, String colour) {
        System.out.println(Terminal.muted("  (Press Enter at any time to stop early)"));
        System.out.println();

        // Non-blocking Enter check via a background thread
        final boolean[] stopped = {false};
        Thread inputThread = new Thread(() -> {
            try { System.in.read(); stopped[0] = true; }
            catch (Exception ignored) {}
        });
        inputThread.setDaemon(true);
        inputThread.start();

        int secondsLeft = totalSeconds;
        while (secondsLeft >= 0 && !stopped[0]) {
            int m = secondsLeft / 60;
            int s = secondsLeft % 60;
            int pct = totalSeconds > 0 ? (int)(100.0 * (totalSeconds - secondsLeft) / totalSeconds) : 100;
            String bar = Terminal.progressBar(totalSeconds - secondsLeft, totalSeconds, 30);

            // Overwrite current line
            System.out.printf("\r  %s%-25s  %s%02d:%02d%s  %s %3d%%%s   ",
                colour, label, Terminal.BOLD, m, s, Terminal.RESET, bar, pct, Terminal.RESET);
            System.out.flush();

            if (secondsLeft == 0) break;

            try { Thread.sleep(1000); } catch (InterruptedException ignored) { break; }
            secondsLeft--;
        }
        System.out.println();
        inputThread.interrupt();
    }

    private void configure() {
        Terminal.header("Configure Durations");
        focusMins      = Terminal.promptInt(sc, "Focus duration (minutes, 5–90):", 5, 90);
        shortBreakMins = Terminal.promptInt(sc, "Short break   (minutes, 1–30):", 1, 30);
        longBreakMins  = Terminal.promptInt(sc, "Long break    (minutes, 5–60):", 5, 60);
        Terminal.ok("Durations updated.");
        Terminal.pause(sc);
    }

    private void printStats() {
        List<StudySession> sessions = store.getSessionsForUser(user.getUsername());
        LocalDate today  = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        int todayMin = sessions.stream()
            .filter(s -> s.getDate().equals(today))
            .mapToInt(StudySession::getDurationMinutes).sum();
        int weekMin = sessions.stream()
            .filter(s -> !s.getDate().isBefore(monday) && !s.getDate().isAfter(today))
            .mapToInt(StudySession::getDurationMinutes).sum();

        int W = 50;
        Terminal.box("Today's Progress", W);
        Terminal.row(Terminal.label("Today:       ") + Terminal.bold(Terminal.formatMin(todayMin)), W);
        Terminal.row(Terminal.label("This week:   ") + Terminal.bold(Terminal.formatMin(weekMin)), W);
        Terminal.row(Terminal.label("Streak:      ") + Terminal.bold(user.getStreak() + " 🔥"), W);
        Terminal.row(Terminal.label("Level:       ") + Terminal.bold("Lv " + user.getLevel() +
            "  (" + user.getXp() + " XP)"), W);
        int xpPct = user.getXp() % 100;
        Terminal.row(Terminal.label("XP Progress: ") + Terminal.progressBar(xpPct, 100, 20), W);
        Terminal.boxEnd(W);
    }

    private String[] getSubjectList() {
        List<TimetableEntry> entries = store.getTimetableForUser(user.getUsername());
        Set<String> subjects = new LinkedHashSet<>();
        subjects.add("General");
        entries.forEach(e -> subjects.add(e.getSubject()));
        if (subjects.size() == 1) {
            subjects.add("Math"); subjects.add("Science");
            subjects.add("English"); subjects.add("Programming");
        }
        return subjects.toArray(new String[0]);
    }
}
