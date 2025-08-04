import java.util.HashSet;
import java.util.Set;

public class IsogramChecker {
    public static boolean isIsogram(String word) {
        if (word == null) return true;

        Set<Character> seen = new HashSet<>();
        for (char c : word.toLowerCase().toCharArray()) {
            if (Character.isLetter(c)) {
                if (seen.contains(c)) {
                    return false;
                }
                seen.add(c);
            }
        }
        return true;
    }
}