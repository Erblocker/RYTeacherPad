package com.netspace.library.utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {
    protected Document mRootDocument;
    protected Element mRootElement;

    public boolean parseXML(String szXML) {
        try {
            this.mRootDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8)));
            this.mRootElement = this.mRootDocument.getDocumentElement();
            return true;
        } catch (SAXException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            e2.printStackTrace();
            return false;
        } catch (ParserConfigurationException e3) {
            e3.printStackTrace();
            return false;
        }
    }

    public Element getXMLNode(String szXPath) {
        try {
            NodeList nodes = (NodeList) XPathFactory.newInstance().newXPath().evaluate(szXPath, this.mRootElement, XPathConstants.NODESET);
            if (nodes.getLength() >= 1) {
                return (Element) nodes.item(0);
            }
        } catch (XPathExpressionException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public NodeList getXMLNodes(String szXPath) {
        try {
            return (NodeList) XPathFactory.newInstance().newXPath().evaluate(szXPath, this.mRootElement, XPathConstants.NODESET);
        } catch (XPathExpressionException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    public int getXMLNodesCount(String szXPath) {
        NodeList Result = getXMLNodes(szXPath);
        if (Result != null) {
            return Result.getLength();
        }
        return 0;
    }
}
