package liquibase.test.liquibaseTest;

public class SQLUtils {
    public static String stripColumnTypeModifiers(String type) {
        int startPos = type.indexOf("(");
        if (startPos != -1) {
            int endPos = type.lastIndexOf(")");
            if (endPos != -1) {
                return type.substring(0, startPos);
            }
        }
        return type;
    }
}
