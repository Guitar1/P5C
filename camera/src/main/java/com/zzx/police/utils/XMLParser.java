package com.zzx.police.utils;

import com.zzx.police.data.ApkFileInfo;
import com.zzx.police.data.ServiceInfo;
import com.zzx.police.data.Values;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**@author Tomy
 * Created by Tomy on 2014/9/26.
 */
public class XMLParser {
    private static SAXParser mSaxParser;
    private static XMLParser mXmlParser;
    private ServiceInfo mInfo;
    private InstallInfo mInstallInfo;
    public static final String REPLACEABLE  = "replaceable";
    public static final String APK_PKG_NAME = "pkgName";
    public static final String TARGET_VERSION   = "version";
    public static final String TARGET_APK       = "apk";
    public static final String UNINSTALL_PKG    = "uninstall";
    private XMLParser() {
    }

    public static XMLParser getInstance() {
        if (mSaxParser == null) {
            try {
                mSaxParser = SAXParserFactory.newInstance().newSAXParser();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mXmlParser == null) {
            mXmlParser = new XMLParser();
        }
        return mXmlParser;
    }

    public ServiceInfo parserXMLFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            return null;
        try {
            mSaxParser.parse(new File(filePath), mHandler);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return mInfo;
    }
    public InstallInfo parserXMLFile(File file) {
        if (!file.exists())
            return null;
        try {
            mSaxParser.parse(file, mHandler2);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (mInstallInfo.isEmpty()) {
            return null;
        }
        return mInstallInfo;
    }
    public void release() {
        mSaxParser.reset();
        mSaxParser = null;
        mXmlParser = null;
    }

    private DefaultHandler mHandler = new XMLContentHandler();
    private class XMLContentHandler extends DefaultHandler {
        private String mFlag;
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            mFlag = localName;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            mFlag = "";
        }

        @Override
        public void startDocument() throws SAXException {
            if (mInfo == null) {
                mInfo = new ServiceInfo();
            }
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String value = String.valueOf(ch, start, length);
            switch (mFlag) {
                case Values.XML_SERVICE_IP:
                    mInfo.mServiceIp = value;
                    break;
                case Values.XML_SERVICE_PORT:
                    mInfo.mPort = Integer.parseInt(value);
                    break;
            }
        }
    }
    private DefaultHandler mHandler2 = new XMLContentHandler2();
    private class XMLContentHandler2 extends DefaultHandler {
        private String mTarget;
        private ApkFileInfo mInfo;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            mTarget = localName;
            switch (localName) {
                case TARGET_APK:
                    mInfo = new ApkFileInfo();
                    mInfo.mPkgName = attributes.getValue(APK_PKG_NAME);
                    mInfo.mReplaceable = attributes.getValue(REPLACEABLE).equalsIgnoreCase("true");
                    break;
                case UNINSTALL_PKG:
                    mInstallInfo.mUninstallList.add(attributes.getValue(APK_PKG_NAME));
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (localName.equals(TARGET_APK)) {
                mInstallInfo.mInstallList.add(mInfo);
            }
            mTarget = "";
        }

        @Override
        public void startDocument() throws SAXException {
            if (mInstallInfo == null) {
                mInstallInfo = new InstallInfo();
            }
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String value = String.valueOf(ch, start, length);
            switch (mTarget) {
                case TARGET_VERSION:
                    mInstallInfo.mVersion  = Integer.parseInt(value);
                    break;
                case TARGET_APK:
                    mInfo.mFilePath = value;
                    break;
            }
        }
    }
    public class InstallInfo {
        public int mVersion = 0;
        public List<ApkFileInfo> mInstallList;
        public List<String> mUninstallList;
        public InstallInfo() {
            mInstallList = new ArrayList<>();
            mUninstallList = new ArrayList<>();
        }
        public void releaseInstall() {
            if (mInstallList != null) {
                mInstallList.clear();
                mInstallList = null;
            }
        }

        public void releaseUninstall() {
            if (mUninstallList != null) {
                mUninstallList.clear();
                mUninstallList = null;
            }
        }

        public boolean isEmpty() {
            return mInstallList.isEmpty() && mUninstallList.isEmpty();
        }
    }
}
