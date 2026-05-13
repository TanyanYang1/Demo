import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringReader;

// 高危 XXE 漏洞代码
public class RiskXXE {
    public void parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 未禁用外部实体 → 高危 XXE
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.parse(new InputSource(new StringReader(xml)));


    }

    public void staxParse(String xml) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        // 无安全配置 → XXE
        factory.createXMLStreamReader(new StringReader(xml));
}