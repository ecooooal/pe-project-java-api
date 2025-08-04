public class SumOfMultiples {
    public static int sumOfMultiples(int limit, int[] divisors) {
        int sum = 0;
        for (int i = 1; i < limit; i++) {
            for (int div : divisors) {
                if (i % div == 0) {
                    sum += i;
                    break; 
                }
            }
        }
        return sum;
    }
}