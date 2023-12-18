package webcrawler;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.json.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import dbquery.UrlDB;

public class WebCrawler implements Runnable{
    private static final int MAX_DEPTH = 1;
    private static final String DB_NAME = "crawled_urls";
    private static Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());

    private int ID;
    private String startingUrl;
    private Thread thread;

    public WebCrawler(String url, int num){
        System.out.println("WebCrawler created");
        startingUrl = url;
        ID = num;
        // Assign a thread to each bot
        thread = new Thread(this);
        thread.start();
    }

    private void crawl(String url, int depth){
        url = rightTrim(url);
        if (depth > MAX_DEPTH || UrlDB.contains(DB_NAME, url)){
            return;
        }

        Document doc = request(url);

        if(doc != null){
            for (Element link: doc.select("a[href]")){
                // for each link on this page, run DFS
                String nextUrl = link.attr("abs:href");
                if (!nextUrl.isEmpty() && !nextUrl.startsWith("javascript:")){
                    crawl(nextUrl, depth+1);
                }
            }
        }
    }

    private Document request(String url){
        try{
            Connection con = Jsoup.connect(url);
            Document doc = con.get();
            // connection succeeds
            if(con.response().statusCode() == 200){
                System.out.println("Bot ID:"+ ID +" Received Webpage at "+url);

                DB.
                return doc;
            }
            return null;
        } catch (IOException e){
            return null;
        }
    }

    // remove unnessecary '/' appending the end of the url
    private String rightTrim(String url){
        String trimUrl = url;
        int i=url.length()-1;
        while (url.charAt(i)=='/'){
            trimUrl = url.substring(0, i);
            i--;
        }
        return trimUrl;
    }

    private JsonObject getDetail(Document document, String url) throws IOException{
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        // data associated with url
        jsonObjectBuilder.add("title", document.title());
        jsonObjectBuilder.add("description", getMetaContent(document, "description"));
        jsonObjectBuilder.add("keyword", getMetaContent(document, "keyword"));
        jsonObjectBuilder.add("url", url);
        return jsonObjectBuilder.build();
    }

    private String getMetaContent(Document document, String metaName){
        // Select meta elements with the specified name attribute
        Elements metaElements = document.select("meta[name="+metaName+"]");
        // Get the content attribute value from the first matching meta element
        Element metaElement = metaElements.first();
        return (metaElement != null) ? metaElement.attr("content") : "";
    }

    @Override
    public void run() {
        crawl(startingUrl, 0);
    }

    public Thread getThread(){
        return thread;
    }

    public int totalUrl(){
        return visitedUrls.size();
    }

    public static void main(String[] args){
        // create a database to store crawled urls
        UrlDB.createDatabase(DB_NAME);

        ArrayList<WebCrawler> bots = new ArrayList<>();
        bots.add(new WebCrawler("https://www.npr.org", 1));
        bots.add(new WebCrawler("https://abcnews.go.com", 2));
        bots.add(new WebCrawler("https://www.nytimes.com", 3));

        for(WebCrawler bot:bots){
            try{
                bot.getThread().join();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}