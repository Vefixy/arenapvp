package com.arenapvp.util;

import com.arenapvp.util.TopPlaceholderParser.TopCategory;
import com.arenapvp.util.TopPlaceholderParser.TopField;
import com.arenapvp.util.TopPlaceholderParser.TopRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TopPlaceholderParserTest {

    @Test
    void parsesTopKillsName() {
        TopRequest request = TopPlaceholderParser.parse("top_kills_3_name");
        assertNotNull(request);
        assertEquals(TopCategory.KILLS, request.category());
        assertEquals(3, request.position());
        assertEquals(TopField.NAME, request.field());
    }

    @Test
    void parsesTopDeathsValueAlias() {
        TopRequest request = TopPlaceholderParser.parse("top_deaths_10_val");
        assertNotNull(request);
        assertEquals(TopCategory.DEATHS, request.category());
        assertEquals(10, request.position());
        assertEquals(TopField.VALUE, request.field());
    }

    @Test
    void parsesTopStreakKillsField() {
        TopRequest request = TopPlaceholderParser.parse("top_streak_1_kills");
        assertNotNull(request);
        assertEquals(TopCategory.STREAK, request.category());
        assertEquals(1, request.position());
        assertEquals(TopField.KILLS, request.field());
    }

    @Test
    void rejectsUnknownPlaceholder() {
        assertNull(TopPlaceholderParser.parse("kills"));
        assertNull(TopPlaceholderParser.parse("top_wins_1_name"));
    }
}
