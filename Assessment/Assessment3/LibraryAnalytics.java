package com.exam.service;

import com.exam.model.Book;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LibraryAnalytics {

    private Map<String, Book> books = new HashMap<>();

    /** Matches a word immediately repeated (case-insensitive), e.g. "Java Java". */
    private static final Pattern REPEATED_WORD =
            Pattern.compile("\\b(\\w+)\\s+\\1\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Rule 1 & 2: parse, validate and de-duplicate incoming records.
     * Iterates with a classic loop (not a stream) so that a mocked List that only
     * stubs size()/iterator() is honoured correctly.
     */
    public void loadBooks(List<String> records) {
        if (records == null || records.size() == 0) {
            return;
        }
        for (String record : records) {
            if (record == null) {
                continue;
            }
            String[] parts = record.split("\\|", -1);
            if (parts.length != 6) {
                continue;
            }
            boolean anyBlank = Arrays.stream(parts)
                    .anyMatch(field -> field == null || field.trim().isEmpty());
            if (anyBlank) {
                continue;
            }
            String bookId = parts[0].trim();
            String title = parts[1].trim();
            String author = parts[2].trim();
            String category = parts[3].trim();
            int borrowCount;
            double rating;
            try {
                borrowCount = Integer.parseInt(parts[4].trim());
                rating = Double.parseDouble(parts[5].trim());
            } catch (NumberFormatException ex) {
                continue;
            }
            if (rating < 0 || rating > 5 || borrowCount < 0) {
                continue;
            }
            Book candidate = new Book(bookId, title, author, category, borrowCount, rating);
            Book existing = books.get(bookId);
            if (existing == null || isPreferred(candidate, existing)) {
                books.put(bookId, candidate);
            }
        }
    }

    /** Duplicate resolution: higher rating, then higher borrow, then smaller title. */
    private boolean isPreferred(Book candidate, Book existing) {
        if (candidate.getRating() != existing.getRating()) {
            return candidate.getRating() > existing.getRating();
        }
        if (candidate.getBorrowCount() != existing.getBorrowCount()) {
            return candidate.getBorrowCount() > existing.getBorrowCount();
        }
        return candidate.getTitle().compareTo(existing.getTitle()) < 0;
    }

    /** Rule 3: Rating DESC, then Borrow Count DESC; return the first n. */
    public List<Book> topRatedBooks(int n) {
        if (n <= 0) {
            return new ArrayList<>();
        }
        return books.values().stream()
                .sorted(Comparator
                        .comparingDouble((Book b) -> b.getRating()).reversed()
                        .thenComparing(Book::getBorrowCount, Comparator.reverseOrder()))
                .limit(n)
                .collect(Collectors.toList());
    }

    /** Rule 4: average rating per category in a TreeMap, rounded to 2 decimals. */
    public Map<String, Double> averageRatingByCategory() {
        Map<String, Double> averages = books.values().stream()
                .collect(Collectors.groupingBy(
                        Book::getCategory,
                        TreeMap::new,
                        Collectors.averagingDouble(Book::getRating)));
        averages.replaceAll((category, average) ->
                BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP).doubleValue());
        return averages;
    }

    /** Rule 5: highest borrow, then highest rating, then smallest bookId. */
    public Optional<Book> mostBorrowedBook() {
        return books.values().stream()
                .max(Comparator
                        .comparingInt((Book b) -> b.getBorrowCount())
                        .thenComparingDouble(Book::getRating)
                        .thenComparing(Book::getBookId, Comparator.reverseOrder()));
    }

    /** Rule 6: authors that appear in more than one category, as a TreeSet. */
    public Set<String> authorsWithMultipleCategories() {
        return books.values().stream()
                .collect(Collectors.groupingBy(
                        Book::getAuthor,
                        Collectors.mapping(Book::getCategory, Collectors.toSet())))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /** Rule 7: LinkedHashMap keyed by author ASC; each list sorted Rating DESC, Borrow DESC. */
    public Map<String, List<Book>> groupBooksByAuthor() {
        Map<String, List<Book>> grouped = books.values().stream()
                .sorted(Comparator
                        .comparingDouble((Book b) -> b.getRating()).reversed()
                        .thenComparing(Book::getBorrowCount, Comparator.reverseOrder()))
                .collect(Collectors.groupingBy(
                        Book::getAuthor,
                        TreeMap::new,
                        Collectors.toList()));
        return new LinkedHashMap<>(grouped);
    }

    /** Rule 8: distinct, alphabetically sorted titles of suspicious books (streams only). */
    public List<String> suspiciousBooks() {
        Map<String, Double> categoryBorrowSum = books.values().stream()
                .collect(Collectors.groupingBy(
                        Book::getCategory, Collectors.summingDouble(Book::getBorrowCount)));
        Map<String, Double> categoryRatingSum = books.values().stream()
                .collect(Collectors.groupingBy(
                        Book::getCategory, Collectors.summingDouble(Book::getRating)));
        Map<String, Long> categoryCount = books.values().stream()
                .collect(Collectors.groupingBy(
                        Book::getCategory, Collectors.counting()));

        return books.values().stream()
                .filter(book -> {
                    boolean repeatedWord = REPEATED_WORD.matcher(book.getTitle()).find();
                    boolean authorInTitle = Pattern
                            .compile("\\b" + Pattern.quote(book.getAuthor()) + "\\b")
                            .matcher(book.getTitle()).find();
                    boolean borrowSurge = false;
                    boolean underperforming = false;
                    long peers = categoryCount.get(book.getCategory());
                    if (peers > 1) {
                        double othersBorrowAvg =
                                (categoryBorrowSum.get(book.getCategory()) - book.getBorrowCount())
                                        / (peers - 1);
                        double othersRatingAvg =
                                (categoryRatingSum.get(book.getCategory()) - book.getRating())
                                        / (peers - 1);
                        borrowSurge = book.getBorrowCount() > 3 * othersBorrowAvg;
                        underperforming = book.getRating() < othersRatingAvg
                                && book.getBorrowCount() > othersBorrowAvg;
                    }
                    return repeatedWord || authorInTitle || borrowSurge || underperforming;
                })
                .map(Book::getTitle)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** Final Challenge: category -> (author -> that author's top-rated book in the category). */
    public Map<String, Map<String, Book>> categoryWiseTopRatedBookByEachAuthor() {
        return books.values().stream()
                .collect(Collectors.groupingBy(
                        Book::getCategory,
                        Collectors.groupingBy(
                                Book::getAuthor,
                                Collectors.collectingAndThen(
                                        Collectors.maxBy(Comparator
                                                .comparingDouble((Book b) -> b.getRating())
                                                .thenComparingInt(Book::getBorrowCount)),
                                        Optional::get))));
    }
}