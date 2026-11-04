package com.dremixam.pastureloot.override;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverridesFileTest {

    /** Number of [[override]] blocks in the example text. */
    private static final int EXAMPLE_RULES = 5;

    @TempDir
    Path directory;

    private final List<String> warnings = new ArrayList<>();

    private Path file() {
        return directory.resolve(OverridesFile.FILE_NAME);
    }

    private List<OverrideRule> loadText(String text) throws IOException {
        Files.writeString(file(), text, StandardCharsets.UTF_8);
        return OverridesFile.load(file(), warnings::add);
    }

    @Test
    void missingFileIsWrittenWithoutActiveRule() throws IOException {
        assertEquals(List.of(), OverridesFile.load(file(), warnings::add));
        assertTrue(Files.exists(file()));
        assertEquals(List.of(), OverridesFile.load(file(), warnings::add));
        assertEquals(List.of(), warnings);
    }

    @Test
    void everyExampleIsAValidRule() throws IOException {
        String examples = OverridesExample.TEXT.substring(OverridesExample.TEXT.indexOf("# [[override]]"));
        String uncommented = examples.lines()
                .map(line -> line.startsWith("# ") ? line.substring(2) : "")
                .reduce("", (text, line) -> text + line + "\n");
        List<OverrideRule> rules = loadText(uncommented);
        assertEquals(EXAMPLE_RULES, rules.size(), uncommented);
        assertEquals(List.of(), warnings);
    }

    @Test
    void fullRuleIsRead() throws IOException {
        List<OverrideRule> rules = loadText("""
                [[override]]
                pokemon = "gastly"
                mode = "replace"
                rate_multiplier = 2
                biomes = ["#minecraft:is_nether"]
                drops = [{ item = "minecraft:soul_sand", quantity = "1-3", chance = 5 }, { item = "minecraft:stick" }]
                """);
        OverrideRule rule = rules.get(0);
        assertEquals(OverrideRule.Mode.REPLACE, rule.mode());
        assertEquals(2, rule.rateMultiplier());
        assertEquals(List.of(new DropSpec("minecraft:soul_sand", 1, 3, 5),
                new DropSpec("minecraft:stick", 1, 1, DropSpec.ALWAYS)), rule.drops());
        assertEquals(List.of(), warnings);
    }

    @Test
    void brokenRulesAndDropsAreSkippedWithAWarning() throws IOException {
        List<OverrideRule> rules = loadText("""
                [[override]]
                mode = "add"

                [[override]]
                pokemon = "vulpix"
                mode = "swap"

                [[override]]
                pokemon = "mareep"
                drops = [{ quantity = 2 }, { item = "minecraft:white_wool", quantity = "3-1" }, { item = "minecraft:string" }]
                """);
        assertEquals(1, rules.size());
        assertEquals(List.of(new DropSpec("minecraft:string", 1, 1, DropSpec.ALWAYS)), rules.get(0).drops());
        assertEquals(4, warnings.size(), warnings.toString());
    }

    @Test
    void unknownKeysAreReported() throws IOException {
        loadText("[[override]]\npokemon = \"vulpix\"\nchanse = 5\n");
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("chanse"));
    }

    @Test
    void quantitiesAreReadAsNumbersOrRanges() {
        assertArrayEquals(new int[]{2, 2}, QuantityRange.parse(2));
        assertArrayEquals(new int[]{1, 3}, QuantityRange.parse("1-3"));
        assertArrayEquals(new int[]{4, 4}, QuantityRange.parse(" 4 "));
        assertNull(QuantityRange.parse("3-1"));
        assertNull(QuantityRange.parse(-1));
        assertNull(QuantityRange.parse(1.5));
        assertNull(QuantityRange.parse("a lot"));
    }
}
