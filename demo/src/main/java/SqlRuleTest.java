import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * 测试 rules-Sql.json 中 Java SQL 注入规则的有效性
 */
public class SqlRuleTest {

    // 规则文件路径
    private static final String RULE_FILE_PATH = "C:\\Users\\19017\\Desktop\\rules\\demo\\src\\main\\java\\rules-Sql.json";
    // ObjectMapper 用于解析JSON
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        try {
            // 1. 读取规则文件
            JsonNode rootNode = OBJECT_MAPPER.readTree(new File(RULE_FILE_PATH));
            JsonNode javaRulesNode = rootNode.get("java");
            if (javaRulesNode == null || !javaRulesNode.isArray()) {
                System.err.println("规则文件中未找到有效的Java规则配置");
                return;
            }

            // 2. 遍历所有Java规则并测试
            Iterator<JsonNode> javaRuleIterator = javaRulesNode.elements();
            int ruleIndex = 0;
            while (javaRuleIterator.hasNext()) {
                ruleIndex++;
                JsonNode ruleNode = javaRuleIterator.next();
                String type = ruleNode.get("type").asText();
                String keyStr = ruleNode.get("keyStr").asText();
                String regExp = ruleNode.get("regExp").asText();

                System.out.println("=====================================");
                System.out.println("测试规则 " + ruleIndex + "：");
                System.out.println("类型：" + type);
                System.out.println("关键词：" + keyStr);
                System.out.println("正则表达式：" + regExp);

                // 3. 生成对应规则的测试用例
                String testCode = getTestCodeByRuleType(type);
                System.out.println("测试代码片段：\n" + testCode);

                // 4. 执行正则匹配
                Pattern pattern = Pattern.compile(regExp);
                boolean isMatched = pattern.matcher(testCode).matches();

                // 5. 输出测试结果
                if (isMatched) {
                    System.out.println("✅ 规则匹配成功");
                } else {
                    System.out.println("❌ 规则匹配失败");
                }
                System.out.println("=====================================\n");
            }

        } catch (IOException e) {
            System.err.println("读取规则文件失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据规则类型生成对应的测试代码片段
     * @param ruleType 规则类型
     * @return 测试代码字符串
     */
    private static String getTestCodeByRuleType(String ruleType) {
        return switch (ruleType) {
            case "Java SQL注入风险" -> getJavaSqlInjectionTestCode();
            case "MyBatis XML ${}参数拼接风险" -> getMyBatisDollarParamTestCode();
            case "MyBatis XML动态SQL单条件风险" -> getMyBatisDynamicSqlTestCode();
            case "MyBatis XML like拼接风险" -> getMyBatisLikeTestCode();
            default -> "// 无匹配的测试代码";
        };
    }

    /**
     * Java原生SQL注入风险测试代码（覆盖Statement、字符串拼接、PreparedStatement、JdbcTemplate等规则）
     */
    private static String getJavaSqlInjectionTestCode() {
        return """
                import java.sql.Statement;
                import javax.servlet.http.HttpServletRequest;
                import org.springframework.jdbc.core.JdbcTemplate;

                public class TestSqlInjection {
                    public void testStatement(HttpServletRequest request) throws Exception {
                        // 测试Statement.execute
                        Statement stmt = null;
                        stmt.execute("SELECT * FROM user WHERE id = 1");
                        // 测试Statement.executeQuery
                        stmt.executeQuery("SELECT * FROM user WHERE name = 'test'");
                        // 测试Statement.executeUpdate
                        stmt.executeUpdate("UPDATE user SET name = 'new' WHERE id = 1");
                        
                        // 测试String sql = "SELECT..." + 拼接
                        String userId = request.getParameter("id");
                        String sql = "SELECT * FROM user WHERE id = " + userId;
                        // 测试String sql = "INSERT..." + 拼接
                        String sql2 = "INSERT INTO user(name) VALUES ('" + request.getParameter("name") + "')";
                        // 测试String sql = "UPDATE..." + 拼接
                        String sql3 = "UPDATE user SET age = " + request.getParameter("age") + " WHERE id = 1";
                        // 测试String sql = "DELETE..." + 拼接
                        String sql4 = "DELETE FROM user WHERE id = " + request.getParameter("id");
                        
                        // 测试PreparedStatement.setString + request.getParameter
                        java.sql.PreparedStatement pstmt = null;
                        pstmt.setString(1, request.getParameter("username"));
                        
                        // 测试JdbcTemplate.query + String.format
                        JdbcTemplate jdbcTemplate = new JdbcTemplate();
                        jdbcTemplate.query(String.format("SELECT * FROM user WHERE id = %s", userId));
                    }
                }
                """;
    }

    /**
     * MyBatis XML ${}参数拼接风险测试代码
     */
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

    /**
     * MyBatis XML动态SQL单条件风险测试代码
     */
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

    /**
     * MyBatis XML like拼接风险测试代码
     */
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