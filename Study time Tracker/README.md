# StudyTrack CLI — Terminal Version

A full-featured terminal (command-line) port of StudyTrack.
Every class has its own file, matching the original GUI structure.

## File Map

| File | Purpose |
|------|---------|
| `Main.java` | Entry point — boots auth → dashboard loop |
| `Terminal.java` | ANSI colours, box drawing, input helpers (replaces AppTheme) |
| `User.java` | User model — credentials, XP, streak, level |
| `StudySession.java` | Study session record — subject, duration, type, date |
| `TimetableEntry.java` | Recurring weekly class block |
| `Deadline.java` | Exam/assignment with urgency labels |
| `DataStore.java` | CSV persistence — same format as GUI version |
| `AuthController.java` | Login & registration screen |
| `Dashboard.java` | Main menu hub (mirrors SideNav) |
| `WeeklyPlannerController.java` | Text-based Mon–Sun grid view |
| `DayViewController.java` | Daily schedule — classes, sessions, deadlines |
| `FocusTimerController.java` | Real-time Pomodoro countdown timer |
| `StatsController.java` | Analytics — bar chart, subject breakdown, XP |
| `FriendsController.java` | Leaderboard — all users sorted by study hours |
| `DeadlinesController.java` | Deadline tracker — add, complete, remove |
| `TimetableController.java` | Manage recurring weekly class schedule |
| `SettingsController.java` | Profile, password change, CSV export |

## Build & Run

### Requirements
- JDK 17 or newer

### Compile
```bash
cd StudyTrackCLI
mkdir -p out
javac -d out src/*.java
```

### Run
```bash
java -cp out Main
```

One-liner:
```bash
javac -d out src/*.java && java -cp out Main
```

## Data Compatibility

The `data/` folder format is **identical** to the GUI version.
You can copy a `data/` folder from the Swing app into this directory
and all users, sessions, timetables, and deadlines will load automatically.

## Features

- ✅ User registration & login (password masked)
- ✅ Weekly planner — ASCII grid, Mon–Sun
- ✅ Day view — classes, sessions, due deadlines
- ✅ Pomodoro focus timer — real-time countdown, auto-logs sessions, awards XP
- ✅ Statistics — bar chart, subject breakdown, XP bar, recent sessions
- ✅ Friends leaderboard — compare hours, 7/14/30/90-day ranges
- ✅ Deadline tracker — urgency colours, mark done, filter
- ✅ Timetable manager — add/remove recurring classes
- ✅ Settings — profile, password change, CSV export
- ✅ Persistent CSV storage (survives restarts)
- ✅ Full ANSI colour support

## Tips

- The Pomodoro timer runs in real-time. Press **Enter** to stop early.
- All data is saved to `data/` automatically.
- Works on Linux, macOS, and Windows Terminal (with ANSI support).
