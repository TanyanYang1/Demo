import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class XXERuleTest1 {

    // 加载 rules-XXE.json 规则文件
    public static JsonNode loadRules(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(new File(filePath));
    }

    // 测试单个规则
    public static boolean testRule(String language, JsonNode rule, List<String> testSnippets) {
        String type = rule.get("type").asText();
        String keyStr = rule.get("keyStr").asText();
        String regExp = rule.get("regExp").asText();

        System.out.println("\n=== 测试规则：" + type + " | 关键词：" + keyStr + " ===");
        System.out.println("正则表达式：" + regExp);

        Pattern pattern = Pattern.compile(regExp);
        int total = testSnippets.size();
        int matched = 0;

        for (int i = 0; i < testSnippets.size(); i++) {
            String code = testSnippets.get(i);
            boolean isMatch = pattern.matcher(code).find(); // 使用 find() 更适合代码扫描
            if (isMatch) matched++;
            System.out.println("测试片段" + (i + 1) + ": " + (isMatch ? "匹配" : "不匹配"));
            System.out.println("代码内容：" + code);
        }

        System.out.println("\n【" + language + "】规则测试统计：总片段" + total + "个，匹配" + matched + "个");
        return matched == total;
    }

    // 获取对应语言的测试代码片段
    public static List<String> getTestCodes(String lang) {
        List<String> list = new ArrayList<>();

        switch (lang) {
            case "java":
                list.add("DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();");
                list.add("SAXParserFactory spf = SAXParserFactory.newInstance();");
                list.add("XMLReader reader = XMLReaderFactory.createXMLReader();");
                list.add("factory.setFeature(\"http://xml.org/sax/features/external-general-entities\", false);");
                list.add("DOMParser parser = new DOMParser(); parser.parse(xmlFile);");
                break;

            case "php":
                list.add("$xml = simplexml_load_string($xmlStr);");
                list.add("$xml = simplexml_load_file('test.xml');");
                list.add("$parser = xml_parser_create(); xml_parse($parser, $xmlData);");
                list.add("$dom = new DOMDocument(); $dom->load('test.xml');");
                list.add("$client = new SoapClient('wsdl.xml');");
                list.add("$data = xmlrpc_decode($response);");
                break;

            case "go":
                list.add("var result MyStruct; xml.Unmarshal(xmlData, &result);");
                list.add("decoder := xml.NewDecoder(strings.NewReader(xmlStr));");
                list.add("err := decoder.Decode(&result);");
                list.add("import \"github.com/beevik/etree\"; doc := etree.ParseXML(xmlData);");
                break;

            case "python":
                list.add("import xml.etree.ElementTree as ET; tree = ET.parse('test.xml');");
                list.add("root = ET.fromstring(xmlString);");
                list.add("from lxml import etree; tree = etree.parse('test.xml');");
                list.add("xml.sax.parse('test.xml', handler);");
                list.add("parser = xml.sax.make_parser();");
                list.add("dom = xml.dom.minidom.parse('test.xml');");
                list.add("import defusedxml; safe_tree = defusedxml.ElementTree.parse('test.xml');");
                break;
        }

        return list;
    }

    public static void main(String[] args) {
        try {
            // 1. 加载规则文件
            JsonNode root = loadRules("C:\\Users\\19017\\Desktop\\rules\\demo\\src\\main\\java\\rules-XXE.json");
            boolean allPassed = true;

            // 2. 遍历所有语言规则
            Iterator<String> languages = root.fieldNames();
            while (languages.hasNext()) {
                String lang = languages.next();
                JsonNode langRules = root.get(lang);
                List<String> testCodes = getTestCodes(lang);

                System.out.println("\n=====================================");
                System.out.println("开始测试【" + lang + "】XXE规则");
                System.out.println("=====================================");

                // 3. 逐个测试规则
                for (JsonNode rule : langRules) {
                    String keyStr = rule.get("keyStr").asText();

                    // 筛选包含关键词的测试片段
                    List<String> targetSnippets = new ArrayList<>();
                    for (String code : testCodes) {
                        if (code.contains(keyStr)) {
                            targetSnippets.add(code);
                        }
                    }

                    if (targetSnippets.isEmpty()) {
                        System.out.println("\n⚠️ 规则" + keyStr + "无匹配测试片段，跳过");
                        continue;
                    }

                    // 执行测试
                    boolean passed = testRule(lang, rule, targetSnippets);
                    if (!passed) allPassed = false;
                }
            }

            // 最终结果
            System.out.println("\n=====================================");
            System.out.println("所有规则测试完成：" + (allPassed ? "全部通过" : "部分失败"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}