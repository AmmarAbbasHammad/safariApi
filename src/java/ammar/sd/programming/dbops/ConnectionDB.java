package ammar.sd.programming.dbops;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            String user;
            String password;
            String driver = "com.mysql.jdbc.Driver";
            String url = "jdbc:mysql://localhost:3306/safari?characterEncoding=UTF-8";
            user = "dalas";
            password = "awaz@321";
//            user = "root";
//            password = "";
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception cnfe) {
            System.out.println("Exception Safari DB" + cnfe);
        }
        return connection;
    }
}
