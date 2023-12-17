import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.json.*;
import javax.json.stream.JsonGenerator;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WebCrawler implements Runnable{
    private static final int MAX_DEPTH = 3;
    private static final int MAX_THREADS = 5;
    // private static final String OUTPUT_FILE = "crawled_urls.json";
    private JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
    private static Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
    private int urlID = 0;
    private int ID;
    private String startingUrl;
    private Thread thread;

    // public void main(String[] agrs){
    //     // Crawl from a starting url
    //     String startingUrl = "http://127.0.0.1:3000/pageExample/index.html";
    //     crawl(startingUrl, 0);
    //     // Save the crawled URLs into a JSON file
    //     saveToJson(jsonArrayBuilder.build());
    // }

    public WebCrawler(String url, int num){
        System.out.println("WebCrawler created");
        startingUrl = url;
        ID = num;

        thread = new Thread(this);
        thread.start();
    }

    private JsonObject getDetail(Document document, String url) throws IOException{
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        // data associated with url
        jsonObjectBuilder.add("id", ++urlID);
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
    
    private void crawl(String url, int depth){
        if (depth > MAX_DEPTH){
            return;
        }
        // If url has already been kept, stop crawling from this url
        if (visitedUrls.get(url)!=null && visitedUrls.get(url)){
            return;
        }

        Document doc = request(url);

        if(doc != null){
            // select all the anchors having href attribute from current url page
            for (Element link: doc.select("a[href]")){
                String nextUrl = link.attr("abs:href");
                if (!nextUrl.isEmpty() && !nextUrl.startsWith("javascript:")){
                    crawl(nextUrl, depth+1);
                }
            }
        }
    }

    private void saveToJson(JsonArray jsonArray){
        try (Writer writer = new FileWriter(OUTPUT_FILE)) {
            // A configuration property to generate JSON prettily
            Map<String, Object> writerConfig = new HashMap<>();
            writerConfig.put(JsonGenerator.PRETTY_PRINTING, true);
            // Create a JsonWriterFactory with appropriate settings 
            JsonWriterFactory writerFactory = Json.createWriterFactory(writerConfig);

            // Use JsonWriter to write the JsonArray to the file
            try (JsonWriter jsonWriter = writerFactory.createWriter(writer)) {
                jsonWriter.writeArray(jsonArray);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Document request(String url){
        try{
            Connection con = Jsoup.connect(url);
            Document doc = con.get();

            if(con.response().statusCode() == 200){
                System.out.println("\nBot ID:"+ ID +" Received Webpage at "+url);

                JsonObject urlPage = getDetail(doc, url);
                jsonArrayBuilder.add(urlPage);
                visitedUrls.put(url, true);
                return doc;
            }
            return null;
        } catch (IOException e){
            return null;
        }
    }

    @Override
    public void run() {
        crawl(startingUrl, 0);
    }
}