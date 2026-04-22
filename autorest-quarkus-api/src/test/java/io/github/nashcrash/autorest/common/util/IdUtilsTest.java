package io.github.nashcrash.autorest.common.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Base64;

public class IdUtilsTest {

    @Test
    public void test_Instant() {
        String s = IdUtils.generateId(Instant.now());
        String s1 = new String(Base64.getDecoder().decode(s));
        System.out.println(s1);
    }
}
