package com.zzx.police.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**@author Tomy
 * Created by Tomy on 2015-06-25.
 */
public class XMLUtils {
    public static String createXML(String policeNum, int state, boolean needTime) {
        String xmlStr = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            document.setXmlVersion("1.0");

            Element root = document.createElement("root");
            document.appendChild(root);

            Element devId = document.createElement("devid");
            devId.setTextContent(policeNum);

            Element status = document.createElement("status");
            status.setTextContent(state + "");

            root.appendChild(status);
            root.appendChild(devId);
            if (needTime) {
                Element time = document.createElement("datatime");
                time.setTextContent(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(System.currentTimeMillis()));
                root.appendChild(time);
            }

            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transFormer = transFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);

            //export string
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            transFormer.transform(domSource, new StreamResult(bos));
            xmlStr = bos.toString();

            //-------
            //save as file
            /*File file = new File("/sdcard/TelePhone.xml");
            if(!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            StreamResult xmlResult = new StreamResult(out);
            transFormer.transform(domSource, xmlResult);*/
            //--------
        } catch (Exception e) {
            e.printStackTrace();
        }

        return xmlStr;
    }
}
