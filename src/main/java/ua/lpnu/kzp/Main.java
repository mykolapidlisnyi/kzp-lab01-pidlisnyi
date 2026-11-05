package ua.lpnu.kzp;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Головний клас програми для лабораторної роботи № 1 (Варіант 20).
 * Здійснює зчитування CSV-файлу обладнання, валідацію полів та формування звіту.
 *
 * @author Підлісний Микола Олександрович
 * @version 1.0.0
 */
public class Main {

    public static final String VERSION = "1.0.0";
    public static final String STUDENT_NAME = "Підлісний Микола Олександрович";
    public static final int VARIANT = 20;

    /**
     * Запис для збереження валідованих даних про одиницю обладнання.
     */
    public record Record(String date, String name, String category, int quantity, double price) {}

    /**
     * Точка входу в програму.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        String inputPath = "data/input.csv";
        String outputPath = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help":
                    printHelp();
                    return;
                case "--version":
                    System.out.println("Версія програми: " + VERSION + " | Автор: " + STUDENT_NAME + " (Варіант " + VARIANT + ")");
                    return;
                case "--input":
                    if (i + 1 < args.length) {
                        inputPath = args[++i];
                    } else {
                        System.err.println("Помилка: Не вказано шлях до вхідного файла після --input");
                        System.exit(2);
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        outputPath = args[++i];
                    } else {
                        System.err.println("Помилка: Не вказано шлях до вихідного файла після --output");
                        System.exit(2);
                    }
                    break;
                default:
                    if (args[i].startsWith("-")) {
                        System.err.println("Невідомий аргумент: " + args[i]);
                        printHelp();
                        System.exit(2);
                    } else {
                        System.err.println("Помилка: Неочікуваний позиційний аргумент: " + args[i]);
                        printHelp();
                        System.exit(2);
                    }
            }
        }

        Path input = Path.of(inputPath);
        if (!Files.exists(input)) {
            System.err.println("Помилка: Файл не знайдено за шляхом: " + input.toAbsolutePath());
            System.exit(1);
        }

        if (outputPath != null) {
            Path output = Path.of(outputPath);
            if (input.toAbsolutePath().normalize().equals(output.toAbsolutePath().normalize())) {
                System.err.println("Помилка: Вихідний файл не може збігатися з вхідним CSV-файлом!");
                System.exit(1);
            }
        }

        List<Record> validRecords = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(";", -1);
                if (parts.length < 5) {
                    System.err.printf("Рядок %d пропущено: Недостатньо полів (очікується 5, отримано %d)%n", lineNumber, parts.length);
                    continue;
                }

                String date = parts[0].trim();
                String name = parts[1].trim();
                String category = parts[2].trim();
                String qtyStr = parts[3].trim();
                String priceStr = parts[4].trim();

                if (date.isEmpty() || name.isEmpty() || category.isEmpty()) {
                    System.err.printf("Рядок %d пропущено: Текстові поля не можуть бути порожніми%n", lineNumber);
                    continue;
                }

                int quantity;
                try {
                    quantity = Integer.parseInt(qtyStr);
                    if (quantity <= 0) {
                        System.err.printf("Рядок %d пропущено: Кількість повинна бути більшою за 0 (отримано %d)%n", lineNumber, quantity);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.err.printf("Рядок %d пропущено: Помилка розбору кількості '%s'%n", lineNumber, qtyStr);
                    continue;
                }

                double price;
                try {
                    price = Double.parseDouble(priceStr);
                    if (!Double.isFinite(price) || price <= 0.0) {
                        System.err.printf("Рядок %d пропущено: Ціна повинна бути скінченним числом більшим за 0.0 (отримано %s)%n", lineNumber, priceStr);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.err.printf("Рядок %d пропущено: Помилка розбору ціни '%s'%n", lineNumber, priceStr);
                    continue;
                }

                validRecords.add(new Record(date, name, category, quantity, price));
            }
        } catch (IOException e) {
            System.err.println("Помилка читання файла: " + e.getMessage());
            System.exit(1);
        }

        String report = generateReport(validRecords);
        System.out.print(report);

        if (outputPath != null) {
            try {
                Path output = Path.of(outputPath);
                Path parent = output.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.writeString(output, report, StandardCharsets.UTF_8);
                System.out.println("Звіт успішно збережено у файл: " + output.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Помилка запису вихідного файла: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    /**
     * Формує підсумковий звіт за списками відвалідованих записів.
     *
     * @param records список коректних записів
     * @return сформований текст звіту
     */
    public static String generateReport(List<Record> records) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "=== ЗВІТ ОБРОБКИ ДАНИХ (Варіант %d) ===%n", VARIANT));
        sb.append(String.format(Locale.ROOT, "Студент: %s%n", STUDENT_NAME));
        sb.append("---------------------------------------\n");

        if (records.isEmpty()) {
            sb.append("Не знайдено жодного коректного запису для обробки.\n");
            return sb.toString();
        }

        int totalValidRecords = records.size();
        double totalSum = 0.0;
        double maxRecordCost = -1.0;
        String maxCostItemName = "";

        Map<String, Integer> categoryCounts = new LinkedHashMap<>();

        for (Record r : records) {
            double cost = r.quantity() * r.price();
            totalSum += cost;

            if (cost > maxRecordCost) {
                maxRecordCost = cost;
                maxCostItemName = r.name();
            }

            categoryCounts.put(r.category(), categoryCounts.getOrDefault(r.category(), 0) + r.quantity());
        }

        String topCategory = "";
        int maxCategoryQty = -1;
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            if (entry.getValue() > maxCategoryQty) {
                maxCategoryQty = entry.getValue();
                topCategory = entry.getKey();
            }
        }

        sb.append(String.format(Locale.ROOT, "1. Усього оброблено коректних записів: %d%n", totalValidRecords));
        sb.append(String.format(Locale.ROOT, "2. Загальна вартість обладнання: %.2f грн%n", totalSum));
        sb.append(String.format(Locale.ROOT, "3. Позиція з найвищою вартістю: %s (%.2f грн)%n", maxCostItemName, maxRecordCost));
        sb.append(String.format(Locale.ROOT, "4. Найпопулярніша категорія: %s (всього одиниць: %d)%n", topCategory, maxCategoryQty));
        sb.append("---------------------------------------\n");

        return sb.toString();
    }

    private static void printHelp() {
        System.out.println("Використання: java -jar target/lab01-pidlisnyi-1.0.0-all.jar [опції]");
        System.out.println("Опції:");
        System.out.println("  --help            Показати цю довідку");
        System.out.println("  --version         Вивести версію програми та інформацію про автора");
        System.out.println("  --input <path>    Вказати шлях до вхідного CSV-файла (за замовчуванням: data/input.csv)");
        System.out.println("  --output <path>   Вказати шлях для збереження вихідного звіту");
    }
}