package liquibase.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class ShenTong {
    public static String stUrl="jdbc:oscar://192.168.218.99:2003/osrdbnew";
    public static String stUserName="newuser";
    public static String stPass="11111";
    public static String stDriver="com.oscar.Driver";

    public static void main(String[] args) throws Exception {
        Class.forName(stDriver);
        Connection connection= DriverManager.getConnection(stUrl,stUserName,stPass);
        connection.getSchema();
        ResultSet resultSet=connection.getMetaData().getSchemas();
        while (resultSet.next()){
            System.out.println(resultSet.getString(1));
        }

    }
}
