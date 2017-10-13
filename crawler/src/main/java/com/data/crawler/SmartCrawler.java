package com.data.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

/**
 * xpath: http://www.w3school.com.cn/xpath/index.asp
 */

public class SmartCrawler {

    private static Log log = LogFactory.getLog(SmartCrawler.class);

    private HtmlCleaner cleaner = new HtmlCleaner();

    private HttpUtil httpPool = new HttpUtil();

    private Queue<String> queueUrl = new LinkedList<String>();

    private Pattern Pat_paging = Pattern.compile("http://science.dataguru.cn/(index.php\\?page=\\d+)?");

    private Pattern Pat_content = Pattern.compile("http://science.dataguru.cn/article-\\d+-\\d+.html");

    public void start(String seed) {
        queueUrl.add(seed);
        while (queueUrl.size() > 0) {
            String uri = queueUrl.poll();
            String html = httpPool.downHtml(uri);

            if (Pat_paging.matcher(uri).matches()) { // 识别索引页
                getOutlinks(html, uri);
            }else if(Pat_content.matcher(uri).matches()){ // 识别内容页
                extractInfo(html, uri);
            }else{
                log.info("regex err: " + uri);
            }
        }
    }

    private void extractInfo(String html, String uri) {
        TagNode doc = cleaner.clean(html);
        try {
            Object[] tags_title = doc.evaluateXPath("//h1");
            String title = ((TagNode)tags_title[0]).getText().toString();
            log.info("title: " + title);

            //** other field
        } catch (XPatherException e) {
            e.printStackTrace();
        }

    }

    public void getOutlinks(String html, String base) {
        TagNode doc = cleaner.clean(html);

        try {
            URL baseUrl = new URL(base);
            Object[] tags_content = doc.evaluateXPath("//div[@class='bm_c xld']/dl/dt/a[@class='xi2']");
            for (Object object : tags_content) {
                String relativeurl = ((TagNode) object).getAttributeByName("href");
                // 相对地址转绝对地址
                URL url = new URL(baseUrl, relativeurl);
                log.info("content url: " + url.toString());
                queueUrl.add(url.toString());
            }

            Object[] tags_next = doc.evaluateXPath("//a[@class='nxt']");
            String relative_url_next = ((TagNode) tags_next[0]).getAttributeByName("href");
            // 相对地址转绝对地址
            URL url = new URL(baseUrl, relative_url_next);
            log.info("paging url: " + url.toString());
            queueUrl.add(url.toString());

        } catch (XPatherException e) {
            log.warn(e.getMessage());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        SmartCrawler crawl = new SmartCrawler();
        String seed = "http://science.dataguru.cn/";
        crawl.start(seed);
        log.info("complete");
    }

}
