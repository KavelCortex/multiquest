package cn.kavel.httpserver;

import java.util.HashMap;

/**
 * Created by wjw_w on 2017/4/8.
 */
public class HTTPResponse {

    private static HashMap<Integer, String> mHTTPStatusCode = new HashMap<Integer, String>() {{
        put(200, "200 OK");
        put(301, "301 Moved Permanently");
        put(400, "400 Bad Request");
        put(403, "403 Forbidden");
        put(404, "404 Not Found");
    }};

    private static final String SERVER_NAME = "Kavel's HTTP Server/0.1";
    private int mStatusCode;
    private boolean mIsConnectionKeepAlive;
    private int mContentLength = 0;

    private HTTPResponse(Builder builder) {
        mStatusCode = builder.mStatusCode;
        mIsConnectionKeepAlive = builder.mIsConnectionKeepAlive;
        mContentLength = builder.mContentLength;
    }

    public static String getStatusString(int statusCode) {
        if (mHTTPStatusCode.containsKey(statusCode))
            return mHTTPStatusCode.get(statusCode);
        else
            return mHTTPStatusCode.get(400);
    }

    public static String getServerName() {
        return SERVER_NAME;
    }

    public int getStatusCode() {
        return mStatusCode;
    }

    public boolean isConnectionKeepAlive() {
        return mIsConnectionKeepAlive;
    }

    public int getContentLength() {
        return mContentLength;
    }

    @Override
    public String toString() {
        StringBuilder responseHeader = new StringBuilder();
        responseHeader.append("HTTP/1.1 " + getStatusString(mStatusCode) + "\r\n");
        responseHeader.append("Server: " + SERVER_NAME + "\r\n");
        responseHeader.append("Connection: " + (mIsConnectionKeepAlive ? "keep-alive" : "Closed") + "\r\n");
        responseHeader.append("Content-Length: " + mContentLength + "\r\n");
        responseHeader.append("\r\n");
        return responseHeader.toString();
    }

    public static class Builder {

        private int mStatusCode;
        private boolean mIsConnectionKeepAlive;
        private int mContentLength = 0;

        public Builder(int statusCode) {
            mStatusCode = statusCode;
        }

        public Builder setConnectionKeepAlive(boolean isKeepAlive) {
            mIsConnectionKeepAlive = isKeepAlive;
            return this;
        }

        public Builder setContentLength(int length) {
            mContentLength = length;
            return this;
        }

        public HTTPResponse build() {
            return new HTTPResponse(this);
        }

    }

}
