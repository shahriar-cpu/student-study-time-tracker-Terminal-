import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * DeadlinesController — add, view, complete, and remove deadlines.
 */
public class DeadlinesController {

    private final DataStore store;
    private final User      user;
    private final Scanner   sc;

    public DeadlinesController(DataStore store, User user, Scanner sc) {
        this.store = store;
        this.user  = user;
        this.sc    = sc;
    }

    public void run() {
        while (true) {
            Terminal.clear();
            Terminal.header("Deadlines");
            listDeadlines(false);
            System.out.println();
            System.out.println(Terminal.accent("  [1]") + " Add Deadline");
            System.out.println(Terminal.accent("  [2]") + " Mark Done / Undo");
            System.out.println(Terminal.accent("  [3]") + " Remove Deadline");
            System.out.println(Terminal.accent("  [4]") + " Show Completed");
            System.out.println(Terminal.muted("  [0]") + " Back");
            System.out.println();
            String choice = Terminal.prompt(sc, "Choice:");
            switch (choice) {
                case "1" -> addDeadline();
                case "2" -> toggleDone();
                case "3" -> removeDeadline();
                case "4" -> { showCompleted(); Terminal.pause(sc); }
                case "0" -> { return; }
                default  -> Terminal.error("Invalid option.");
            }
        }
    }

    private void listDeadlines(boolean includeCompleted) {
        List<Deadline> all = store.getDeadlinesForUser(user.getUsername());
        List<Deadline> list = all.stream()
            .filter(d -> includeCompleted || !d.isCompleted())
            .toList();

        if (list.isEmpty()) {
            Terminal.info(includeCompleted ? "No deadlines found." : "No upcoming deadlines. 🎉");
            return;
        }

        int W = 70;
        Terminal.box("Upcoming Deadlines", W);
        int i = 1;
        for (Deadline d : list) {
            long days = d.daysRemaining();
            String daysStr = days < 0 ? "Overdue " + (-days) + "d"
                           : days == 0 ? "Due TODAY"
                           : "in " + days + "d";
            String urgency   = Terminal.urgencyColour(d.urgencyLabel(), "[" + d.urgencyLabel() + "]");
            String completed = d.isCompleted() ? Terminal.muted(" ✓DONE") : "";
            String row = Terminal.bold(String.format("%2d. ", i)) +
                         Terminal.accent(d.getTitle()) +
                         "  " + Terminal.muted("[" + d.getType() + "]") +
                         "  " + Terminal.label(d.getSubject()) +
                         "  " + Terminal.muted(d.getDueDate().toString()) +
                         "  " + urgency + "  " + Terminal.warn(daysStr) + completed;
            Terminal.row(row, W);
            i++;
        }
        Terminal.boxEnd(W);
    }

    private void showCompleted() {
        Terminal.header("Completed Deadlines");
        listDeadlines(true);
    }

    private void addDeadline() {
        Terminal.header("Add Deadline");

        String title = Terminal.prompt(sc, "Title:");
        if (title.isEmpty()) { Terminal.error("Title required."); return; }

        String subject = Terminal.prompt(sc, "Subject:");
        String dateStr = Terminal.prompt(sc, "Due date (YYYY-MM-DD):");
        LocalDate dueDate;
        try { dueDate = LocalDate.parse(dateStr); }
        catch (Exception e) { Terminal.error("Invalid date. Use YYYY-MM-DD."); return; }

        System.out.println(Terminal.muted("  Types: EXAM  ASSIGNMENT  PROJECT  OTHER"));
        String typeStr = Terminal.prompt(sc, "Type:").toUpperCase();
        Deadline.DeadlineType type;
        try { type = Deadline.DeadlineType.valueOf(typeStr); }
        catch (Exception e) { Terminal.error("Invalid type."); return; }

        String notes = Terminal.prompt(sc, "Notes (optional):");

        Deadline d = new Deadline(user.getUsername(), title, subject, dueDate, type);
        d.setNotes(notes);
        store.addDeadline(d);
        Terminal.ok("Deadline \"" + title + "\" added.");
        Terminal.pause(sc);
    }

    private void toggleDone() {
        List<Deadline> list = store.getDeadlinesForUser(user.getUsername())
            .stream().filter(d -> !d.isCompleted()).toList();
        if (list.isEmpty()) { Terminal.info("No pending deadlines."); Terminal.pause(sc); return; }
        listDeadlines(false);
        int idx = Terminal.promptInt(sc, "Enter number to mark done (0 to cancel):", 0, list.size());
        if (idx == 0) return;
        Deadline d = list.get(idx - 1);
        d.setCompleted(true);
        store.saveDeadlines();
        Terminal.ok("\"" + d.getTitle() + "\" marked as done! ✓");
        Terminal.pause(sc);
    }

    private void removeDeadline() {
        List<Deadline> list = store.getDeadlinesForUser(user.getUsername());
        if (list.isEmpty()) { Terminal.error("No deadlines to remove."); return; }
        listDeadlines(true);
        int idx = Terminal.promptInt(sc, "Enter number to remove (0 to cancel):", 0, list.size());
        if (idx == 0) return;
        Deadline d = list.get(idx - 1);
        String confirm = Terminal.prompt(sc, "Delete \"" + d.getTitle() + "\"? (yes/no):");
        if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
            store.removeDeadline(d);
            Terminal.ok("Deleted.");
        } else {
            Terminal.info("Cancelled.");
        }
        Terminal.pause(sc);
    }
}
