package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testGenerateReportWithValidData() {
        List<Main.Record> records = List.of(
            new Main.Record("2026-10-01", "Сервер Dell", "Сервери", 2, 50000.0),
            new Main.Record("2026-10-02", "Комутатор Cisco", "Мережі", 5, 10000.0)
        );

        String report = Main.generateReport(records);

        assertTrue(report.contains("Усього оброблено коректних записів: 2"));
        assertTrue(report.contains("150000.00 грн"));
        assertTrue(report.contains("Сервер Dell"));
        assertTrue(report.contains("Мережі"));
    }

    @Test
    void testGenerateReportEmptyList() {
        String report = Main.generateReport(List.of());
        assertTrue(report.contains("Не знайдено жодного коректного запису"));
    }

    @Test
    void testRecordValidations() {
        Main.Record record = new Main.Record("2026-10-01", "Ноутбук Lenovo", "ПК", 10, 25000.50);
        assertEquals("2026-10-01", record.date());
        assertEquals("Ноутбук Lenovo", record.name());
        assertEquals("ПК", record.category());
        assertEquals(10, record.quantity());
        assertEquals(25000.50, record.price());
    }
}