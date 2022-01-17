package search;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {

        String fileName = args[0].equals("--data") ? args[1] : null;
        makeIndex(fileName);
        boolean quit = false;
        while (!quit) {
            printMenu();
            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    System.out.println("Select a matching strategy: ALL, ANY, NONE");
                    String strategy = scanner.nextLine().toUpperCase();
                    Finder finder = null;
                    switch (strategy) {
                        case "ALL":
                            finder = new Finder(new AllFindingStrategy(getQuery()));
                            break;
                        case "ANY":
                            finder = new Finder(new AnyFindingStrategy(getQuery()));
                            break;
                        case "NONE":
                            finder = new Finder(new NoneFindingStrategy(getQuery()));
                            break;
                        default:
                            System.out.println("Wrong strategy Try again.");
                            break;
                    }
                    assert finder != null;
                    printResults(finder.find(invertedIndex, lines));
                    break;
                case "2":
                    printLines();
                    break;
                case "0":
                    System.out.println("Bye!");
                    quit = true;
                default:
                    System.out.println("Incorrect option! Try again.");
                    break;
            }
        }
    }

    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<Integer, String> lines = new HashMap<>(); // K-line number V-sentence as a line
    private static final Map<String, List<Integer>> invertedIndex = new HashMap<>(); //K-word V-listOfLines where word occurs

    public static void printMenu() {
        System.out.println("\n === Menu ===\n" +
                "1. Find a person\n" +
                "2. Print all people\n" +
                "0. Exit");
    }

    public static void makeIndex(String fileName) {
        int count = 0;
        String line;
        File file = new File(fileName);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                line = scanner.nextLine().trim();
                lines.put(count, line);
                String[] words = line.toLowerCase().split("\\s+");
                for (String word : words) {
                    if (!invertedIndex.containsKey(word)) {
                        invertedIndex.put(word, new ArrayList<>());
                    }
                    invertedIndex.get(word).add(count);
                }
                count++;  // counting scanned lines
            }
        } catch (FileNotFoundException e) {
            System.out.println("No file found: " + fileName);
        }
    }

    public static String[] getQuery() {
        String input;
        do {
            System.out.println("Enter a name or email to search all suitable people.");
            input = scanner.nextLine().trim();
        } while (input.isEmpty());

        return input.toLowerCase().split(" ");
    }

    public static void printResults(List<Integer> indexes) {
        if (indexes == null) {
            System.out.println("No matching people found.");
            return;
        }
        System.out.println(indexes.size() + " persons found:");
        indexes.stream()
                .filter(lines::containsKey)
                .forEach(idx -> System.out.println(lines.get(idx)));
    }

    public static void printLines() {
        System.out.println("=== List of people === ");
        for (String l : lines.values()) {
            System.out.println(l);
        }
    }
}

class Finder {

    public FindingStrategy strategy;

    public Finder(FindingStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Integer> find(Map<String, List<Integer>> invertedIndex, Map<Integer, String> lines) {
        return this.strategy.getResults(invertedIndex, lines);
    }
}

interface FindingStrategy {
    List<Integer> getResults(Map<String, List<Integer>> invertedIndex, Map<Integer, String> lines);
}

class AllFindingStrategy implements FindingStrategy {

    private final String[] query;

    public AllFindingStrategy(String[] query) {
        this.query = query;
    }

    public List<Integer> getResults(Map<String, List<Integer>> invertedIndex, Map<Integer, String> lines) {
        List<List<Integer>> queryLinesLists = Arrays.stream(query)
                //if query is not found .map list = null
                .map(invertedIndex::get)
                .collect(Collectors.toList());

        List<Integer> commons;
        if (queryLinesLists.get(0) != null) {
            commons = new ArrayList<>(queryLinesLists.get(0));
            for (var list : queryLinesLists) {
                commons.retainAll(list);
            }
        } else {
            commons = new ArrayList<>();
        }
        return commons;
    }
}

class AnyFindingStrategy implements FindingStrategy {

    private final String[] query;

    public AnyFindingStrategy(String[] query) {
        this.query = query;
    }

    //  list of line numbers (map values) where query (key) is present - UNION
    public List<Integer> getResults(Map<String, List<Integer>> invertedIndex, Map<Integer, String> lines) {
        return Arrays.stream(query)
                .map(invertedIndex::get)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }
}

class NoneFindingStrategy implements FindingStrategy {

    private final String[] query;

    public NoneFindingStrategy(String[] query) {
        this.query = query;
    }

    //  list of line numbers (map values) where query (key) is absent - DIFFERENCE
    public List<Integer> getResults(Map<String, List<Integer>> invertedIndex, Map<Integer, String> lines) {
        List<Integer> queryLinesList = Arrays.stream(query)
                .map(invertedIndex::get)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        List<Integer> difList = new ArrayList<>(lines.keySet());
        difList.removeAll(queryLinesList);
        return difList;
    }
}



