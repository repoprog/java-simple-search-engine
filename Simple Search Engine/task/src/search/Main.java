package search;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        String fileName = args[0].equals("--data") ? args[1] : null;
        System.out.println("Enter all people:");
        makeIndex(fileName);
        boolean quit = false;
        while (!quit) {
            printMenu();
            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    getQuery();
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
    private static final Map<Integer, String> lines = new HashMap<>(); // line number/ sentence as a line
    private static Map<String, List<Integer>> invertedIndex = new HashMap<>(); // word in line, lines where word occurs
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

    public static void getQuery() {
        System.out.println("Enter a name or email to search all suitable people.");
        String query = scanner.nextLine().toLowerCase();
        searchForQuery(query);
    }

    public static void searchForQuery(String query) {
        List<Integer> index;
        if (invertedIndex.containsKey(query)) {
            index = invertedIndex.get(query); // assigns lost of values(lines numbers)
            // from key (word) to new list;
            System.out.println(index.size() + " persons found:");
            for (Integer idx : index) {
                if (lines.containsKey(idx)) {
                    System.out.println(lines.get(idx));
                }
            }
        } else {
            System.out.println("No matching people found.");
        }
    }

    public static void printLines() {
        System.out.println("=== List of people === ");
        for (String l : lines.values()) {
            System.out.println(l);
        }
    }
}

