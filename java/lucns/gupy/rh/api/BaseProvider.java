package lucns.gupy.rh.api;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class BaseProvider {

    public static final int READ_TIMEOUT = 300000;
    public static final int CONNECT_TIMEOUT = 300000;

    public static final int ERROR_APP_INTERNAL = -1;
    public static final int ERROR_TIMEOUT = -10;
    public static final int ERROR_UNKNOWN_HOST = -11;
    public static final int ERROR_BAD_CONNECTION = -12;
    public static final int ERROR_NO_STREAM_DATA = -13;
    public static final int ERROR_NOT_FOUND = 404;

    protected ResponseCallback responseCallback;
    protected Thread thread;
    protected int responseCode;
    protected String responseMessage;
    protected HttpsURLConnection connection;
    protected int operation;
    protected String request;
    protected boolean isRunning;
    protected Handler handler;

    public BaseProvider(ResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getRawRequest() {
        return request;
    }

    public void cancel() {
        if (isThreadRunning()) thread.interrupt();
        if (connection != null) connection.disconnect();
    }

    public boolean isThreadRunning() {
        return thread != null && !thread.isInterrupted() && thread.getState() != Thread.State.TERMINATED;
    }

    public int getOperation() {
        return operation;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    protected String requestGet(String url) {
        return request(url, "GET", null);
    }

    protected String requestPost(String url, String data) {
        return request(url, "POST", data);
    }

    protected String requestPut(String url, String data) {
        return request(url, "PUT", data);
    }

    protected String requestDelete(String url, String data) {
        return request(url, "DELETE", data);
    }

    private String request(String url, String method, String data) {
        responseCode = 0;
        InputStreamReader inputStreamReader = null;
        try {
            connection = (HttpsURLConnection) (new URL(url)).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
            connection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
            connection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            if (data!= null) {
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                dos.write(data.getBytes(StandardCharsets.UTF_8));
                dos.flush();
                dos.close();
            }

            connection.connect();
            responseCode = connection.getResponseCode();
            if (responseCode == 204) return "";
            inputStreamReader = new InputStreamReader(connection.getInputStream());
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            if (responseCode == 0) {
                if (e instanceof SocketTimeoutException) responseCode = ERROR_TIMEOUT;
                else if (e instanceof UnknownHostException) responseCode = ERROR_UNKNOWN_HOST;
                else if (e instanceof ConnectException) responseCode = ERROR_BAD_CONNECTION;
                else if (e instanceof FileNotFoundException) responseCode = ERROR_NOT_FOUND;
                else responseCode = ERROR_NOT_FOUND;
            }
        }

        if (inputStreamReader == null) {
            if (connection.getErrorStream() != null) {
                inputStreamReader = new InputStreamReader(connection.getErrorStream());
            } else {
                return "";
            }
        }

        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder builder = new StringBuilder();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (builder.length() > 0) builder.append("\n");
                builder.append(line);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
