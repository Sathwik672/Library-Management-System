import java.sql.Connection;
import java.sql.DriverManager;

public class TestDB {
    public static void main(String[] args) {
        System.out.println("Starting test...");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Connecting...");
            Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000", "root", "");
            System.out.println("Connected!");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
