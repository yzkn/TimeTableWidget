package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

public final class AsyncDlTask extends AsyncTask<URL, Integer, String[]> {
    private AsyncCallback _asyncCallback = null;

    public AsyncDlTask(AsyncCallback asyncCallback) {
        this._asyncCallback = asyncCallback;
    }

    public static String buildQueryString(String baseUri, Map<String, String> querys) {
        querys.put("acl:consumerKey", OdptKey.TOKEN);

        final StringBuffer sb = new StringBuffer(baseUri);
        sb.append("?");
        for (Map.Entry<String, String> query : querys.entrySet()) {
            String key;
            try {
                key = URLEncoder.encode(query.getKey(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                key = "";
            }
            String val;
            try {
                val = URLEncoder.encode(query.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                val = "";
            }
            sb.append(key);
            sb.append("=");
            sb.append(val);
            sb.append("&");
        }
        String url = sb.toString();
        url = url.substring(0, url.length() - 1);

        return url;
    }

    public static String downloadText(final URL url) {
        return downloadText(url, "UTF-8");
    }

    public static String downloadText(final URL url, final String charsetName) {
        try {
            if (OdptKey.IS_DEBUG) Log.v("TTW", url.toString());

            final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(30000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setReadTimeout(30000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Accept-Language", "jp");
            httpURLConnection.connect();

            final InputStream inputStream = httpURLConnection.getInputStream();
            final InputStreamReader objReader = new InputStreamReader(inputStream, charsetName);
            final BufferedReader bufferedReader = new BufferedReader(objReader);
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String utfLine = new String(line.getBytes(), "UTF-8");
                sb.append(utfLine).append("\n");
            }
            final String result = sb.toString();
            inputStream.close();

            return result;
        } catch (final IOException e) {
        }
        return "";
    }

    @Override
    protected String[] doInBackground(URL... urls) {
        final int count = urls.length;
        final String[] results = new String[count];
        Arrays.fill(results, "");

        for (int i = 0; i < count; i++) {
            results[i] = downloadText(urls[i]);
            publishProgress((int) ((i / (float) count) * 100));
            if (isCancelled())
                break;
        }
        return results;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this._asyncCallback.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        this._asyncCallback.onProgressUpdate(values[0]);
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        this._asyncCallback.onPostExecute(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        this._asyncCallback.onCancelled();
    }

    public interface AsyncCallback {
        void onPreExecute();

        void onPostExecute(String[] result);

        void onProgressUpdate(int progress);

        void onCancelled();
    }
}
