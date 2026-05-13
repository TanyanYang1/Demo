import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;
import java.util.regex.Pattern;

public class SqlRuleTest3 {

    // ====================== 你的配置（不用改，直接用）======================
    private static final String RULE_FILE_PATH = "C:\\Users\\19017\\Desktop\\rules\\demo\\src\\main\\java\\rules-Sql.json";
    private static final String GITHUB_OWNER = "TanyanYang1";
    private static final String GITHUB_REPO = "Demo";
    private static final String GITHUB_TOKEN = ""; // 公开仓库留空
    // ====================================================================

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static void main(String[] args) {
        try {
            // 读取规则文件
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
                String regExp = rule.get("regExp").asText();

                System.out.println("=====================================");
                System.out.println("规则 " + index + "：" + type);
                System.out.println("正在扫描 GitHub：" + GITHUB_OWNER + "/" + GITHUB_REPO);

                // 在线扫描（不克隆）
                scanGitHubRepo("", regExp, type);

                System.out.println("=====================================\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在线扫描 GitHub 仓库（不克隆、不下载）
     */
    private static void scanGitHubRepo(String path, String regExp, String ruleType) throws IOException, InterruptedException {
        String apiUrl = String.format(
                "https://api.github.com/repos/%s/%s/contents/%s",
                GITHUB_OWNER, GITHUB_REPO, path
        );

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/vnd.github.v3+json");

        // 只有 token 不为空才添加请求头
        if (GITHUB_TOKEN != null && !GITHUB_TOKEN.isBlank()) {
            requestBuilder.header("Authorization", "token " + GITHUB_TOKEN);
        }

        HttpResponse<String> response = HTTP_CLIENT.send(
                requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString()
        );

        JsonNode files = OBJECT_MAPPER.readTree(response.body());

        if (files.isArray()) {
            for (JsonNode file : files) {
                String type = file.get("type").asText();
                String name = file.get("name").asText();
                String filePath = file.get("path").asText();

                // 跳过隐藏目录
                if (filePath.startsWith(".git") || filePath.startsWith(".")) continue;

                if ("dir".equals(type)) {
                    scanGitHubRepo(filePath, regExp, ruleType);
                } else {
                    // 只扫描 java / xml
                    if (isTargetFile(name, ruleType)) {
                        System.out.println("\n→ 扫描文件：" + filePath);
                        String content = getFileContent(file);

                        // 【修复】改用 find() 匹配，100%能抓到 ${xxx}
                        boolean isMatch = Pattern.compile(regExp).matcher(content).find();

                        if (isMatch) {
                            System.out.println("✅ 【高危】匹配成功：" + ruleType);
                        } else {
                            System.out.println("❌ 安全");
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取文件内容
     */
    private static String getFileContent(JsonNode fileNode) throws IOException, InterruptedException {
        String downloadUrl = fileNode.get("download_url").asText();
        HttpResponse<String> resp = HTTP_CLIENT.send(
                HttpRequest.newBuilder(URI.create(downloadUrl)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        return resp.body();
    }

    /**
     * 只扫描需要的文件
     */
    private static boolean isTargetFile(String fileName, String ruleType) {
        if (ruleType.contains("Java") && fileName.endsWith(".java")) return true;
        if (ruleType.contains("MyBatis") && fileName.endsWith(".xml")) return true;
        return false;
    }
}