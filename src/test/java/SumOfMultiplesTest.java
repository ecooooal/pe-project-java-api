import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SumOfMultiplesTest {

    @Test
    public void testEdgeCases() {
        assertEquals(0, SumOfMultiples.sumOfMultiples(0, new int[]{1}));
        assertEquals(0, SumOfMultiples.sumOfMultiples(10, new int[]{}));
        assertEquals(0, SumOfMultiples.sumOfMultiples(10, null));
    }

    @Test
    public void testNegativeLimitReturnsZero() {
        assertEquals(0, SumOfMultiples.sumOfMultiples(-10, new int[]{3, 5}));
    }

    @Test
    public void testLargeInput() {
        int result = SumOfMultiples.sumOfMultiples(10000, new int[]{3, 5});
        assertTrue(result > 0); // sanity check
    }
}