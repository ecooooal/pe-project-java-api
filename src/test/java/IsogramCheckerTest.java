import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IsogramCheckerTest {

    @Test
    public void testIsograms() {
        assertTrue(IsogramChecker.isIsogram("machine"));
        assertTrue(IsogramChecker.isIsogram("isogram"));
        assertTrue(IsogramChecker.isIsogram("")); // Empty string
    }

    @Test
    public void testNotIsograms() {
        assertFalse(IsogramChecker.isIsogram("Alphabet")); // A appears twice (case-insensitive)
        assertFalse(IsogramChecker.isIsogram("hello"));
        assertFalse(IsogramChecker.isIsogram("IsIsogram"));
    }

    @Test
    public void testNullInput() {
        assertTrue(IsogramChecker.isIsogram(null));
    }

    @Test
    public void testWithSymbolsAndSpaces() {
        assertTrue(IsogramChecker.isIsogram("Dog!"));
        assertFalse(IsogramChecker.isIsogram("No lemon, no melon")); // e, n, o repeat
    }
}