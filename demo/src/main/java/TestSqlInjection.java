import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class TestSqlInjection {

    // 高危：Statement 执行拼接SQL
    public void testStatement(String userId) throws Exception {
        Connection conn = DriverManager.getConnection("url");
        Statement stmt = conn.createStatement();

        // 高危！直接拼接
        String sql = "SELECT * FROM user WHERE id = " + userId;
        stmt.executeQuery(sql);
    }

    // 高危：String.format 拼接
    public void testFormat(String name) {
        String sql = String.format("INSERT INTO user(name) VALUES ('%s')", name);
    }

    // 高危：+ 号拼接
    public void testConcat(String id) {
        String sql = "DELETE FROM user WHERE id = " + id;
    }
}