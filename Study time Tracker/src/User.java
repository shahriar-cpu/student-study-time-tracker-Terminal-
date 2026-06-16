import java.time.LocalDate;

/**
 * User — stores credentials, XP, streak, and display info.
 */
public class User {
    private String    username;
    private String    password;
    private String    email;
    private String    role;
    private int       xp;
    private int       streak;
    private LocalDate lastStudyDate;

    public User(String username, String password, String email) {
        this.username      = username;
        this.password      = password;
        this.email         = email;
        this.role          = "student";
        this.xp            = 0;
        this.streak        = 0;
        this.lastStudyDate = null;
    }

    /** Full constructor — used when loading from CSV. */
    public User(String username, String password, String email,
                String role, int xp, int streak) {
        this.username      = username;
        this.password      = password;
        this.email         = email;
        this.role          = role;
        this.xp            = xp;
        this.streak        = streak;
        this.lastStudyDate = null;
    }

    /** Award XP and maintain streak after a study session. */
    public void recordStudy(int minutes) {
        this.xp += minutes / 5; // 1 XP per 5 min
        LocalDate today = LocalDate.now();
        if (lastStudyDate == null) {
            streak = 1;
        } else if (lastStudyDate.equals(today)) {
            // same day — no change
        } else if (lastStudyDate.equals(today.minusDays(1))) {
            streak++;
        } else {
            streak = 1; // broken
        }
        lastStudyDate = today;
    }

    // ── Getters / setters ─────────────────────────────────────────────────────
    public String    getUsername()        { return username; }
    public String    getPassword()        { return password; }
    public String    getEmail()           { return email; }
    public String    getRole()            { return role; }
    public int       getXp()              { return xp; }
    public int       getStreak()          { return streak; }
    public int       getLevel()           { return (xp / 100) + 1; }
    public int       getXpToNextLevel()   { return 100 - (xp % 100); }
    public LocalDate getLastStudyDate()   { return lastStudyDate; }
    public void      setPassword(String p){ this.password = p; }
    public void      setRole(String r)    { this.role = r; }

    /** Initials for display in terminal (e.g. "AB"). */
    public String getAvatarInitials() {
        if (username == null || username.isEmpty()) return "?";
        String[] parts = username.trim().split("\\s+");
        if (parts.length >= 2)
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return username.substring(0, Math.min(2, username.length())).toUpperCase();
    }

    public String toCsv() {
        return username + "," + password + "," + email + "," + role + "," + xp + "," + streak;
    }

    @Override public String toString() { return username; }
}
