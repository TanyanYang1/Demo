import java.sql.*;
import javax.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;

public class SQLInjectTest {
    public void testJavaSQLInject(HttpServletRequest request) throws SQLException {
        // 1. Statement 执行SQL（对应 Statement.execute/ executeQuery/ executeUpdate 规则）
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "123456");
        Statement stmt = conn.createStatement();
        stmt.execute("SELECT * FROM user WHERE id = 1"); // 触发 execute 规则
        stmt.executeQuery("SELECT * FROM user WHERE name = 'test'"); // 触发 executeQuery 规则
        stmt.executeUpdate("UPDATE user SET age = 20 WHERE id = 1"); // 触发 executeUpdate 规则

        // 2. 字符串拼接SQL（SELECT/INSERT/UPDATE/DELETE 拼接）
        String userId = request.getParameter("id");
        String sql1 = "SELECT * FROM user WHERE id = " + userId; // 触发 SELECT 拼接规则
        String sql2 = "INSERT INTO user(name) VALUES ('" + userId + "')"; // 触发 INSERT 拼接规则
        String sql3 = "UPDATE user SET name = '" + userId + "' WHERE id = 1"; // 触发 UPDATE 拼接规则
        String sql4 = "DELETE FROM user WHERE id = " + userId; // 触发 DELETE 拼接规则

        // 3. PreparedStatement 直接拼接请求参数
        PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM user WHERE id = ?");
        pstmt.setString(1, request.getParameter("id")); // 触发 PreparedStatement.setString + request.getParameter 规则

        // 4. JdbcTemplate + String.format 拼接
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.query(String.format("SELECT * FROM user WHERE id = %s", userId)); // 触发 JdbcTemplate.query + String.format 规则

        stmt.close();
        pstmt.close();
        conn.close();
    }
}

// MyBatis XML 测试示例（UserMapper.xml）
/*
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.test.UserMapper">
    <!-- 1. ${} 参数拼接风险 -->
    <select id="selectUserById" resultType="com.test.User">
        SELECT * FROM user WHERE id = ${id}
    </select>

    <!-- 2. 动态SQL单条件风险（<where> 标签） -->
    <select id="selectUser" resultType="com.test.User">
        SELECT * FROM user
        <where>
            <if test="id != null">AND id = #{id}</if>
        </where>
    </select>

    <!-- 3. LIKE 拼接风险 -->
    <select id="selectUserByName" resultType="com.test.User">
        SELECT * FROM user WHERE name LIKE '%${name}%'
    </select>
</mapper>
*/