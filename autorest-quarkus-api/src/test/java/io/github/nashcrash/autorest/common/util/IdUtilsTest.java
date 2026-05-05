package io.github.nashcrash.autorest.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Base64;

public class IdUtilsTest {

    @Test
    public void test_Instant() {
        String testInstant = "2007-12-03T10:15:30.00Z";
        String encoded="MjAwNy0xMi0wM1QxMDoxNTozMFpbVVRDXQ==";
        String decoded="2007-12-03T10:15:30Z[UTC]";
        String s = IdUtils.generateId(Instant.parse(testInstant));
        String s1 = new String(Base64.getDecoder().decode(s));
        Assertions.assertEquals(encoded, s);
        Assertions.assertEquals(decoded, s1);
    }
}
