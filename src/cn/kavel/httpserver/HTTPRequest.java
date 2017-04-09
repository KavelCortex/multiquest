package cn.kavel.httpserver;

import java.util.HashMap;

/**
 * Created by wjw_w on 2017/4/8.
 */
public class HTTPRequest {
    private final String mRawRequest;
    private String mMethod;
    private String mPath;
    private HashMap<String, String> mQueryMap = new HashMap<>();
    private boolean isBadRequest;

    HTTPRequest(String requestHeader) {
        mRawRequest = requestHeader;
        if(mRawRequest.isEmpty()) {
            isBadRequest=true;
            throw new IllegalStateException();
        }
        parsePath();
    }

    private HTTPRequest(String rawRequest,String method,String path){
        mRawRequest=rawRequest;
        mMethod=method;
        mPath=path;
    }

    String getRawRequest() {
        return mRawRequest;
    }

    String getRequestMethod() {
        return mMethod;
    }

    boolean isRequestMethod(String methodType) {
        return mMethod.equals(methodType);
    }

    String getRequestFileName() {
        return mPath;
    }

    HashMap<String, String> getQueryMap() {
        return mQueryMap;
    }

    String getQueryValue(String key) {
        return mQueryMap.get(key);
    }

    boolean isBadRequest(){
        return isBadRequest;
    }

    static HTTPRequest generateBadRequest(){
        return new HTTPRequest("","","/bad.request");
    }

    private void parsePath() {

        mMethod = mRawRequest.substring(0, mRawRequest.indexOf("/")).trim();
        String pathRaw = mRawRequest.substring(mRawRequest.indexOf(mMethod) + 4, mRawRequest.indexOf("HTTP/")).trim();

        if (pathRaw.contains("#"))  //Cut the Anchor
            pathRaw = pathRaw.split("#")[0];
        if (pathRaw.contains("?")) {
            mPath = pathRaw.split("\\?")[0];
            String queryString = pathRaw.split("\\?")[1];
            fillQueryMap(queryString);
        } else {
            mPath = pathRaw;
        }
        if (mPath.endsWith("/"))
            mPath = mPath + "index.html";
        else if (!mPath.substring(mPath.lastIndexOf("/")).contains(".")) { //handle the case when using "http://www.xxx.com/a" format.
            mPath = mPath + "/index.html";
        }
    }

    private void fillQueryMap(String queryString) {
        for (String querySet : queryString.split("&")) {
            String queryKey = querySet.split("=")[0];
            String queryValue = "";
            try {
                queryValue = querySet.split("=")[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                queryValue = "";
            }
            mQueryMap.put(queryKey, queryValue);
        }
    }

    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder("Request: " + mMethod + ": " + mPath + " Query: ");
        for (String key : mQueryMap.keySet()) {
            returnString.append(key + "=" + mQueryMap.get(key) + ", ");
        }
        return returnString.toString();
    }
}
