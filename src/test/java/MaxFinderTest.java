import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class MaxFinderTest {

    @Test
    public void testFindMax() {
        assertEquals(20, MaxFinder.findMax(10, 20, 15));
        assertEquals(30, MaxFinder.findMax(30, 20, 10));
        assertEquals(25, MaxFinder.findMax(25, 25, 25));
        assertEquals(50, MaxFinder.findMax(50, 40, 50));
    }
    @Test
    public void testindMax() {
        assertEquals(20, MaxFinder.findMax(10, 20, 15));
        assertEquals(30, MaxFinder.findMax(30, 20, 10));
        assertEquals(25, MaxFinder.findMax(25, 25, 25));
        assertEquals(50, MaxFinder.findMax(50, 40, 50));
    }
}