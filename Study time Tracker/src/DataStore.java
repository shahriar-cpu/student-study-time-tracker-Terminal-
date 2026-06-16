import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DataStore — flat-file CSV persistence for all app data.
 * Files are stored in the "data/" directory.
 * Thread-safe: all public methods are synchronized.
 */
public class DataStore {
    private static final String DIR            = "data/";
    private static final String USERS_FILE     = DIR + "users.csv";
    private static final String SESSIONS_FILE  = DIR + "sessions.csv";
    private static final String TIMETABLE_FILE = DIR + "timetable.csv";
    private static final String DEADLINES_FILE = DIR + "deadlines.csv";

    private final Map<String, User>     users     = new LinkedHashMap<>();
    private final List<StudySession>    sessions  = new ArrayList<>();
    private final List<TimetableEntry>  timetable = new ArrayList<>();
    private final List<Deadline>        deadlines = new ArrayList<>();

    public DataStore() {
        initDir();
        loadUsers();
        loadSessions();
        loadTimetable();
        loadDeadlines();
    }

    private void initDir() {
        try { Files.createDirectories(Paths.get(DIR)); }
        catch (IOException e) { e.printStackTrace(); }
    }

    // ── Users ──────────────────────────────────────────────────────────────────
    public synchronized boolean registerUser(String username, String password, String email) {
        if (users.containsKey(username)) return false;
        users.put(username, new User(username, password, email));
        saveUsers();
        return true;
    }

    public synchronized User loginUser(String username, String password) {
        User u = users.get(username);
        return (u != null && u.getPassword().equals(password)) ? u : null;
    }

    public synchronized User getUser(String username) { return users.get(username); }

    public synchronized Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public synchronized void saveUser(User u) { users.put(u.getUsername(), u); saveUsers(); }

    private void saveUsers() {
        try (PrintWriter w = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User u : users.values()) w.println(u.toCsv());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadUsers() {
        users.clear();
        try (BufferedReader r = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 6)
                    users.put(p[0], new User(p[0], p[1], p[2], p[3],
                        parseInt(p[4]), parseInt(p[5])));
            }
        } catch (IOException ignored) {}
    }

    // ── Sessions ───────────────────────────────────────────────────────────────
    public synchronized void addSession(StudySession s) { sessions.add(s); saveSessions(); }

    public synchronized List<StudySession> getSessionsForUser(String username) {
        return sessions.stream()
            .filter(s -> s.getUsername().equals(username))
            .collect(Collectors.toList());
    }

    public synchronized List<StudySession> getAllSessions() {
        return Collections.unmodifiableList(sessions);
    }

    private void saveSessions() {
        try (PrintWriter w = new PrintWriter(new FileWriter(SESSIONS_FILE))) {
            for (StudySession s : sessions) w.println(s.toCsv());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadSessions() {
        sessions.clear();
        try (BufferedReader r = new BufferedReader(new FileReader(SESSIONS_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 6) {
                    sessions.add(new StudySession(
                        p[0], p[1], parseInt(p[2]),
                        StudySession.Type.valueOf(p[3]),
                        LocalDate.parse(p[4]),
                        LocalTime.parse(p[5]),
                        p.length > 6 ? p[6].replace(";", ",") : ""));
                }
            }
        } catch (IOException ignored) {}
    }

    // ── Timetable ──────────────────────────────────────────────────────────────
    public synchronized void addTimetableEntry(TimetableEntry e) { timetable.add(e); saveTimetable(); }

    public synchronized void removeTimetableEntry(TimetableEntry e) { timetable.remove(e); saveTimetable(); }

    public synchronized List<TimetableEntry> getTimetableForUser(String username) {
        return timetable.stream()
            .filter(e -> e.getUsername().equals(username))
            .collect(Collectors.toList());
    }

    private void saveTimetable() {
        try (PrintWriter w = new PrintWriter(new FileWriter(TIMETABLE_FILE))) {
            for (TimetableEntry e : timetable) w.println(e.toCsv());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadTimetable() {
        timetable.clear();
        try (BufferedReader r = new BufferedReader(new FileReader(TIMETABLE_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 7)
                    timetable.add(new TimetableEntry(
                        p[0], p[1], DayOfWeek.valueOf(p[2]),
                        LocalTime.parse(p[3]), LocalTime.parse(p[4]),
                        p[5].replace(";", ","), parseInt(p[6])));
            }
        } catch (IOException ignored) {}
    }

    // ── Deadlines ──────────────────────────────────────────────────────────────
    public synchronized void addDeadline(Deadline d) { deadlines.add(d); saveDeadlines(); }

    public synchronized void removeDeadline(Deadline d) { deadlines.remove(d); saveDeadlines(); }

    public synchronized void saveDeadlines() {
        try (PrintWriter w = new PrintWriter(new FileWriter(DEADLINES_FILE))) {
            for (Deadline d : deadlines) w.println(d.toCsv());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public synchronized List<Deadline> getDeadlinesForUser(String username) {
        return deadlines.stream()
            .filter(d -> d.getUsername().equals(username))
            .sorted(Comparator.comparing(Deadline::getDueDate))
            .collect(Collectors.toList());
    }

    private void loadDeadlines() {
        deadlines.clear();
        try (BufferedReader r = new BufferedReader(new FileReader(DEADLINES_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 6) {
                    Deadline d = new Deadline(p[0], p[1].replace(";", ","),
                        p[2].replace(";", ","), LocalDate.parse(p[3]),
                        Deadline.DeadlineType.valueOf(p[4]));
                    d.setCompleted(Boolean.parseBoolean(p[5]));
                    if (p.length > 6) d.setNotes(p[6].replace(";", ","));
                    deadlines.add(d);
                }
            }
        } catch (IOException ignored) {}
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
}
