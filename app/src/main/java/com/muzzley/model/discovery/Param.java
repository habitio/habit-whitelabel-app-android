package com.muzzley.model.discovery;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by ruigoncalo on 09/07/14.
 */
public class Param implements Parcelable {

    public static final String ST = "st";
    public static final String MX = "mx";
    public static final String METHOD = "method";
    public static final String URL = "url";
    public static final String HEADERS = "headers";
    public static final String BODY = "body";
    public static final String COMPLETION_STATUS_CODE = "completionStatusCode";
    public static final String INTERFACE = "interface";
    public static final String PREFIX_LENGTH = "prefixLength";
    public static final String BROADCAST = "broadcast";
    public static final String IP = "ip";
    public static final String EXPECT_RESPONSE = "expectResponse";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String DATA = "data";
    public static final String TTL = "ttl";


    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    private String st;
    private String mx;
    private String method;
    private String url;
    transient private String headers;
    @SerializedName("headers")
    public List<Map<String,String>> headersMap;
    private String body;
    private String completionStatusCode;

    @SerializedName("interface")
    public String pInterface;
//    private String pInterface;
    private boolean prefixLength;
    private boolean broadcast;
    private boolean ip;

    private boolean expectResponse;
    private String host;
    private int port;
    private String data;
    private int ttl;

    public Param(){}

    public Param(String st, String mx, String method, String url, String headers, String body, String completionStatusCode,
                 String pInterface, boolean prefixLength, boolean broadcast, boolean ip, boolean expectResponse, String host, int port,
                 String data, int ttl) {
        this.st = st;
        this.mx = mx;
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;
        this.completionStatusCode = completionStatusCode;
        this.pInterface = pInterface;
        this.prefixLength = prefixLength;
        this.broadcast = broadcast;
        this.ip = ip;
        this.expectResponse = expectResponse;
        this.host = host;
        this.port = port;
        this.data = data;
        this.ttl = ttl;
    }

    public Param(Parcel in) {
        st = in.readString();
        mx = in.readString();
        method = in.readString();
        url = in.readString();
        headers = in.readString();
        body = in.readString();
        completionStatusCode = in.readString();
        pInterface = in.readString();
        prefixLength = in.readByte() != 0;
        broadcast = in.readByte() != 0;
        ip = in.readByte() != 0;
        expectResponse = in.readByte() != 0;
        host = in.readString();
        port = in.readInt();
        data = in.readString();
        ttl = in.readInt();
    }

    public String getSt() {
        return st;
    }

    public String getMx() {
        return mx;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getCompletionStatusCode() {
        return completionStatusCode;
    }

    public void setSt(String st) {
        this.st = st;
    }

    public void setMx(String mx) {
        this.mx = mx;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setCompletionStatusCode(String completionStatusCode) {
        this.completionStatusCode = completionStatusCode;
    }

    public String getpInterface() {
        return pInterface;
    }

    public boolean isPrefixLength() {
        return prefixLength;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public boolean isIp() {
        return ip;
    }

    public void setpInterface(String pInterface) {
        this.pInterface = pInterface;
    }

    public void setPrefixLength(boolean prefixLength) {
        this.prefixLength = prefixLength;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public void setIp(boolean ip) {
        this.ip = ip;
    }

    public int describeContents() {
        return 0;
    }

    public boolean isExpectResponse() {
        return expectResponse;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getData() {
        return data;
    }

    public int getTtl() {
        return ttl;
    }

    public void setExpectResponse(boolean expectResponse) {
        this.expectResponse = expectResponse;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(st);
        out.writeString(mx);
        out.writeString(method);
        out.writeString(url);
        out.writeString(headers);
        out.writeString(body);
        out.writeString(completionStatusCode);
        out.writeString(pInterface);
        out.writeByte((byte) (prefixLength ? 1 : 0));
        out.writeByte((byte) (broadcast ? 1 : 0));
        out.writeByte((byte) (ip ? 1 : 0));
        out.writeByte((byte) (expectResponse ? 1 : 0));
        out.writeString(host);
        out.writeInt(port);
        out.writeString(data);
        out.writeInt(ttl);
    }

    public static final Creator<Param> CREATOR = new Creator<Param>() {
        @Override public Param createFromParcel(Parcel source) {
            return new Param(source);
        }

        @Override public Param[] newArray(int size) {
            return new Param[size];
        }
    };

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(ST + ":").append(st == null ? "null" : st).append(",")
                .append(MX + ":").append(mx == null ? "null" : mx).append(",")
                .append(METHOD + ":").append(method == null ? "null" : method).append(",")
                .append(URL + ":").append(url == null ? "null" : url).append(",")
                .append(HEADERS + ":").append(headers == null ? "null" : headers).append(",")
                .append(BODY + ":").append(body == null ? "null" : body).append(",")
                .append(COMPLETION_STATUS_CODE + ":").append(completionStatusCode == null ? "null" : completionStatusCode).append(",")
                .append(INTERFACE + ":").append(pInterface == null ? "null" : pInterface).append(",")
                .append(PREFIX_LENGTH + ":").append(prefixLength).append(",")
                .append(BROADCAST + ":").append(broadcast).append(",")
                .append(IP + ":").append(ip).append(",")
                .append(EXPECT_RESPONSE + ":").append(expectResponse).append(",")
                .append(HOST + ":").append(host == null ? "null" : host).append(",")
                .append(PORT + ":").append(port).append(",")
                .append(DATA + ":").append(data == null ? "null" : data).append(",")
                .append(TTL + ":").append(ttl);
        return builder.toString();
    }
}