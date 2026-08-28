// TypingTrainer.java
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TypingTrainer {
    @Parameter(names = "--length")
    private int length = 50;
    @Parameter(names = "--charset")
    private String charset = "all";
    @Parameter(names = "--mode")
    private String mode = "char";
    @Parameter(names = "--language")
    private String language = "en";
    @Parameter(names = "--export-stats")
    private String exportFile;

    private String text;
    private int errors;
    private int totalChars;
    private long startTime;
    private long endTime;
    private Scanner scanner = new Scanner(System.in);

    public void run() throws Exception {
        text = generateText();
        System.out.println("\u001B[36mТренажер печати (символы)\u001B[0m");
        System.out.println("\u001B[33mВведите следующий текст как можно точнее и быстрее:\u001B[0m");
        System.out.println("\u001B[37m" + text + "\u001B[0m");
        System.out.println("\nНажмите Enter, чтобы начать...");
        scanner.nextLine();
        startTime = System.currentTimeMillis();
        System.out.print("\u001B[32m> \u001B[0m");
        String input = scanner.nextLine();
        endTime = System.currentTimeMillis();
        totalChars = input.length();
        int minLen = Math.min(text.length(), input.length());
        errors = 0;
        for (int i = 0; i < minLen; i++) {
            if (text.charAt(i) != input.charAt(i)) errors++;
        }
        if (input.length() > text.length()) errors += input.length() - text.length();
        double elapsed = (endTime - startTime) / 1000.0;
        double cpm = (totalChars / elapsed) * 60;
        double wpm = cpm / 5;
        double accuracy = totalChars > 0 ? ((double)(totalChars - errors) / totalChars * 100) : 0;
        System.out.println("\n\u001B[36m--- Результаты ---\u001B[0m");
        System.out.printf("\u001B[32mСкорость: %.1f симв/мин (%.1f слов/мин)\u001B[0m%n", cpm, wpm);
        System.out.printf("\u001B[33mТочность: %.1f%%\u001B[0m%n", accuracy);
        System.out.printf("\u001B[35mВремя: %.2f сек\u001B[0m%n", elapsed);
        System.out.printf("\u001B[31mОшибок: %d\u001B[0m%n", errors);
        Map<String, Object> stats = new HashMap<>();
        stats.put("cpm", cpm); stats.put("wpm", wpm); stats.put("accuracy", accuracy); stats.put("time", elapsed); stats.put("errors", errors);
        if (exportFile != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.write(Paths.get(exportFile), gson.toJson(stats).getBytes());
            System.out.println("Статистика сохранена в " + exportFile);
        }
    }

    private String getCharset() {
        String letters = language.equals("ru") ?
            "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" :
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String symbols = "!@#$%^&*()_+-=,.;:?/[]{}|";
        if (charset.equals("letters")) return letters;
        if (charset.equals("alnum")) return letters + digits;
        return letters + digits + symbols;
    }

    private String generateText() {
        String charsetStr = getCharset();
        Random rand = new Random();
        if (mode.equals("word")) {
            int wordCount = Math.max(1, length / 5);
            List<String> words = new ArrayList<>();
            for (int i = 0; i < wordCount; i++) {
                int wl = 3 + rand.nextInt(6);
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < wl; j++) sb.append(charsetStr.charAt(rand.nextInt(charsetStr.length())));
                words.add(sb.toString());
            }
            return String.join(" ", words);
        } else if (mode.equals("sentence")) {
            int sentCount = Math.max(1, length / 30);
            List<String> sentences = new ArrayList<>();
            for (int i = 0; i < sentCount; i++) {
                int wc = 5 + rand.nextInt(8);
                List<String> words = new ArrayList<>();
                for (int j = 0; j < wc; j++) {
                    int wl = 3 + rand.nextInt(6);
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < wl; k++) sb.append(charsetStr.charAt(rand.nextInt(charsetStr.length())));
                    words.add(sb.toString());
                }
                String sent = String.join(" ", words);
                sent = sent.substring(0,1).toUpperCase() + sent.substring(1) + ".";
                sentences.add(sent);
            }
            return String.join(" ", sentences);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) sb.append(charsetStr.charAt(rand.nextInt(charsetStr.length())));
            return sb.toString();
        }
    }

    public static void main(String[] args) throws Exception {
        TypingTrainer trainer = new TypingTrainer();
        JCommander.newBuilder().addObject(trainer).build().parse(args);
        trainer.run();
    }
}
