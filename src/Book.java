package src;

/**
 * Represents a book in the library.
 * Stores book details: ID, title, author, and publication year.
 */
public class Book {

    private String bookId;
    private String title;
    private String author;
    private int publicationYear; // 0 means not provided

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Book(String bookId, String title, String author, int publicationYear) {
        this.bookId          = bookId.trim();
        this.title           = title.trim();
        this.author          = author.trim();
        this.publicationYear = publicationYear;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getBookId()          { return bookId; }
    public String getTitle()           { return title; }
    public String getAuthor()          { return author; }
    public int    getPublicationYear() { return publicationYear; }

    // -------------------------------------------------------------------------
    // CSV serialisation / deserialisation
    // -------------------------------------------------------------------------

    /**
     * Serialises this Book to a single CSV line.
     * Fields are separated by the pipe character '|' to avoid conflicts with
     * commas that may appear in titles or author names.
     */
    public String toCsvLine() {
        return bookId + "|" + title + "|" + author + "|" + publicationYear;
    }

    /**
     * Creates a Book from a CSV line produced by {@link #toCsvLine()}.
     *
     * @param line a non-null, non-empty CSV line
     * @return the parsed Book, or {@code null} if the line is malformed
     */
    public static Book fromCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split("\\|", -1);
        if (parts.length != 4) return null;
        try {
            String bookId = parts[0].trim();
            String title  = parts[1].trim();
            String author = parts[2].trim();
            int    year   = Integer.parseInt(parts[3].trim());
            if (bookId.isEmpty() || title.isEmpty() || author.isEmpty()) return null;
            return new Book(bookId, title, author, year);
        } catch (NumberFormatException e) {
            return null; // malformed year field
        }
    }

    // -------------------------------------------------------------------------
    // Display helper
    // -------------------------------------------------------------------------

    /**
     * Returns a formatted, human-readable representation of this book.
     */
    @Override
    public String toString() {
        String year = (publicationYear > 0) ? String.valueOf(publicationYear) : "N/A";
        return String.format(
                "┌─────────────────────────────────────────┐%n" +
                "│  Book ID   : %-27s│%n" +
                "│  Title     : %-27s│%n" +
                "│  Author    : %-27s│%n" +
                "│  Year      : %-27s│%n" +
                "└─────────────────────────────────────────┘",
                bookId, title, author, year
        );
    }
}
