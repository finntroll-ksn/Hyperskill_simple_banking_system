package banking;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    static private Scanner scanner = new Scanner(System.in);
    static private String cardNumber;

    public static void main(String[] args) {
        String dbFileName = getFileName(args);

        if (dbFileName == null) {
            System.out.println("Please specify db name");
            System.exit(0);
        }

        Database.init(dbFileName);
        makeChoice();
    }

    private static String getFileName(String[] args) {

        for (int i = 0; i < args.length; i++) {
            if ("-fileName".equalsIgnoreCase(args[i])) {
                return args[++i];
            }
        }

        return null;
    }

    static private void printMenu() {
        System.out.printf(
                "%s\n%s\n%s\n",
                "1. Create an account",
                "2. Log into account",
                "0. Exit"
        );
    }

    static private void printActionMenu() {
        System.out.printf(
                "%s\n%s\n%s\n%s\n%s\n%s\n",
                "1. Balance",
                "2. Add income",
                "3. Do transfer",
                "4. Close account",
                "5. Log out",
                "0. Exit"
        );
    }

    static private void makeChoice() {
        boolean exit = false;
        boolean secondExit = false;

        do {
            printMenu();
            int check = Integer.parseInt(scanner.nextLine());

            switch (check) {
                case 1:
                    createNewAccount();
                    break;
                case 2:
                    if (logIntoAccount()) {
                        do {
                            printActionMenu();

                            int secondCheck = Integer.parseInt(scanner.nextLine());

                            switch (secondCheck) {
                                case 1:
                                    System.out.printf("%s%d\n", "Balance: ", Database.load(cardNumber));
                                    break;
                                case 2:
                                    addIncome();
                                    break;
                                case 3:
                                    transferMoney();
                                    break;
                                case 4:
                                    closeAccount();
                                    secondExit = true;
                                    break;
                                case 5:
                                    cardNumber = "";
                                    System.out.println("You have successfully logged out!");
                                    secondExit = true;
                                    break;
                                case 0:
                                    secondExit = true;
                                    exit = true;
                                    break;
                                default:
                                    System.out.println("Incorrect option! Try again.");
                                    break;
                            }
                        } while (!secondExit);
                    }
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("Incorrect option! Try again.");
                    break;
            }
        } while (!exit);

        System.out.println("Bye!");
    }

    static private void createNewAccount() {
        boolean cardExists = true;
        String cardNumber;
        int pin;
        int checkSum;

        do {
            cardNumber = "400000" + ThreadLocalRandom.current().nextLong(100000000L, 999999999L);
            checkSum = getLuhnCheckSum(cardNumber);
            pin = ThreadLocalRandom.current().nextInt(1000, 9999);

            if (Database.load(cardNumber) == null) {
                cardExists = false;
            }
        } while (cardExists);
        
        Database.save(cardNumber + checkSum, pin);

        System.out.printf(
                "%s\n%s\n%s\n%s\n%d\n",
                "Your card has been created",
                "Your card number:",
                cardNumber + checkSum,
                "Your card PIN:",
                pin
        );
    }

    static private boolean logIntoAccount() {
        System.out.println("Enter your card number:");
        String userEnterCardNumber = scanner.nextLine();

        System.out.println("Enter your PIN:");
        int cardPIN = Integer.parseInt(scanner.nextLine());

        if (Database.load(userEnterCardNumber, cardPIN) == null) {
            System.out.println("Wrong card number or PIN!");

            return false;
        }

        cardNumber = userEnterCardNumber;
        System.out.println("You have successfully logged in!");

        return true;
    }

    static private int getLuhnCheckSum(String cardNumber) {
        int sum = 0;
        int num;

        for (int i = 0; i < cardNumber.length(); i++) {
            num = Character.getNumericValue(cardNumber.charAt(i));

            if (i % 2 == 0) {
                num *= 2;
                num = num > 9 ? num - 9 : num;
            }

            sum += num ;
        }

        if (sum % 10 > 0) {
            return 10 - (sum % 10);
        } else {
            return 0;
        }
    }

    static private void addIncome() {
        System.out.println("Enter income:");
        Database.update(cardNumber, Integer.parseInt(scanner.nextLine()));
        System.out.println("Income was added!");
    }

    static private void closeAccount() {
        Database.delete(cardNumber);
        System.out.println("The account has been closed!");
    }

    static private void transferMoney() {
        System.out.println("Transfer");
        System.out.println("Enter card number:");

        String recipientCardNumber = scanner.nextLine();
        Integer recipientBalance = Database.load(recipientCardNumber);

        if (recipientBalance == null) {
            System.out.println("Probably you made mistake in the card number. Please try again!");

            return;
        }

        System.out.println("How much money yoy want to transfer:");
        int moneyToTransfer = Integer.parseInt(scanner.nextLine());

        Integer currentBalance = Database.load(cardNumber);
        if (currentBalance == null || moneyToTransfer > currentBalance) {
            System.out.println("Not enough money!");

            return;
        }

        Database.update(cardNumber, currentBalance - moneyToTransfer);
        Database.update(recipientCardNumber, recipientBalance + moneyToTransfer);

        System.out.println("Success!");
    }
}
