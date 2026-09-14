package io.github.nashcrash.autorest.common.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class MultipleDateTimeFormatParserTest {

    @Test
    public void testAtTime1() {
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd@T(12:34:56.789)@Z(Europe/Rome)"};
        String testDate = "2025-01-12";
        Date date = MultipleDateTimeFormatParser.parseDate(testDate, patterns, null);
        Assertions.assertEquals(1736681696789L, date.getTime());
    }
    @Test
    public void testAtTime2() {
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd'T'HH:mm", "dd.MM.yyyy@T(00:00:00)@Z(Europe/Rome)"};
        String testDate = "12.01.2025";
        Date date = MultipleDateTimeFormatParser.parseDate(testDate, patterns, null);
        Assertions.assertEquals(1736636400000L, date.getTime());
    }
    @Test
    public void testAtTime3() {
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd'T'HH:mm", "dd/MM/yyyy@T(00:00:00)"};
        String testDate = "12/01/2025";
        Date date = MultipleDateTimeFormatParser.parseDate(testDate, patterns, null);
        Assertions.assertEquals(1736636400000L, date.getTime());
    }
    @Test
    public void testAtTime4() {
        String[] patterns = {"yyyy-MM-dd HH:mm:ss@Z(America/Los_Angeles)", "dd/MM/yyyy HH:mm:ss@Z(America/Los_Angeles)"};
        String testDate = "12/01/2025 12:23:52";
        Date date = MultipleDateTimeFormatParser.parseDate(testDate, patterns, null);
        Assertions.assertEquals(1736713432000L, date.getTime());
    }
}
