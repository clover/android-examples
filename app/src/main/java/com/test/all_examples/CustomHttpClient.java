package com.test.all_examples;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class CustomHttpClient extends DefaultHttpClient {
  private static final int CONNECT_TIMEOUT = 60000;
  private static final int READ_TIMEOUT = 60000;
  private static final int MAX_TOTAL_CONNECTIONS = 5;
  private static final int MAX_CONNECTIONS_PER_ROUTE = 3;
  private static final int SOCKET_BUFFER_SIZE = 8192;
  private static final boolean FOLLOW_REDIRECT = false;
  private static final boolean STALE_CHECKING_ENABLED = true;
  private static final String CHARSET = HTTP.UTF_8;
  private static final HttpVersion HTTP_VERSION = HttpVersion.HTTP_1_1;
  private static final String USER_AGENT = "CustomHttpClient"; // + version

  private CustomHttpClient() {
  }

  private CustomHttpClient(ClientConnectionManager conman, HttpParams params) {
    super(conman, params);
  }

  private CustomHttpClient(HttpParams params) {
    super(params);
  }

  public static CustomHttpClient getHttpClient() {
    CustomHttpClient httpClient = new CustomHttpClient();
    final HttpParams params = httpClient.getParams();
    HttpProtocolParams.setUserAgent(params, USER_AGENT);
    HttpProtocolParams.setContentCharset(params, CHARSET);
    HttpProtocolParams.setVersion(params, HTTP_VERSION);
    HttpClientParams.setRedirecting(params, FOLLOW_REDIRECT);
    HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
    HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);
    HttpConnectionParams.setSocketBufferSize(params, SOCKET_BUFFER_SIZE);
    HttpConnectionParams.setStaleCheckingEnabled(params, STALE_CHECKING_ENABLED);
    ConnManagerParams.setTimeout(params, CONNECT_TIMEOUT);
    ConnManagerParams.setMaxTotalConnections(params, MAX_TOTAL_CONNECTIONS);
    ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(MAX_CONNECTIONS_PER_ROUTE));

    return httpClient;
  }

  public String get(String url) throws IOException, HttpException {
    String result;
    HttpGet request = new HttpGet(url);
    HttpResponse response = execute(request);
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == HttpStatus.SC_OK) {
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        result = EntityUtils.toString(entity);
      } else {
        throw new HttpException("Received empty body from HTTP response");
      }
    } else {
      throw new HttpException("Received non-OK status from server: " + response.getStatusLine());
    }
    return result;
  }

  @SuppressWarnings("unused")
  public String post(String url, String body) throws IOException, HttpException {
    String result;
    HttpPost request = new HttpPost(url);
    HttpEntity bodyEntity = new StringEntity(body);
    request.setEntity(bodyEntity);
    HttpResponse response = execute(request);
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == HttpStatus.SC_OK) {
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        result = EntityUtils.toString(entity);
      } else {
        throw new HttpException("Received empty body from HTTP response");
      }
    } else {
      throw new HttpException("Received non-OK status from server: " + response.getStatusLine());
    }
    return result;
  }
}
