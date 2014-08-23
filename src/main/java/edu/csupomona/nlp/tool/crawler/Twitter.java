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

import java.io.IOException;
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
 *
 * @author xing
 */
public class Twitter {
    
    private final TwitterStream ts;
    
    public Twitter() throws IOException {
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
        
        System.out.println(key.getProperty("ConsumerKey"));
        
        // create twitter stream
        ts = new TwitterStreamFactory(cb.build()).getInstance();
        
        // add listener
        ts.addListener(new StatusListener() {

            @Override
            public void onStatus(Status status) {
                System.out.println(status.getText());
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
    
    
    public void query(String[] keywords) {        
        FilterQuery fQuery = new FilterQuery();
        fQuery.track(keywords);
    }
    
    
    public static void main(String[] args) throws IOException {
        Twitter twitter = new Twitter();
        
        String[] keywords = {"ios", "android"};
        twitter.query(keywords);
    }
    
}
