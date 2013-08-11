package com.mgranstrom.elasticsearch.levenshtein.tests.base;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.jackson.core.JsonFactory;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;



public class HttpClient {

    private final URL baseUrl;

    public HttpClient(TransportAddress transportAddress) {
        InetSocketAddress address = ((InetSocketTransportAddress) transportAddress).address();
        try {
            baseUrl = new URL("http", address.getHostName(), address.getPort(), "/");
        } catch (MalformedURLException e) {
            throw new ElasticSearchException("", e);
        }
    }

    public HttpClient(String url) {
        try {
            baseUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new ElasticSearchException("", e);
        }
    }

    public HttpClient(URL url) {
        baseUrl = url;
    }

    public HttpClientResponse request(String path) throws IOException {
        return request("GET", path, null);
    }

    public HttpClientResponse request(String Method,String path) throws IOException {
        return request(Method, path, null);
    }


    public HttpClientResponse request(String method, String path, XContentBuilder json) throws IOException {
        URL url;
        try {
            url = new URL(baseUrl, path);
        } catch (MalformedURLException e) {
            throw new ElasticSearchException("Cannot parse " + path, e);
        }

        HttpURLConnection urlConnection;
        String message = "";
        if(json != null)
            message = json.string();

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method);
            urlConnection.setReadTimeout( 10000 /*milliseconds*/ );
            urlConnection.setConnectTimeout( 15000 /* milliseconds */ );
            if(!message.isEmpty()){
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setFixedLengthStreamingMode(message.getBytes().length);
                //make some HTTP header nicety
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                urlConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            }
            //open
            urlConnection.connect();

            if(!message.isEmpty()){
                //setup send
                OutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
                os.write(message.getBytes());
                //clean up
                os.flush();
            }
        } catch (IOException e) {
            throw new ElasticSearchException("", e);
        }

        int errorCode = -1;
        Map<String, List<String>> respHeaders = null;
        try {
            errorCode = urlConnection.getResponseCode();
            respHeaders = urlConnection.getHeaderFields();
            InputStream inputStream = urlConnection.getInputStream();
            String body = null;
            try {
                body = Streams.copyToString(new InputStreamReader(inputStream, Charset.defaultCharset()));
            } catch (IOException e1) {
                throw new ElasticSearchException("problem reading error stream", e1);
            }
            return new HttpClientResponse(body, errorCode, respHeaders, null);
        } catch (IOException e) {
            InputStream errStream = urlConnection.getErrorStream();
            String body = null;
            try {
                body = Streams.copyToString(new InputStreamReader(errStream, Charset.defaultCharset()));
            } catch (IOException e1) {
                throw new ElasticSearchException("problem reading error stream", e1);
            }
            return new HttpClientResponse(body, errorCode, respHeaders, e);
        } finally {
            urlConnection.disconnect();
        }
    }
}
