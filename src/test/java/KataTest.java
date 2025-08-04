import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KataTest {

    @Test
    void testBasicConversion() {
        assertEquals("hELLO wORLD!", Kata.alternateCase("Hello World!"));
    }

    @Test
    void testNumbersIncluded() {
        assertEquals("JAVA123", Kata.alternateCase("java123"));
        assertEquals("java123", Kata.alternateCase("JAVA123"));
    }

    @Test
    void testSymbolsOnly() {
        assertEquals("123!@#", Kata.alternateCase("123!@#"));
    }

    @Test
    void testMixedCase() {
        assertEquals("tEsTiNg", Kata.alternateCase("TeStInG"));
    }

    @Test
    void testEmptyString() {
        assertEquals("", Kata.alternateCase(""));
    }

    @Test
    void testSingleCharacters() {
        assertEquals("A", Kata.alternateCase("a"));
        assertEquals("a", Kata.alternateCase("A"));
    }
}