package src;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Entry point for the Library Management System.
 *
 * <p>Displays a menu, reads user input, and delegates all business logic to
 * {@link Library}. All user-facing messages are formatted here; no business
 * logic lives in this class.
 */
public class Main {

    // -------------------------------------------------------------------------
    // ANSI colour codes (gracefully ignored on terminals that don't support them)
    // -------------------------------------------------------------------------
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String BLUE   = "\u001B[34m";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    private static final Library library = new Library();
    private static final Scanner scanner = new Scanner(System.in);

    // -------------------------------------------------------------------------
    // main()
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        printBanner();
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readMenuChoice();
            switch (choice) {
                case 1 -> handleAddBook();
                case 2 -> handleRemoveBook();
                case 3 -> handleSearchBook();
                case 4 -> handleDisplayAll();
                case 5 -> running = handleExit();
                default -> printWarning("Invalid choice. Please enter a number between 1 and 5.");
            }
        }
        scanner.close();
    }

    // -------------------------------------------------------------------------
    // Menu handlers
    // -------------------------------------------------------------------------

    /** Handles the "Add Book" flow. */
    private static void handleAddBook() {
        printSectionHeader("ADD A NEW BOOK");

        String id     = promptNonBlank("Enter Book ID     : ");
        String title  = promptNonBlank("Enter Title       : ");
        String author = promptNonBlank("Enter Author      : ");
        int    year   = promptPublicationYear();

        Book newBook = new Book(id, title, author, year);

        if (library.addBook(newBook)) {
            printSuccess("Book added successfully!");
            System.out.println(newBook);
        } else {
            printError("A book with ID '" + id + "' already exists. Duplicate IDs are not allowed.");
        }
    }

    /** Handles the "Remove Book" flow. */
    private static void handleRemoveBook() {
        printSectionHeader("REMOVE A BOOK");

        if (library.getBookCount() == 0) {
            printWarning("The library is empty – nothing to remove.");
            return;
        }

        String id = promptNonBlank("Enter Book ID to remove: ");
        Optional<Book> found = library.searchById(id);

        if (found.isEmpty()) {
            printError("No book found with ID '" + id + "'.");
            return;
        }

        // Show the found book and ask for confirmation
        System.out.println("\nBook found:");
        System.out.println(found.get());
        String confirm = promptNonBlank("\nAre you sure you want to remove this book? (yes/no): ");

        if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
            library.removeBook(id);
            printSuccess("Book with ID '" + id + "' has been removed.");
        } else {
            printWarning("Remove operation cancelled.");
        }
    }

    /** Handles the "Search Book" flow. */
    private static void handleSearchBook() {
        printSectionHeader("SEARCH FOR A BOOK");

        if (library.getBookCount() == 0) {
            printWarning("The library is empty – nothing to search.");
            return;
        }

        System.out.println("Search by:");
        System.out.println("  1. Book ID (exact match)");
        System.out.println("  2. Keyword (title or author)");
        int searchType = readMenuChoice();

        switch (searchType) {
            case 1 -> {
                String id = promptNonBlank("Enter Book ID: ");
                Optional<Book> result = library.searchById(id);
                if (result.isPresent()) {
                    printSuccess("Book found:");
                    System.out.println(result.get());
                } else {
                    printError("No book found with ID '" + id + "'.");
                }
            }
            case 2 -> {
                String keyword = promptNonBlank("Enter keyword (title or author): ");
                List<Book> results = library.searchByKeyword(keyword);
                if (results.isEmpty()) {
                    printError("No books found matching '" + keyword + "'.");
                } else {
                    printSuccess("Found " + results.size() + " book(s) matching '" + keyword + "':");
                    results.forEach(System.out::println);
                }
            }
            default -> printWarning("Invalid search type. Please enter 1 or 2.");
        }
    }

    /** Handles the "Display All Books" flow. */
    private static void handleDisplayAll() {
        printSectionHeader("ALL BOOKS IN THE LIBRARY");

        List<Book> all = library.getAllBooks();
        if (all.isEmpty()) {
            printWarning("The library is currently empty.");
            return;
        }

        System.out.println(CYAN + "Total books: " + all.size() + RESET + "\n");
        int index = 1;
        for (Book book : all) {
            System.out.println(BLUE + "  #" + index++ + RESET);
            System.out.println(book);
            System.out.println();
        }
    }

    /** Handles the "Exit" flow; returns {@code false} to stop the main loop. */
    private static boolean handleExit() {
        printSectionHeader("GOODBYE");
        System.out.println(GREEN + "  Thank you for using the Library Management System!" + RESET);
        System.out.println(GREEN + "  All changes have been saved automatically." + RESET + "\n");
        return false; // signal to stop the loop
    }

    // -------------------------------------------------------------------------
    // Input helpers
    // -------------------------------------------------------------------------

    /**
     * Reads a menu-choice integer from stdin.
     * Returns {@code -1} for any non-integer or empty input so the caller can
     * handle it gracefully without crashing.
     */
    private static int readMenuChoice() {
        System.out.print(CYAN + "  Your choice: " + RESET);
        try {
            int value = scanner.nextInt();
            scanner.nextLine(); // consume trailing newline
            return value;
        } catch (InputMismatchException e) {
            scanner.nextLine(); // discard the bad token
            return -1;          // caller will treat as invalid
        }
    }

    /**
     * Prompts the user with the given message and loops until a non-blank string
     * is entered.
     */
    private static String promptNonBlank(String prompt) {
        while (true) {
            System.out.print("  " + prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            printWarning("Input cannot be blank. Please try again.");
        }
    }

    /**
     * Prompts for an optional publication year.
     * Returns {@code 0} when the user skips the field or enters an invalid value.
     */
    private static int promptPublicationYear() {
        System.out.print("  Enter Publication Year (press Enter to skip): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return 0;
        try {
            int year = Integer.parseInt(input);
            if (year < 1 || year > 9999) {
                printWarning("Year out of range – field will be skipped.");
                return 0;
            }
            return year;
        } catch (NumberFormatException e) {
            printWarning("Invalid year format – field will be skipped.");
            return 0;
        }
    }

    // -------------------------------------------------------------------------
    // UI helpers
    // -------------------------------------------------------------------------

    private static void printBanner() {
        System.out.println();
        System.out.println(CYAN + BOLD +
                "╔══════════════════════════════════════════════╗\n" +
                "║       LIBRARY MANAGEMENT SYSTEM  v1.0        ║\n" +
                "║          Built with Java – CLI Edition        ║\n" +
                "╚══════════════════════════════════════════════╝" +
                RESET);
        System.out.println();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println(BOLD + CYAN + "┌─── MAIN MENU ───────────────────────────────┐" + RESET);
        System.out.println("│  1. Add Book                                │");
        System.out.println("│  2. Remove Book                             │");
        System.out.println("│  3. Search Book                             │");
        System.out.println("│  4. Display All Books                       │");
        System.out.println("│  5. Exit                                    │");
        System.out.println(BOLD + CYAN + "└─────────────────────────────────────────────┘" + RESET);
    }

    private static void printSectionHeader(String title) {
        System.out.println();
        System.out.println(BOLD + YELLOW + "══ " + title + " ══" + RESET);
        System.out.println();
    }

    private static void printSuccess(String message) {
        System.out.println("\n" + GREEN + "  ✔  " + message + RESET);
    }

    private static void printError(String message) {
        System.out.println("\n" + RED + "  ✖  " + message + RESET);
    }

    private static void printWarning(String message) {
        System.out.println("\n" + YELLOW + "  ⚠  " + message + RESET);
    }
}
