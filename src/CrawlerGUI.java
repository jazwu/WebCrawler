
import java.util.ArrayList;

import webcrawler.URLDatabase;
import webcrawler.WebCrawler;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
        startButton.setEnabled(false);

        // Create layout
        JPanel panel = new JPanel(new GridLayout(8, 2));
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

        // Add DocumentListener to each text field
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateButtonState();
            }
        };

        urlField1.getDocument().addDocumentListener(documentListener);
        urlField2.getDocument().addDocumentListener(documentListener);
        urlField3.getDocument().addDocumentListener(documentListener);

        // Add action listener to the Start button
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                totalTimeLabel.setText("Calculating...");
                SwingUtilities.invokeLater(() -> startCrawling());
            }
        });

        // Add the panel to the frame
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);

        // Set the frame visible
        setVisible(true);
    }

    public void updateButtonState(){
        // Enable the startButton if at least one text field has non-empty text
        boolean enableButton = !urlField1.getText().isEmpty() ||
                               !urlField2.getText().isEmpty() ||
                               !urlField3.getText().isEmpty();
        startButton.setEnabled(enableButton);
    }

    public void startCrawling(){
        int selectedDepth1 = (Integer) depthComboBox1.getSelectedItem();
        int selectedDepth2 = (Integer) depthComboBox2.getSelectedItem();
        int selectedDepth3 = (Integer) depthComboBox3.getSelectedItem();
        String url1 = urlField1.getText();
        String url2 = urlField2.getText();
        String url3 = urlField3.getText();

        long startTime = System.currentTimeMillis();

        // Create Web Crawler(s)
        ArrayList<WebCrawler> bots = new ArrayList<>();
        if (url1 != ""){
            bots.add(new WebCrawler(url1, 1, crawlerDB, selectedDepth1));
        }
        if (url2 != ""){
            bots.add(new WebCrawler(url2, 1, crawlerDB, selectedDepth2));
        }
        if (url3 != ""){
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
        totalTimeLabel.setText(String.valueOf(endTime-startTime)+"s");
        totalUrlsLabel.setText(String.valueOf(crawlerDB.urlCount("crawled_urls")));
        startButton.setEnabled(false);
    }

    public static void main(String[] args){
        CrawlerGUI gui = new CrawlerGUI();
    }
}
