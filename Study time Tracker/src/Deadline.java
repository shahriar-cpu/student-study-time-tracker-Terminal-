import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Deadline — an exam, assignment, or project with a due date.
 */
public class Deadline {
    public enum DeadlineType { EXAM, ASSIGNMENT, PROJECT, OTHER }

    private String       username;
    private String       title;
    private String       subject;
    private LocalDate    dueDate;
    private DeadlineType type;
    private boolean      completed;
    private String       notes;

    public Deadline(String username, String title, String subject,
                    LocalDate dueDate, DeadlineType type) {
        this.username  = username;
        this.title     = title;
        this.subject   = subject;
        this.dueDate   = dueDate;
        this.type      = type;
        this.completed = false;
        this.notes     = "";
    }

    public String       getUsername() { return username; }
    public String       getTitle()    { return title; }
    public String       getSubject()  { return subject; }
    public LocalDate    getDueDate()  { return dueDate; }
    public DeadlineType getType()     { return type; }
    public boolean      isCompleted() { return completed; }
    public String       getNotes()    { return notes; }
    public void         setCompleted(boolean c) { this.completed = c; }
    public void         setNotes(String n)      { this.notes = n; }

    /** Days remaining (negative = overdue). */
    public long daysRemaining() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    public String urgencyLabel() {
        long d = daysRemaining();
        if (d < 0)  return "OVERDUE";
        if (d == 0) return "TODAY";
        if (d <= 3) return "URGENT";
        if (d <= 7) return "SOON";
        return "OK";
    }

    public String toCsv() {
        return username + "," + title.replace(",", ";") + "," +
               subject.replace(",", ";") + "," + dueDate + "," +
               type.name() + "," + completed + "," + notes.replace(",", ";");
    }

    @Override public String toString() {
        return "[" + type + "] " + title + " — " + dueDate + " (" + urgencyLabel() + ")";
    }
}
