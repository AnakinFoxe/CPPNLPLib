/*
<dependency>
    <groupId>org.twitter4j</groupId>
    <artifactId>twitter4j</artifactId>
    <version>4.0.2</version>
</dependency> * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Twitter crawler using Twitter4j 
 * @author xing
 */
public class Twitter {
    
    // Twitter Stream API
    private final TwitterStream ts; 
    
    // parameters
    private String lang_;    // for language filter
    private boolean includeRetweet_; // for retweet filter
    private final HashSet<String> idSet_;  // for id redundancy filter
    private final String IDSET_FILE_ = "idset.txt";
    
    /**
     * Construct Twitter for crawling with Stream API
     * @throws IOException
     */
    public Twitter() throws IOException {
        // init default parameters
        lang_ = "en";
        includeRetweet_ = false;
        idSet_ = loadSet();
        
        // read and construct property
        Properties key = new Properties();
        key.load(getClass().getResourceAsStream("/etc/key.properties"));
        
        // set API keys
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey(key.getProperty("ConsumerKey"));
        cb.setOAuthConsumerSecret(key.getProperty("ConsumerSecret"));
        cb.setOAuthAccessToken(key.getProperty("AccessToken"));
        cb.setOAuthAccessTokenSecret(key.getProperty("AccessTokenSecret"));
        
        // create twitter stream
        ts = new TwitterStreamFactory(cb.build()).getInstance();
        
        // add listener
        ts.addListener(new StatusListener() {

            @Override
            public void onStatus(Status status) {
                System.out.println("text:" + status.getText());
                System.out.println("lang:" + status.getLang());
                System.out.println("reTw:" + status.isRetweet());
                System.out.println("id:" + status.getId());
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice sdn) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public void onTrackLimitationNotice(int i) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public void onScrubGeo(long l, long l1) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public void onStallWarning(StallWarning sw) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public void onException(Exception excptn) {
                excptn.printStackTrace();
            }
        });
        
        
    }

    public String getLang() {
        return lang_;
    }

    public void setLang(String lang) {
        this.lang_ = lang;
    }

    public boolean isIncludeRetweet() {
        return includeRetweet_;
    }

    public void setIncludeRetweet(boolean includeRetweet) {
        this.includeRetweet_ = includeRetweet;
    }
    
    /**
     * Load HashSet of ID for crawled tweet
     * @return          HashSet which contains ID
     */
    private HashSet<String> loadSet() {
        HashSet<String> idSet = new HashSet<>();
        
        try {
            FileReader fr = new FileReader(IDSET_FILE_);
            try (BufferedReader br = new BufferedReader(fr)) {
                String line;
                while ((line = br.readLine()) != null) 
                    idSet.add(line.trim());
            }
        } catch (IOException e) {
            System.out.println("WARNING: no " + IDSET_FILE_ + " exists!");
            System.out.println("Creating one...");
            new File(IDSET_FILE_);
        }
        
        return idSet;
    }
    
    /**
     * Update file which contains ID of tweet has been crawled
     * @param idSet
     * @throws IOException 
     */
    private void updateSet(HashSet<String> idSet) throws IOException {
        FileWriter fw = new FileWriter(IDSET_FILE_, false);
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            for (String id : idSet) 
                bw.write(id + "\n");
        }
    }
    
    /**
     * Query with given keywords. Crawling will start immediately.
     * @param keywords      Array of keywords
     */
    public void query(String[] keywords) {        
        // construct FilterQuery with given keywords
        FilterQuery fQuery = new FilterQuery();
        fQuery.track(keywords);
        
        // start streaming with FilterQuery
        ts.filter(fQuery);
    }
    
    
}
