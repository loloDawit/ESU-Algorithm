package esu.algorithm.ui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The dark stylesheet restates only colours, layered over the base one. That
 * is only safe while it restates *every* colour: a class the base sheet gives
 * a dark text colour, and the dark sheet forgets, renders dark on dark.
 *
 * That happened to .log-line, and was invisible until someone looked at the
 * running app. This makes it fail here instead.
 */
public class ThemeTest {

    private static final Pattern RULE =
            Pattern.compile("([^{}]+)\\{([^}]*)\\}");
    private static final Pattern COLOUR =
            Pattern.compile("-fx-(text-)?fill\\s*:");

    private String read(String name) throws IOException {
        try (InputStream in = TreeRenderer.class.getResourceAsStream(name)) {
            assertNotNull(in, name + " is not on the classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Selectors in a stylesheet that set a fill or text colour. */
    private Set<String> colouredSelectors(String css) {
        Set<String> found = new TreeSet<>();
        Matcher rule = RULE.matcher(css);
        while (rule.find()) {
            if (!COLOUR.matcher(rule.group(2)).find()) {
                continue;
            }
            for (String selector : rule.group(1).split(",")) {
                String trimmed = selector.trim();
                trimmed = trimmed.substring(trimmed.lastIndexOf('\n') + 1).trim();
                if (trimmed.startsWith(".")) {
                    found.add(trimmed);
                }
            }
        }
        return found;
    }

    @Test
    public void darkRestatesEveryColourTheBaseSheetSets() throws IOException {
        String light = read("esu.css");
        String dark = read("dark.css");

        List<String> missing = new ArrayList<>();
        for (String selector : colouredSelectors(light)) {
            String leaf = selector.substring(selector.lastIndexOf(' ') + 1);
            if (!dark.contains(leaf)) {
                missing.add(selector);
            }
        }

        assertEquals(List.of(), missing,
                "these set a colour in esu.css but are never restated in "
                + "dark.css, so they keep their light colour on a dark "
                + "background");
    }

    @Test
    public void bothSheetsUseWellFormedColours() throws IOException {
        for (String name : List.of("esu.css", "dark.css")) {
            Matcher hex = Pattern.compile("#[0-9a-zA-Z]+").matcher(read(name));
            while (hex.find()) {
                String value = hex.group();
                assertTrue(value.matches("#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})"),
                        name + " has a malformed colour: " + value);
            }
        }
    }
}
