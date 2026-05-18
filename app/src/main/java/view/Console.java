package view;

import java.util.Scanner;

public class Console {
    private Scanner scanner = new Scanner(System.in);

    public void displayInfo(String s) {
        System.out.println(s);
    }

    public String promptUser(String s) {
        System.out.print(s);
        if (!scanner.hasNextLine()) {
            return "quit";
        }

        return scanner.nextLine();
    }
}
