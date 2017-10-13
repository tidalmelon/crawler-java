package com.data.crawler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class HttpUtil {

    private static Log log = LogFactory.getLog(HttpUtil.class);

    private final int MAX_TOTAL_CONNECTIONS = 10;

    private PoolingHttpClientConnectionManager connectionManager;

    private HttpClientContext context = null;

    public HttpUtil() {
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        connectionManager.setDefaultMaxPerRoute(10);

        context = HttpClientContext.create();
        Registry<CookieSpecProvider> registry = RegistryBuilder.<CookieSpecProvider> create()
                .register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
                .register(CookieSpecs.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory()).build();
        context.setCookieSpecRegistry(registry);
    }

    private CloseableHttpClient getHttpClient() {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(120000)
                .setConnectTimeout(60000).build();
        return HttpClients.custom().setDefaultRequestConfig(config).setConnectionManager(connectionManager).build();
    }

    public String downHtml(String uri) {

        CloseableHttpClient httpClient = getHttpClient();
        HttpGet httpget = new HttpGet(uri);

        httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0");

        CloseableHttpResponse response = null;

        try {
            response = httpClient.execute(httpget);

            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                return null;
            }

            HttpEntity entity = response.getEntity();
            if (null == entity) {
                return null;
            }

            //System.out.println("reponse headers: ");
            //for (Header header:response.getAllHeaders()) {
            //    System.out.println(header.getName() + "\t" + header.getValue());
            //}
            //System.out.println("reponse headers!");

            InputStream in = entity.getContent();
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int rc = 0;
            while ((rc=in.read(buff, 0, 1024)) >0) {
                swapStream.write(buff, 0, rc);
            }
            byte[] data = swapStream.toByteArray();
            String encoding = CharsetDetector.getEncoding(data);
            encoding = encoding == null ? "utf-8": encoding;
            String html = new String(data, encoding);
            in.close();

            return html;

        } catch (ClientProtocolException e) {
            log.info(e.getMessage());;
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        return null;
    }
}
