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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private final TwitterStream ts_; 
    
    // parameters
    private String lang_;    // for language filter
    private boolean includeRetweet_; // for retweet filter
    private final HashSet<Long> idSet_;  // for id redundancy filter
    private final String IDSET_FILE_ = "idset.txt";
    
    // limit restriction
    private Integer sizeLimit_;
    private Integer hourLimit_;
    
    // recorded tweet list
    private List<String> tweet_;
    // file name for recording the tweets
    private String filename_;
    
    /**
     * Construct Twitter for crawling with Stream API
     * @throws IOException
     */
    public Twitter() throws IOException {
        // init default parameters
        lang_ = "en";
        includeRetweet_ = false;
        idSet_ = loadSet();
        
        // init default restriction
        sizeLimit_ = 3000;
        hourLimit_ = 24;    // 24 hours
        
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
        ts_ = new TwitterStreamFactory(cb.build()).getInstance();
        
        // add listener
        ts_.addListener(new StatusListener() {

            @Override
            public void onStatus(Status status) {
                // only record tweet matches requirement
                if (isRetweetMatch(status) && !isIdRedundant(status)) {
                    tweet_.add(status.getText());
                    idSet_.add(status.getId());
                }
                
                System.out.println("[" + idSet_.size() + "/" + sizeLimit_ + "]"  
                        + status.getId() + ": " + status.getText());
                
                // when limit is reached
                if (isLimitReached()) {
                    try {
                        // write tweet to file
                        write2File();
                        
                        // write id to file for future tracking
                        updateSet(idSet_);
                        
                        // stop streaming
                        ts_.cleanUp();
                    } catch (IOException ex) {
                        Logger.getLogger(Twitter.class.getName())
                                .log(Level.SEVERE, null, ex);
                    }
                }
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

    public Integer getSizeLimit_() {
        return sizeLimit_;
    }

    public void setSizeLimit_(Integer sizeLimit_) {
        this.sizeLimit_ = sizeLimit_;
    }

    public Integer getHourLimit_() {
        return hourLimit_;
    }

    public void setHourLimit_(Integer hourLimit_) {
        this.hourLimit_ = hourLimit_;
    }
    
    /**
     * Load HashSet of ID for crawled tweet
     * @return          HashSet which contains ID
     */
    private HashSet<Long> loadSet() {
        HashSet<Long> idSet = new HashSet<>();
        
        try {
            FileReader fr = new FileReader(IDSET_FILE_);
            try (BufferedReader br = new BufferedReader(fr)) {
                String line;
                while ((line = br.readLine()) != null) 
                    idSet.add(Long.valueOf(line.trim()));
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
    private void updateSet(HashSet<Long> idSet) 
            throws IOException {
        FileWriter fw = new FileWriter(IDSET_FILE_, false);
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            for (Long id : idSet) 
                bw.write(id + "\n");
        }
    }
    
    private void write2File() throws IOException {
        // since we kept tracking id of tweet, so newly obtained tweet
        // should be new ones. therefore we don't overwrite previous ones.
        FileWriter fw = new FileWriter(filename_, true);
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            for (String tweet : tweet_)
                bw.write(tweet + "\n");
            
            // also at the end of the file record time stamp
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            bw.write("#### Finished at: " 
                    + df.format(Calendar.getInstance().getTime()) 
                    + " ####\n");
        }
        
    }
    
    /**
     * Check whether retweet status mathces requirement
     * @param status        Status from Twitter
     * @return              True: match, False: mismatch
     */
    private boolean isRetweetMatch(Status status) {
        return (status.isRetweet() == includeRetweet_);
    }
    
    /**
     * Check whether ID of the tweet is redundant
     * @param status        Status from Twitter
     * @return              True: redundant, False: no
     */
    private boolean isIdRedundant(Status status) {
        return idSet_.contains(status.getId());
    }
    
    /**
     * Check whether size and time restrictions have met
     * @return              True: reached, False: no
     */
    private boolean isLimitReached() {
        if (tweet_.size() >= sizeLimit_)
            return true;
        
        return false;
    }
    
    /**
     * Query with given keywords. Crawling will start immediately.
     * @param keywords      Array of keywords
     */
    public void query(String[] keywords) {    
        // prepare for the new query
        filename_ = "";
        for (String keyword : keywords) 
            filename_ += (keyword + "_");
        filename_ += ".txt";
        tweet_ = new ArrayList<>();
        
        // construct FilterQuery
        FilterQuery fQuery = new FilterQuery();
        fQuery.track(keywords);     // track specified keywords
        String[] languages = {lang_};
        fQuery.language(languages); // track specified language
        
        // start streaming with FilterQuery
        ts_.filter(fQuery);
    }
    
    
}
