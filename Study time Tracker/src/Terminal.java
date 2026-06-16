import java.util.Scanner;

/**
 * Terminal — ANSI colour/box utilities and input helpers.
 * Drop-in replacement for AppTheme in the CLI version.
 */
public final class Terminal {
    private Terminal() {}

    // ── ANSI codes ─────────────────────────────────────────────────────────────
    public static final String RESET   = "\u001B[0m";
    public static final String BOLD    = "\u001B[1m";
    public static final String DIM     = "\u001B[2m";

    // Foreground colours
    public static final String FG_CYAN    = "\u001B[96m";
    public static final String FG_GREEN   = "\u001B[92m";
    public static final String FG_RED     = "\u001B[91m";
    public static final String FG_YELLOW  = "\u001B[93m";
    public static final String FG_BLUE    = "\u001B[94m";
    public static final String FG_MAGENTA = "\u001B[95m";
    public static final String FG_WHITE   = "\u001B[97m";
    public static final String FG_GRAY    = "\u001B[37m";

    // Background colours
    public static final String BG_BLUE    = "\u001B[44m";
    public static final String BG_CYAN    = "\u001B[46m";

    // ── Box drawing ────────────────────────────────────────────────────────────
    public static void box(String title, int width) {
        String line = "─".repeat(width - 2);
        System.out.println(FG_CYAN + "┌" + line + "┐" + RESET);
        int pad = width - 2 - title.length();
        int left  = pad / 2;
        int right = pad - left;
        System.out.println(FG_CYAN + "│" + " ".repeat(left) + BOLD + FG_WHITE + title +
            RESET + FG_CYAN + " ".repeat(right) + "│" + RESET);
        System.out.println(FG_CYAN + "├" + line + "┤" + RESET);
    }

    public static void boxEnd(int width) {
        System.out.println(FG_CYAN + "└" + "─".repeat(width - 2) + "┘" + RESET);
    }

    public static void row(String content, int width) {
        // strip ANSI for length calculation
        int visLen = stripAnsi(content).length();
        int pad = Math.max(0, width - 2 - visLen);
        System.out.println(FG_CYAN + "│" + RESET + " " + content + " ".repeat(pad - 1) + FG_CYAN + "│" + RESET);
    }

    public static void divider(int width) {
        System.out.println(FG_CYAN + "├" + "─".repeat(width - 2) + "┤" + RESET);
    }

    public static void separator(int width) {
        System.out.println(FG_GRAY + "─".repeat(width) + RESET);
    }

    // ── Text helpers ───────────────────────────────────────────────────────────
    public static String accent(String s)  { return FG_CYAN  + BOLD + s + RESET; }
    public static String success(String s) { return FG_GREEN + s + RESET; }
    public static String danger(String s)  { return FG_RED   + s + RESET; }
    public static String warn(String s)    { return FG_YELLOW + s + RESET; }
    public static String muted(String s)   { return FG_GRAY  + s + RESET; }
    public static String bold(String s)    { return BOLD + s + RESET; }
    public static String label(String s)   { return FG_GRAY + s + RESET; }

    /** Urgency colour for deadlines. */
    public static String urgencyColour(String label, String text) {
        return switch (label) {
            case "OVERDUE" -> danger(text);
            case "TODAY"   -> danger(text);
            case "URGENT"  -> warn(text);
            case "SOON"    -> FG_YELLOW + text + RESET;
            default        -> success(text);
        };
    }

    /** ASCII progress bar [████░░░░] */
    public static String progressBar(int value, int max, int barWidth) {
        int filled = max > 0 ? (int)((double) value / max * barWidth) : 0;
        filled = Math.min(filled, barWidth);
        return FG_CYAN + "█".repeat(filled) + FG_GRAY + "░".repeat(barWidth - filled) + RESET;
    }

    /** Mini bar chart row: Subject  [████░░░░] 2h 30m */
    public static String barRow(String label, int minutes, int maxMinutes, int barWidth) {
        int padded = Math.max(maxMinutes, 1);
        int filled = (int)((double) minutes / padded * barWidth);
        filled = Math.min(filled, barWidth);
        String bar = FG_CYAN + "█".repeat(filled) + FG_GRAY + "░".repeat(barWidth - filled) + RESET;
        return String.format("%-18s %s %s", label, bar, muted(formatMin(minutes)));
    }

    /** Remove ANSI escape codes for length calculation. */
    public static String stripAnsi(String s) {
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    /** Pad a coloured string to a given visible width. */
    public static String padRight(String s, int visWidth) {
        int visLen = stripAnsi(s).length();
        int pad = Math.max(0, visWidth - visLen);
        return s + " ".repeat(pad);
    }

    public static String formatMin(int min) {
        return (min / 60) + "h " + (min % 60) + "m";
    }

    // ── Input helpers ──────────────────────────────────────────────────────────
    public static String prompt(Scanner sc, String msg) {
        System.out.print(FG_CYAN + "  → " + RESET + msg + " ");
        return sc.nextLine().trim();
    }

    public static String promptPassword(Scanner sc, String msg) {
        System.out.print(FG_CYAN + "  → " + RESET + msg + " ");
        return sc.nextLine().trim();
    }

    public static int promptInt(Scanner sc, String msg, int min, int max) {
        while (true) {
            String raw = prompt(sc, msg);
            try {
                int v = Integer.parseInt(raw);
                if (v >= min && v <= max) return v;
                error("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                error("Invalid number.");
            }
        }
    }

    public static void error(String msg) {
        System.out.println(danger("  ✗ " + msg));
    }

    public static void ok(String msg) {
        System.out.println(success("  ✓ " + msg));
    }

    public static void info(String msg) {
        System.out.println(muted("  · " + msg));
    }

    public static void header(String title) {
        System.out.println();
        System.out.println(BOLD + FG_CYAN + "  ══ " + title + " ══" + RESET);
        System.out.println();
    }

    public static void clear() {
        // Works on most terminals
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void pause(Scanner sc) {
        System.out.print(muted("  [Press Enter to continue]"));
        sc.nextLine();
    }
}
