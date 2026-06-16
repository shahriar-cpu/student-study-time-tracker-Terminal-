import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * TimetableEntry — a recurring weekly class or lecture block.
 */
public class TimetableEntry {
    private String    username;
    private String    subject;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String    location;
    private int       colorIndex;

    public TimetableEntry(String username, String subject, DayOfWeek dayOfWeek,
                          LocalTime startTime, LocalTime endTime,
                          String location, int colorIndex) {
        this.username   = username;
        this.subject    = subject;
        this.dayOfWeek  = dayOfWeek;
        this.startTime  = startTime;
        this.endTime    = endTime;
        this.location   = location;
        this.colorIndex = colorIndex;
    }

    public String    getUsername()   { return username; }
    public String    getSubject()    { return subject; }
    public DayOfWeek getDayOfWeek()  { return dayOfWeek; }
    public LocalTime getStartTime()  { return startTime; }
    public LocalTime getEndTime()    { return endTime; }
    public String    getLocation()   { return location; }
    public int       getColorIndex() { return colorIndex; }

    public int getDurationMinutes() {
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }

    public String toCsv() {
        return username + "," + subject + "," + dayOfWeek.name() + "," +
               startTime + "," + endTime + "," +
               location.replace(",", ";") + "," + colorIndex;
    }

    @Override public String toString() {
        return subject + " (" + dayOfWeek + " " + startTime + "–" + endTime +
               (location.isBlank() ? "" : " @ " + location) + ")";
    }
}
