package com.zzx.police.apn;

/**
 * Created by Administrator on 2016/9/22 0022.
 */
public class ApnInfo {

    public static final String APN_ID = "_id";
    public static final String APN_NAME = "name";
    public static final String APN_MCC = "mcc";
    public static final String APN_MNC = "mnc";
    public static final String APN_NUMERIC = "numeric";
    public static final String APN_APN = "apn";
    public static final String APN_CURRENT = "current";
    public static final String APN_TYPE = "type";
    public static final String APN_PROTOCOL = "protocol";
    public static final String APN_ROAMING = "roaming_protocol";
    public static final String APN_USER = "user";
    public static final String APN_PASSWORD = "password";
    public static final String APN_AUTHENTICATION_TYPE = "authtype";


    private String id;
    private String name;
    private String mcc;
    private String mnc;
    private String numeric;
    private String apn;
    private String current;
    private String type;
    private String protocol;
    private String roaming;
    private String user;
    private String password;
    private int authenticationtype;

    @Override
    public String toString() {
        return "ApnInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", mcc='" + mcc + '\'' +
                ", mnc='" + mnc + '\'' +
                ", numeric='" + numeric + '\'' +
                ", apn='" + apn + '\'' +
                ", current='" + current + '\'' +
                ", type='" + type + '\'' +
                ", protocol='" + protocol + '\'' +
                ", roaming='" + roaming + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", authenticationtype=" + authenticationtype +
                '}';
    }

    public int getAuthenticationtype() {
        return authenticationtype;
    }

    public void setAuthenticationtype(int authenticationtype) {
        this.authenticationtype = authenticationtype;
    }


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public static ApnInfo getApnInfo(String name) {
        ApnInfo apnInfo = new ApnInfo();

        if ("xinzeli".equals(name)) {
            apnInfo.setName("xinzeli");
            apnInfo.setApn("GDSZXZL01.CLFU.GZM2MAPN");
            apnInfo.setMcc("460");
            apnInfo.setMnc("06");
            apnInfo.setCurrent("1");
            apnInfo.setType("default");
        }

        if ("yika".equals(name)) {
            apnInfo.setName("UNIM2M.GZM2MAPN");
            apnInfo.setApn("UNIM2M.GZM2MAPN");
            apnInfo.setMcc("460");
            apnInfo.setMnc("06");
            apnInfo.setCurrent("1");
            apnInfo.setType("default");
        }
        return apnInfo;
    }


    public boolean isSame(ApnInfo info) {
        if (info.getName().equals(getName()))
            if (info.getNumeric().equals(getNumeric()))
                return true;
        return false;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public String getApn() {
        return apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getNumeric() {
        numeric = getMcc() + getMnc();
        return numeric;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getRoaming() {
        return roaming;
    }

    public void setRoaming(String roaming) {
        this.roaming = roaming;
    }
}
