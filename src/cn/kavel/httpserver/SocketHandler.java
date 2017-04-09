package cn.kavel.httpserver;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wjw_w on 2017/4/7.
 */
public class SocketHandler implements Runnable {

    private static ConcurrentHashMap<UUID, Socket> clientSocketList = new ConcurrentHashMap<>();

    private UUID mUUID;
    private List<Socket> mSockets = new ArrayList<>();

    SocketHandler(List<Socket> sockets) {
        mSockets = sockets;
    }

    public static Socket getClientSocket(UUID clientUUID) {
        return clientSocketList.get(clientUUID);
    }

    static int getClientServingCount() {
        if (clientSocketList != null)
            return clientSocketList.size();
        else
            return 0;
    }

    int getSocketCount(){
        return mSockets.size();
    }

    public UUID getUUID() {
        return mUUID;
    }

    @Override
    public void run() {
        for (Socket socket : mSockets) {
            try {
                try {
                    mUUID = UUID.randomUUID();
                    clientSocketList.put(mUUID, socket);
                    //System.out.println("ADD:" + getUUID() + "/ " + getClientServingCount() + " serving");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    StringBuilder requestRaw = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        requestRaw.append(line);
                        requestRaw.append("\r\n");
                        if (line.isEmpty())
                            break;
                    }

                    HTTPRequest httpRequest;
                    try {
                        httpRequest = new HTTPRequest(requestRaw.toString());
                        //System.out.println(httpRequest);
                    } catch (IllegalStateException e) {
                        //System.out.println("Received a bad/empty request");
                        //httpRequest = HTTPRequest.generateBadRequest();
                        closeSocket(socket);
                        continue;
                    }

                    StringBuilder defaultContent = new StringBuilder();
                    defaultContent.append("Your HTTP Request is:\r\n\r\n");
                    defaultContent.append(httpRequest.getRawRequest());
                    defaultContent.append("#EOF\r\n");

                    int statusCode;

                    InputStream contentStream;
                    String directoryPath = "D:\\Workspace\\JavaNtwkJAVA\\site";
                    File getFile = new File(directoryPath, httpRequest.getRequestFileName());
                    try {
                        contentStream = new FileInputStream(getFile);
                        statusCode = 200;
                    } catch (FileNotFoundException e) {
                        //System.out.println("FileNotFound or BAD REQUEST");
                        if (httpRequest.isBadRequest())
                            statusCode = 400;
                        else
                            statusCode = 404;
                        defaultContent.insert(0, HTTPResponse.getStatusString(statusCode) + "\r\n\r\n");
                        contentStream = new ByteArrayInputStream(defaultContent.toString().getBytes());
                    }

                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    HTTPResponse httpResponse = new HTTPResponse.Builder(statusCode).setConnectionKeepAlive(false)
                            .setContentLength(contentStream.available()).build();
                    bufferedWriter.write(httpResponse.toString());
                    bufferedWriter.flush();

                    if (!httpRequest.isRequestMethod("HEAD")) {
                        //System.out.println("Serving File: " + (statusCode == 200 ? httpRequest.getRequestFileName() : "404 Message") + "...");
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
                        int len = -1;
                        byte[] buffer = new byte[1024];
                        while ((len = contentStream.read(buffer)) != -1) {
                            bufferedOutputStream.write(buffer);
                            bufferedOutputStream.flush();
                        }
                        bufferedOutputStream.close();
                    }

                    contentStream.close();
                    bufferedWriter.close();
                    bufferedReader.close();
                    closeSocket(socket);

                } catch (SocketException e) {
                    e.printStackTrace();
                    closeSocket(socket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeSocket(Socket socket) throws IOException {
        socket.close();
        clientSocketList.remove(mUUID);
        //System.out.println("DEL:" + getUUID() + "/ " + getClientServingCount() + " left");
    }
}
