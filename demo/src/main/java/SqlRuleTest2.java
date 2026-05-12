import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Iterator;
import java.util.regex.Pattern;

public class SqlRuleTest2 {
    private static final String RULE_FILE_PATH = "C:\\Users\\19017\\Desktop\\rules\\demo\\src\\main\\java\\rules-Sql.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(new File(RULE_FILE_PATH));
            JsonNode javaRulesNode = rootNode.get("java");

            if (javaRulesNode == null || !javaRulesNode.isArray()) {
                System.err.println("未找到java规则");
                return;
            }

            Iterator<JsonNode> iterator = javaRulesNode.elements();
            int index = 0;

            while (iterator.hasNext()) {
                index++;
                JsonNode rule = iterator.next();
                String type = rule.get("type").asText();
                String keyStr = rule.get("keyStr").asText();
                String regExp = rule.get("regExp").asText();

                System.out.println("=====================================");
                System.out.println("测试规则 " + index + "：");
                System.out.println("类型：" + type);
                System.out.println("关键词：" + keyStr);
                System.out.println("正则表达式：" + regExp);

                String testCode = getTestCodeByRuleType(type);
                System.out.println("测试代码片段：\n" + testCode);

                // 最兼容写法，不用 find()
                boolean isMatched = Pattern.matches("(?s).*" + regExp + ".*", testCode);

                if (isMatched) {
                    System.out.println("✅ 规则匹配成功");
                } else {
                    System.out.println("❌ 规则匹配失败");
                }
                System.out.println("=====================================\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getTestCodeByRuleType(String type) {
        return switch (type) {
            case "Java SQL注入风险" -> getJavaSqlInjectionTestCode();
            case "MyBatis XML ${}参数拼接风险" -> getMyBatisDollarParamTestCode();
            case "MyBatis XML动态SQL单条件风险" -> getMyBatisDynamicSqlTestCode();
            case "MyBatis XML like拼接风险" -> getMyBatisLikeTestCode();
            default -> "// 无测试代码";
        };
    }

    private static String getJavaSqlInjectionTestCode() {
        return """
                import java.sql.Statement;
                import javax.servlet.http.HttpServletRequest;
                org.springframework.jdbc.core.JdbcTemplate;

                public class TestSqlInjection {
                    public void testStatement(HttpServletRequest request) throws Exception {
                        Statement stmt = null;
                        stmt.execute("SELECT * FROM user WHERE id = 1");
                        stmt.executeQuery("SELECT * FROM user WHERE name = 'test'");
                        stmt.executeUpdate("UPDATE user SET name = 'new' WHERE id = 1");

                        String userId = request.getParameter("id");
                        String sql = "SELECT * FROM user WHERE id = " + userId;
                        String sql2 = "INSERT INTO user(name) VALUES ('" + request.getParameter("name") + "')";
                        String sql3 = "UPDATE user SET age = " + request.getParameter("age") + " WHERE id = 1";
                        String sql4 = "DELETE FROM user WHERE id = " + request.getParameter("id");

                        java.sql.PreparedStatement pstmt = null;
                        pstmt.setString(1, request.getParameter("username"));

                        JdbcTemplate jdbcTemplate = new JdbcTemplate();
                        jdbcTemplate.query(String.format("SELECT * FROM user WHERE id = %s", userId));
                    }
                }
                """;
    }

    private static String getMyBatisDollarParamTestCode() {
        return """
                <!-- MyBatis Mapper XML -->
                <select id="getUserById" resultType="User">
                    SELECT * FROM user WHERE id = ${userId}
                </select>
                <update id="updateUserName">
                    UPDATE user SET name = ${userName} WHERE id = 1
                </update>
                <delete id="deleteUser">
                    DELETE FROM user WHERE id = ${userId}
                </delete>
                <insert id="insertUser">
                    INSERT INTO user(name) VALUES (${userName})
                </insert>
                """;
    }

    private static String getMyBatisDynamicSqlTestCode() {
        return """
                <!-- MyBatis Mapper XML -->
                <select id="getUserList" resultType="User">
                    SELECT * FROM user
                    <where>
                        <if test="id != null">
                            id = #{id}
                        </if>
                    </where>
                </select>
                <update id="updateUser">
                    UPDATE user SET name = #{name}
                    <where>
                        id = #{id}
                    </where>
                </update>
                <delete id="deleteUserByCondition">
                    DELETE FROM user
                    <where>
                        age > #{age}
                    </where>
                </delete>
                """;
    }

    private static String getMyBatisLikeTestCode() {
        return """
                <!-- MyBatis Mapper XML -->
                <select id="getUserByName" resultType="User">
                    SELECT * FROM user WHERE name LIKE '%${userName}%'
                </select>
                <select id="getUserByEmail" resultType="User">
                    SELECT * FROM user WHERE email LIKE '#{email}%'
                </select>
                <update id="updateUserByLike">
                    UPDATE user SET status = 1 WHERE remark LIKE '%${keyword}%'
                </update>
                """;
    }
}
