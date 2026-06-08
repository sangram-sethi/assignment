package customer;

public class Customer {
    private String customerId;
    private String name;
    private final String pan;
    private int creditScore;

    public Customer(String customerId,
                    String name,
                    String pan,
                    int creditScore) {

        this.customerId = customerId;
        this.name = name;
        this.pan = pan;
        setCreditScore(creditScore);
    }

    public void setCreditScore(int score) {

        if(score < 300 || score > 900) {
            throw new IllegalArgumentException("Invalid Score");
        }

        this.creditScore = score;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public String getPan() {
        return pan;
    }
}