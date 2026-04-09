import io.javalin.Javalin;

public class TestJavalin {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        });
        System.out.println("Javalin created");
    }
}
