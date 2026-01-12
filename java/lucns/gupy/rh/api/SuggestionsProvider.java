package lucns.gupy.rh.api;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class SuggestionsProvider extends BaseProvider {

    private SuggestionsRequester requester;

    public SuggestionsProvider(ResponseCallback responseCallback) {
        super(responseCallback);
    }

    public void request(String text) {
        if (requester != null) requester.cancel();
        requester = new SuggestionsRequester(new SuggestionsRequester.Callback() {
            @Override
            public void onFinish(String responseData) {
                if (responseData == null || responseData.isEmpty()) return;
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray jsonArray = jsonObject.getJSONArray("jobs");
                    String[] suggestions = new String[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) suggestions[i] = jsonArray.getString(i);
                    responseCallback.onSuggestionsReceived(suggestions);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        requester.requestSuggestions(text);
    }

    private static class SuggestionsRequester {

        public interface Callback {
            void onFinish(String responseData);
        }

        private Callback callback;
        private Thread t;
        private int responseCode;

        public SuggestionsRequester(Callback callback) {
            this.callback = callback;
        }

        public void requestSuggestions(String text) {
            t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String response = request("https://portal.api.gupy.io/api/autocomplete/" + URLEncoder.encode(text, "UTF-8"));
                        if (responseCode == 200) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (callback != null) callback.onFinish(response);
                                }
                            });
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }

        public void cancel() {
            callback = null;
            if (t != null && !t.isInterrupted()) t.interrupt();
        }

        private String request(String url) {
            responseCode = 0;
            InputStreamReader inputStreamReader = null;
            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection) (new URL(url)).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setRequestProperty("Connection", "keep-alive");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
                connection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
                connection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

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

            if (connection == null) return "";

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
}
