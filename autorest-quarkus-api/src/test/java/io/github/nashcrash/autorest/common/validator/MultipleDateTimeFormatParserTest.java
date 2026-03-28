package io.github.nashcrash.autorest.common.validator;

import org.junit.jupiter.api.Test;

import java.util.Date;

public class MultipleDateTimeFormatParserTest {

    @Test
    public void testAtTime1() {
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd@T12:34:56.789@TEurope/Rome"};
        String testDate = "2025-01-12";
        Date date = MultipleDateTimeFormatParser.parseDate(testDate, patterns, null);
        System.out.println(date);
    }
    @Test
    public void testAtTime2() {
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd'T'HH:mm", "dd.MM.yyyy@T00:00:00@TEurope/Rome"};
        String testDate = "12.01.2025";
        Date date = MultipleDateTimeFormatParser.parseDate(testDate, patterns, null);
        System.out.println(date);
    }
    @Test
    public void testAtTime3() {
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd'T'HH:mm", "dd/MM/yyyy@T00:00:00"};
        String testDate = "12/01/2025";
        Date date = MultipleDateTimeFormatParser.parseDate(testDate, patterns, null);
        System.out.println(date);
    }
}
