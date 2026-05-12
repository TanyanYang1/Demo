import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.regex.Pattern;

public class XXERuleTest2 {

    // 扫描仓库根目录
   // private static final String REPO_ROOT = System.getProperty("user.dir"); // 当前项目根目录
    private static final String REPO_ROOT =
            "C:\\Users\\19017\\Desktop\\rules\\demo\\diboot-develop-v3";
    private static final String RULE_FILE = "C:\\Users\\19017\\Desktop\\rules\\demo\\src\\main\\java\\rules-XXE.json";

    public static void main(String[] args) {
        try {
            System.out.println("=== 开始扫描项目仓库中的 XXE 漏洞 ===");
            System.out.println("扫描目录：" + REPO_ROOT + "\n");

            // 1. 加载规则
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File(RULE_FILE));

            // 2. 遍历所有语言规则
            Iterator<String> languages = root.fieldNames();
            while (languages.hasNext()) {
                String lang = languages.next();
                System.out.println("========== 扫描语言：" + lang + " ==========");

                JsonNode rules = root.get(lang);
                for (JsonNode rule : rules) {
                    String ruleName = rule.get("type").asText();
                    String regex = rule.get("regExp").asText();
                    System.out.println("\n规则：" + ruleName);
                    System.out.println("正则：" + regex);

                    Pattern pattern = Pattern.compile(regex);

                    // 3. 遍历仓库文件
                    scanFiles(new File(REPO_ROOT), pattern, ruleName);
                }
            }

            System.out.println("\n=== 扫描完成 ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 递归扫描文件
    private static void scanFiles(File dir, Pattern pattern, String ruleName) {
        if (!dir.exists()) return;
        if (dir.isFile()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanFiles(file, pattern, ruleName); // 递归
            } else {
                // 只扫描代码文件
                String name = file.getName();
                if (name.endsWith(".java") || name.endsWith(".php") || name.endsWith(".py") || name.endsWith(".go")) {
                    try {
                        String content = Files.readString(file.toPath());
                        if (pattern.matcher(content).find()) {
                            System.out.println("⚠️  发现漏洞：" + file.getAbsolutePath());
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}