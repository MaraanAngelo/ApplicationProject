package com.example.applicationproject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TaskTest {
    @Test
    public void convertsFormDateToDatabaseFormat() {
        assertEquals("2026-11-21", Task.toDatabaseDate("11/21/2026"));
    }

    @Test
    public void acceptsDatabaseDateWhenEditing() {
        assertEquals("2026-11-21", Task.toDatabaseDate("2026-11-21"));
        assertEquals("11/21/2026", Task.toFormDate("2026-11-21"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidDate() {
        Task.toDatabaseDate("02/30/2026");
    }

    @Test
    public void normalizesOptionalTime() {
        assertEquals("09:05:00", Task.toDatabaseTime("9:05"));
        assertEquals("21:05:00", Task.toDatabaseTime("9:05 PM"));
        assertEquals("", Task.toDatabaseTime(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidTime() {
        Task.toDatabaseTime("25:80");
    }
}
