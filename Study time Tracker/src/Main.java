import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner   sc    = new Scanner(System.in);
        DataStore store = new DataStore();

        while (true) {
            AuthController auth = new AuthController(store, sc);
            User user = auth.run();

            if (user == null) {
                System.out.println(Terminal.muted("  Goodbye! 👋"));
                break;
            }

            Dashboard dashboard = new Dashboard(store, user, sc);
            dashboard.run();
            // After logout → loop back to auth screen
        }
        sc.close();
    }
}
