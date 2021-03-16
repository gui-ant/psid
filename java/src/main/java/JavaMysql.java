import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JavaMysql {

    public static void main(String[] args) {
        String url = "jdbc:mysql://194.210.86.10:3306/aluno_g07"; //"http://194.210.86.10/phpmyadmin/db_structure.php?server=1&db=aluno_g07"
        String username = "aluno";
        String password = "aluno";

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connection established!!!");

            // teste inserir um user na tabela "users"
//           String sql = "INSERT INTO users (id, user, pass) VALUES (?, ?, ?)";
//            PreparedStatement statement = connection.prepareStatement(sql);
//            statement.setInt(1, 1);
//            statement.setString(2, "ze manel");
//            statement.setString(3, "password");
//
//            int rows = statement.executeUpdate();
//            if (rows > 0){
//                System.out.println("Inserted value successfully!!!");
//            }

            // teste ler todos os users da tabela "users"
            String sql = "SELECT * FROM users";
            Statement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery(sql);

            while (result.next()){
                String name = result.getString("name");
                String pass = result.getString("pass");
                System.out.println("User: " + name + "\n" + "Password: " + pass  );
            }

            //statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Connection failed!!!");
            e.printStackTrace();
        }
    }
}
