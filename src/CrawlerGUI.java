
import java.util.ArrayList;

import webcrawler.URLDatabase;
import webcrawler.WebCrawler;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CrawlerGUI extends JFrame{
    private static final String DB_USER = "myuser";
    private static final String DB_PASSWORD = "535897";

    private URLDatabase crawlerDB;
    private JComboBox<Integer> depthComboBox1, depthComboBox2, depthComboBox3;
    private JTextField urlField1, urlField2, urlField3;
    private JLabel totalTimeLabel, totalUrlsLabel;
    private JButton startButton;

     public CrawlerGUI() {
        // Start database webcrawler
        crawlerDB = new URLDatabase("webcrawler", DB_USER, DB_PASSWORD);
        crawlerDB.createTable("crawled_urls");

        // Set up the frame
        setTitle("Web Crawler");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // the window is place in the center of the screen

        // Create components
        totalUrlsLabel = new JLabel(String.valueOf(crawlerDB.urlCount("crawled_urls")));
        depthComboBox1 = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        depthComboBox2 = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        depthComboBox3 = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        urlField1 = new JTextField(20);
        urlField2 = new JTextField(20);
        urlField3 = new JTextField(20);
        totalTimeLabel = new JLabel("No Crawler Working");
        startButton = new JButton("Start Crawling!");

        // Create layout
        JPanel panel = new JPanel(new GridLayout(9, 2));
        JPanel urlRow = new JPanel(new FlowLayout());
        JPanel timeRow = new JPanel(new FlowLayout());

        // Add components to the panel
        urlRow.add(new JLabel("Crawled URLs in Database: "));
        urlRow.add(totalUrlsLabel);
        panel.add(urlRow);
        timeRow.add(new JLabel("Time for Crawling: "));
        timeRow.add(totalTimeLabel);
        panel.add(timeRow);
        panel.add(new JLabel("URL to Crawl 1:"));
        panel.add(new JLabel("Select Depth:"));
        panel.add(urlField1);
        panel.add(depthComboBox1);
        panel.add(new JLabel("URL to Crawl 2:"));
        panel.add(new JLabel("Select Depth:"));
        panel.add(urlField2);
        panel.add(depthComboBox2);
        panel.add(new JLabel("URL to Crawl 3:"));
        panel.add(new JLabel("Select Depth:"));
        panel.add(urlField3);
        panel.add(depthComboBox3);
        panel.add(new JLabel("")); // Placeholder for spacing
        panel.add(new JLabel(""));
        panel.add(startButton);

        // Add action listener to the Start button
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                totalTimeLabel.setText("Calculating...");
                startCrawling();
            }
        });

        // Add the panel to the frame
        add(panel);

        // Set the frame visible
        setVisible(true);
    }

    public void startCrawling(){
        int selectedDepth1 = (depthComboBox1.getSelectedItem() == null) ? 0 : (Integer) depthComboBox1.getSelectedItem();
        int selectedDepth2 = (depthComboBox2.getSelectedItem() == null) ? 0 : (Integer) depthComboBox2.getSelectedItem();
        int selectedDepth3 = (depthComboBox3.getSelectedItem() == null) ? 0 : (Integer) depthComboBox3.getSelectedItem();
        String url1 = urlField1.getText();
        String url2 = urlField2.getText();
        String url3 = urlField3.getText();

        long startTime = System.currentTimeMillis();

        // Create Web Crawler(s)
        ArrayList<WebCrawler> bots = new ArrayList<>();
        if (url1 != "" && selectedDepth1 != 0){
            bots.add(new WebCrawler(url1, 1, crawlerDB, selectedDepth1));
        }
        if (url2 != "" && selectedDepth2 != 0){
            bots.add(new WebCrawler(url2, 1, crawlerDB, selectedDepth2));
        }
        if (url3 != "" && selectedDepth3 != 0){
            bots.add(new WebCrawler(url3, 1, crawlerDB, selectedDepth3));
        }

        for(WebCrawler bot:bots){
            try{
                bot.getThread().join();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        totalTimeLabel.setText(String.valueOf(endTime-startTime));
        totalUrlsLabel.setText(String.valueOf(crawlerDB.urlCount("crawled_urls")));
    }

    public static void main(String[] args){
        CrawlerGUI gui = new CrawlerGUI();
    }
}
