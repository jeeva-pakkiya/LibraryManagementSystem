package src;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages the library's book collection.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Maintain an in-memory {@link ArrayList} of {@link Book} objects.</li>
 *   <li>Provide add, remove, search, and display operations.</li>
 *   <li>Persist the collection to, and load it from, a plain-text CSV file.</li>
 * </ul>
 */
public class Library {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Path to the data file (relative to the project root when run via the
     *  recommended command). */
    private static final String DATA_FILE_PATH = "data/books.txt";

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private final List<Book> books = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructor – loads persisted records immediately
    // -------------------------------------------------------------------------

    public Library() {
        loadFromFile();
    }

    // -------------------------------------------------------------------------
    // Core Operations
    // -------------------------------------------------------------------------

    /**
     * Adds a new book to the library.
     *
     * @param book the book to add (must not be {@code null})
     * @return {@code true} if added successfully;
     *         {@code false} if a book with the same ID already exists
     */
    public boolean addBook(Book book) {
        if (book == null) return false;
        if (findById(book.getBookId()).isPresent()) {
            return false; // duplicate ID
        }
        books.add(book);
        saveToFile();
        return true;
    }

    /**
     * Removes the book with the given ID.
     *
     * @param bookId the ID to look for
     * @return {@code true} if a book was removed; {@code false} if not found
     */
    public boolean removeBook(String bookId) {
        Optional<Book> target = findById(bookId);
        if (target.isEmpty()) return false;
        books.remove(target.get());
        saveToFile();
        return true;
    }

    /**
     * Searches for a book by its ID (case-insensitive).
     *
     * @param bookId the ID to search for
     * @return an {@link Optional} containing the book, or empty if not found
     */
    public Optional<Book> searchById(String bookId) {
        return findById(bookId);
    }

    /**
     * Searches for books whose title or author contains the given query string
     * (case-insensitive substring match).
     *
     * @param query the search term
     * @return a list of matching books (may be empty)
     */
    public List<Book> searchByKeyword(String query) {
        String lower = query.toLowerCase();
        List<Book> results = new ArrayList<>();
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(lower) ||
                b.getAuthor().toLowerCase().contains(lower)) {
                results.add(b);
            }
        }
        return results;
    }

    /**
     * Returns an unmodifiable snapshot of all books.
     */
    public List<Book> getAllBooks() {
        return List.copyOf(books);
    }

    /**
     * Returns the total number of books in the library.
     */
    public int getBookCount() {
        return books.size();
    }

    // -------------------------------------------------------------------------
    // Internal helper
    // -------------------------------------------------------------------------

    private Optional<Book> findById(String bookId) {
        String normalized = bookId.trim().toLowerCase();
        return books.stream()
                .filter(b -> b.getBookId().toLowerCase().equals(normalized))
                .findFirst();
    }

    // -------------------------------------------------------------------------
    // File Persistence
    // -------------------------------------------------------------------------

    /**
     * Loads books from {@value #DATA_FILE_PATH}.
     * Skips blank lines and malformed entries silently – the file is treated as
     * best-effort; a missing or unreadable file is not an error.
     */
    private void loadFromFile() {
        File file = new File(DATA_FILE_PATH);
        if (!file.exists()) return; // first run – nothing to load

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                Book book = Book.fromCsvLine(line);
                if (book == null) {
                    System.err.println("[WARN] Skipping malformed record at line " + lineNumber);
                } else {
                    books.add(book);
                }
            }
            if (!books.isEmpty()) {
                System.out.println("[INFO] Loaded " + books.size() + " book(s) from storage.");
            }
        } catch (IOException e) {
            System.err.println("[WARN] Could not read data file: " + e.getMessage());
        }
    }

    /**
     * Saves the current collection to {@value #DATA_FILE_PATH}.
     * Creates parent directories automatically.
     */
    private void saveToFile() {
        File file = new File(DATA_FILE_PATH);
        File parentDir = file.getParentFile();

        // Ensure the data directory exists
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.err.println("[ERROR] Cannot create data directory: " + parentDir.getPath());
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("# Library Management System – book records");
            writer.newLine();
            writer.write("# Format: bookId|title|author|publicationYear (0 = not provided)");
            writer.newLine();
            for (Book b : books) {
                writer.write(b.toCsvLine());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Could not save data file: " + e.getMessage());
        }
    }
}
