package com.joeyoey.spacestacker.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JaroAlgTest {

    @Test
    void exactMatchShouldHaveHighScore() {
        double score = JaroAlg.getJaroWinkler("zombie", "zombie");
        assertTrue(score >= 99.0);
    }
}
