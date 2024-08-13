package com.reactor.webdav;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.reactor.webdav.paths_utils.PathUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.*;

@SuppressWarnings("ALL")
@Slf4j
@Service
public class PropfindToRsService {

    /*
            "<?xml version=\"1.0\"?>\n" +
                "<D:multistatus xmlns:D=\"DAV:\">\n" +
"  <D:response>\n" +
                "      <D:href>/</D:href>\n" +
                "      <D:propstat>\n" +
                "        <D:prop>\n" +
                "          <D:getcontentlength>\n" +
                "            4525\n" +
                "          </D:getcontentlength>"+
                "          <D:resourcetype>\n" +
                "            <D:collection/>\n" +
                "          </D:resourcetype>\n" +
                "        </D:prop>\n" +
                "        <D:status>HTTP/1.1 200 OK</D:status>\n" +
                "      </D:propstat>\n" +
                "    </D:response>" +
                "</D:multistatus>"
     */

    @SneakyThrows
    public String toResponseStreem(List<FileInfo> listFiles) {

        Document doc = getEmptyDocument();
        Element root = doc.createElement("D:multistatus");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:D", "DAV:");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns-138", "http://apache.org/dav/props/");

        listFiles.forEach( it -> {

            Element response = doc.createElement("D:response");

            Element href = doc.createElement("D:href");
            href.setTextContent(it.href);
            response.appendChild(href);

            Element propstat = doc.createElement("D:propstat");
            response.appendChild(propstat);

            Element prop = doc.createElement("D:prop");

            if (it.displayname != null) {
                Node node = doc.createElement("D:displayname" );
                node.setTextContent(it.displayname);
                prop.appendChild(node );
            }

            if (it.creationdate != null) {
                Node node = doc.createElement("D:creationdate" );
                var instance = java.time.Instant.ofEpochMilli(it.creationdate);
                var localDateTime =  ZonedDateTime.ofInstant(instance,  java.time.ZoneId.systemDefault());
                var formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
                var string = formatter.format(localDateTime);
                node.setTextContent(string);
                prop.appendChild(node );

            }

            if (it.isCollection) {
                Node node = doc.createElement("ns-138:executable" );
                prop.appendChild(node);
            }

            if (it.isCollection) {
                Node node = doc.createElement("D:resourcetype" );
                node.appendChild(doc.createElement("D:collection" ));
                prop.appendChild(node );
            } else if (it.mimeType != null) {
                Node node = doc.createElement("D:getcontenttype" );
                node.setTextContent(String.valueOf(it.mimeType));
                prop.appendChild(node);
            }

            if (it.contentLength != null) {
                Node node = doc.createElement("D:getcontentlength" );
                node.setTextContent(String.valueOf(it.contentLength));
                prop.appendChild(node );

            }

            if (it.etag != null) {
                prop.appendChild(doc.createElement("D:getetag"));

            }

            if (it.lastmodified != null) {
                prop.appendChild(doc.createElement("D:getlastmodified"));
            }

            if (it.availbleBytes != null) {
                Node node = doc.createElement("D:quota-available-bytes" );
                node.setTextContent(String.valueOf(it.availbleBytes));
                prop.appendChild(node );

            }

            if (it.usedBytes != null) {
                Node node = doc.createElement("D:quota-used-bytes" );
                node.setTextContent(String.valueOf(it.availbleBytes));
                prop.appendChild(node );

            }

            if (prop.getFirstChild()!= null) {
                propstat.appendChild(prop);
            }


            Element status = doc.createElement("D:status");
            if (it.exist) {
                status.setTextContent("HTTP/1.1 " + HttpStatus.OK);
            } else {
                status.setTextContent("HTTP/1.1 " + HttpStatus.NOT_FOUND);
            }
            propstat.appendChild(status);

            root.appendChild(response);

        });



        doc.appendChild(root);
        StringWriter writer = toWrite(doc);

        var resss = writer.toString();
        return  resss;
    }



    private Document getEmptyDocument() {
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = xmlFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ignore) {}
        assert dBuilder != null;
        return dBuilder.newDocument();

    }
    private StringWriter  toWrite(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(source, result);
        return writer;
    }

}
