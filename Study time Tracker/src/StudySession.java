import java.time.LocalDate;
import java.time.LocalTime;

/**
 * StudySession — a single logged study block.
 */
public class StudySession {
    public enum Type { STUDY, POMODORO, CLASS, EXAM, ASSIGNMENT }

    private String    username;
    private String    subject;
    private int       durationMinutes;
    private Type      type;
    private LocalDate date;
    private LocalTime startTime;
    private String    notes;

    public StudySession(String username, String subject, int durationMinutes, Type type) {
        this.username        = username;
        this.subject         = subject;
        this.durationMinutes = durationMinutes;
        this.type            = type;
        this.date            = LocalDate.now();
        this.startTime       = LocalTime.now();
        this.notes           = "";
    }

    public StudySession(String username, String subject, int durationMinutes,
                        Type type, LocalDate date, LocalTime startTime, String notes) {
        this.username        = username;
        this.subject         = subject;
        this.durationMinutes = durationMinutes;
        this.type            = type;
        this.date            = date;
        this.startTime       = startTime;
        this.notes           = notes;
    }

    public String    getUsername()        { return username; }
    public String    getSubject()         { return subject; }
    public int       getDurationMinutes() { return durationMinutes; }
    public Type      getType()            { return type; }
    public LocalDate getDate()            { return date; }
    public LocalTime getStartTime()       { return startTime; }
    public String    getNotes()           { return notes; }
    public void      setNotes(String n)   { this.notes = n; }

    public String toCsv() {
        return username + "," + subject + "," + durationMinutes + "," +
               type.name() + "," + date + "," + startTime + "," +
               notes.replace(",", ";");
    }

    @Override public String toString() {
        return date + " | " + subject + " | " + durationMinutes + " min [" + type + "]";
    }
}
