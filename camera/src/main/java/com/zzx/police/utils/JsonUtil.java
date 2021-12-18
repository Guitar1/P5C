package com.zzx.police.utils;

import com.alibaba.fastjson.JSON;

public class JsonUtil {
    private Object header;
    private Object body;
    public JsonUtil() {
    }

    public JsonUtil(Object header, Object body) {
        this.header = header;
        this.body = body;
    }

    public Object getHeader() {
        return header;
    }

    public void setHeader(Object header) {
        this.header = header;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(new JsonUtil(new HeaderJson(),new BodyJson()));
    }
}
