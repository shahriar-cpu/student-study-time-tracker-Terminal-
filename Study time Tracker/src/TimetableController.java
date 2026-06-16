import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

/**
 * TimetableController — add, list, and remove recurring weekly class entries.
 */
public class TimetableController {

    private final DataStore store;
    private final User      user;
    private final Scanner   sc;

    public TimetableController(DataStore store, User user, Scanner sc) {
        this.store = store;
        this.user  = user;
        this.sc    = sc;
    }

    public void run() {
        while (true) {
            Terminal.clear();
            Terminal.header("Timetable — Weekly Classes");
            listEntries();
            System.out.println();
            System.out.println(Terminal.accent("  [1]") + " Add Class");
            System.out.println(Terminal.accent("  [2]") + " Remove Class");
            System.out.println(Terminal.muted("  [0]") + " Back");
            System.out.println();
            String choice = Terminal.prompt(sc, "Choice:");
            switch (choice) {
                case "1" -> addEntry();
                case "2" -> removeEntry();
                case "0" -> { return; }
                default  -> Terminal.error("Invalid option.");
            }
        }
    }

    private void listEntries() {
        List<TimetableEntry> entries = store.getTimetableForUser(user.getUsername());
        if (entries.isEmpty()) {
            Terminal.info("No classes added yet. Use [1] to add one.");
            return;
        }

        int W = 64;
        Terminal.box("Class Schedule", W);
        int i = 1;
        for (TimetableEntry e : entries) {
            String loc  = e.getLocation().isBlank() ? "" : "  @ " + e.getLocation();
            String row  = Terminal.bold(String.format("%2d. ", i)) +
                          Terminal.accent(e.getSubject()) + "  " +
                          Terminal.muted(e.getDayOfWeek().toString().substring(0, 3)) + "  " +
                          e.getStartTime() + "–" + e.getEndTime() +
                          "  " + Terminal.muted(e.getDurationMinutes() + " min") + loc;
            Terminal.row(row, W);
            i++;
        }
        Terminal.boxEnd(W);

        int totalMin = entries.stream().mapToInt(TimetableEntry::getDurationMinutes).sum();
        Terminal.info(entries.size() + " classes · " + totalMin + " min/week (" +
            String.format("%.1f", totalMin / 60.0) + "h)");
    }

    private void addEntry() {
        Terminal.header("Add Class");

        String subject = Terminal.prompt(sc, "Subject:");
        if (subject.isEmpty()) { Terminal.error("Subject required."); return; }

        System.out.println(Terminal.muted("  Days: MON TUE WED THU FRI SAT SUN"));
        String dayStr = Terminal.prompt(sc, "Day (e.g. MONDAY):").toUpperCase();
        DayOfWeek day;
        try { day = DayOfWeek.valueOf(dayStr); }
        catch (Exception e) { Terminal.error("Invalid day. Use MONDAY, TUESDAY, etc."); return; }

        String startStr = Terminal.prompt(sc, "Start time (HH:MM, e.g. 09:00):");
        String endStr   = Terminal.prompt(sc, "End time   (HH:MM, e.g. 10:30):");
        LocalTime start, end;
        try {
            start = LocalTime.parse(startStr);
            end   = LocalTime.parse(endStr);
        } catch (Exception e) {
            Terminal.error("Invalid time format. Use HH:MM.");
            return;
        }
        if (!end.isAfter(start)) { Terminal.error("End time must be after start time."); return; }

        String location = Terminal.prompt(sc, "Location (optional, press Enter to skip):");
        int colorIdx    = (int)(Math.random() * 8); // random colour index

        store.addTimetableEntry(new TimetableEntry(user.getUsername(), subject, day,
            start, end, location, colorIdx));
        Terminal.ok("Class \"" + subject + "\" added.");
        Terminal.pause(sc);
    }

    private void removeEntry() {
        List<TimetableEntry> entries = store.getTimetableForUser(user.getUsername());
        if (entries.isEmpty()) { Terminal.error("No classes to remove."); return; }
        listEntries();
        int idx = Terminal.promptInt(sc, "Enter number to remove (0 to cancel):", 0, entries.size());
        if (idx == 0) return;
        TimetableEntry e = entries.get(idx - 1);
        store.removeTimetableEntry(e);
        Terminal.ok("\"" + e.getSubject() + "\" removed.");
        Terminal.pause(sc);
    }
}
