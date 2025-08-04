import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PrimeCheckerTest {

    PrimeChecker checker = new PrimeChecker();

    @Test
    void testPrimes() {
        assertTrue(checker.isPrime(2));
        assertTrue(checker.isPrime(3));
        assertTrue(checker.isPrime(5));
        assertTrue(checker.isPrime(13));
    }

    @Test
    void testNonPrimes() {
        assertFalse(checker.isPrime(0));
        assertFalse(checker.isPrime(1));
        assertFalse(checker.isPrime(4));
        assertFalse(checker.isPrime(100));
    }

    @Test
    void testNegativeNumbers() {
        assertFalse(checker.isPrime(-3));
        assertFalse(checker.isPrime(-7));
    }
}