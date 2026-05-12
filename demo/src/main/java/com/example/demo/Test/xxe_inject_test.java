package com.example.demo.Test;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.stream.XMLInputFactory;
import org.w3c.dom.Document;
import java.io.StringReader;

public class xxe_inject_test {
    public static void main(String[] args) throws Exception {
        // 恶意XXE payload
        String maliciousXml = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE root ["
                + "<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + "<root>&xxe;</root>";

        // 1. DocumentBuilderFactory 风险点
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // 安全修复：禁用外部实体
        // dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        // dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(maliciousXml)));
        System.out.println("DocumentBuilderFactory 结果: " + doc.getTextContent());

        // 2. SAXParserFactory 风险点
        SAXParserFactory spf = SAXParserFactory.newInstance();
        // 安全修复：禁用外部实体
        // spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        // spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        SAXParser saxParser = spf.newSAXParser();
        saxParser.parse(new InputSource(new StringReader(maliciousXml)), new org.xml.sax.helpers.DefaultHandler());

        // 3. XMLReaderFactory 风险点
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        // 安全修复：禁用外部实体
        // xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        // xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        xmlReader.parse(new InputSource(new StringReader(maliciousXml)));

        // 4. TransformerFactory 风险点
        TransformerFactory tf = TransformerFactory.newInstance();
        // 安全修复：设置安全特性
        // tf.setAttribute("jdk.xml.transformer.accessExternalDTD", "");

        // 5. XMLInputFactory 风险点
        XMLInputFactory xif = XMLInputFactory.newInstance();
        // 安全修复：禁用外部实体
        // xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        // 6. DOMParser 风险点
        DOMParser domParser = new DOMParser();
        // 安全修复：禁用外部实体
        // domParser.setFeature("http://xml.org/sax/features/external-general-entities", false);
        domParser.parse(new InputSource(new StringReader(maliciousXml)));
    }
}