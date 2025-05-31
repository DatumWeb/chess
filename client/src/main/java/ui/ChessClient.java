package ui;

import java.util.Scanner;

public class ChessClient {
    private final String serverUrl;
    private final Scanner scanner;

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Test UI");
        System.out.println(serverUrl);

        while(true) {
            System.out.println(">>> ");
            String input = scanner. nextLine().trim();

            if (input.equals("quit")) {
                break;
            } else {
                System.out.println(input);
            }
        }
    }
}
