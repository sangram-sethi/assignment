package loan;

public class LoanIdGenerator {

    private static int counter = 1000;

    public static String generate() {

        return "LN" + (++counter);
    }
}