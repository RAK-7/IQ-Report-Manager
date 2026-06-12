import org.springframework.scheduling.support.CronExpression;

public class TestCron {
    public static void main(String[] args) {
        try {
            CronExpression.parse("0 8 * * *");
            System.out.println("5 fields works!");
        } catch (Exception e) {
            System.out.println("5 fields FAILED: " + e.getMessage());
        }
        try {
            CronExpression.parse("0 0 8 * * *");
            System.out.println("6 fields works!");
        } catch (Exception e) {
            System.out.println("6 fields FAILED: " + e.getMessage());
        }
    }
}
