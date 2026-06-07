import java.util.Scanner;

public class Factors {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a number: ");
        int num = sc.nextInt();

        int count = 0;
        int sum = 0;

        System.out.println("Factors:");

        for (int i = 1; i <= num; i++) {
            if (num % i == 0) {
                System.out.print(i + " ");
                count++;
                sum += i;
            }
        }

        System.out.println("\nCount of Factors = " + count);
        System.out.println("Sum of Factors = " + sum);
    }
}
