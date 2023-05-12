package edu.uw.info314.xmlrpc.server;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.logging.*;

import java.io.IOException;

import static spark.Spark.*;

class Call {
    public String name;
    public List<Object> args = new ArrayList<Object>();
}

public class App {
    public static final Logger LOG = Logger.getLogger(App.class.getCanonicalName());
    private static final int PORT = 8080;

    public static void main(String[] args) {
        port(PORT);
        LOG.info("Starting up on port " + PORT);

        before((req, res) -> {
            if (!req.uri().equals("/RPC")) {
                halt(404, "URL not found");
            }
            if (!req.requestMethod().equals("POST")) {
                halt(405, "Method Not Supported");
            }
        });

        // This is the mapping for POST requests to "/RPC";
        // this is where you will want to handle incoming XML-RPC requests
        post("/RPC", (req, res) -> {
            res.status(200);

            Call call = extractXMLRPCCall(req.body());
            String methodName = call.name;

            int[] params = new int[call.args.size()];
            for (int i = 0; i < params.length; i++) {
                params[i] = (int)call.args.get(i);
            }

            Calc cal = new Calc();

            if (methodName.equals("add")) {
                return createXMLResponse(cal.add(params));
            } else if (methodName.equals("subtract")) {
                return createXMLResponse(cal.subtract(params[0], params[1]));
            } else if (methodName.equals("multiply")) {
                return createXMLResponse(cal.multiply(params));
            } else if (methodName.equals("divide")) {
                return createXMLResponse(cal.divide(params[0], params[1]));
            } else {
                return createXMLResponse(cal.modulo(params[0], params[1]));
            }

        });
    }

    public static Call extractXMLRPCCall(String xml) {
        Call result = new Call();
        // parse XML
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(bais);
            doc.getDocumentElement().normalize();

            result.name = doc.getElementsByTagName("methodName").item(0).getTextContent();

            NodeList list = doc.getElementsByTagName("i4");
            List<Object> arguments = new ArrayList<>();
            for (int i = 0; i < list.getLength(); i++) {
                arguments.add(Integer.valueOf(list.item(i).getTextContent()));
            }
            result.args = arguments;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public static String createXMLResponse(int r) {
        String result = "<?xml version=\"1.0\"?><methodResponse><params><param><value><string>";
        result += r;
        result += "</string></value></param></params></methodResponse>";
        return result;
    }
}
