/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool.crawler;

import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Page;
import facebook4j.ResponseList;
import facebook4j.conf.ConfigurationBuilder;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Facebook crawler using Facebook4j
 * @author xing
 */
public class Facebook {
    
    private final facebook4j.Facebook fb_;
    
    public Facebook() throws IOException {
        
        
        // read and construct property
        Properties key = new Properties();
        key.load(getClass().getResourceAsStream("/etc/facebook.properties"));
        
        // set authentication key/token
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setRestBaseURL("https://graph.facebook.com/v2.0/")
                .setOAuthAppId(key.getProperty("AppID"))
                .setOAuthAppSecret(key.getProperty("AppSecret"))
                .setOAuthAccessToken(key.getProperty("AccessToken"))
                .setOAuthPermissions(key.getProperty("Permissions"));
        
        // create Facebook4j instance
        fb_ = new FacebookFactory(cb.build()).getInstance();
        
        System.out.println(fb_.getConfiguration().getRestBaseURL());
    }
    
    public void search() throws JSONException {
        try {
            ResponseList<Page> results = fb_.searchPages("samsung");
            for (Page result : results) {
                String query = "select is_verified from page where page_id=" + result.getId();
                JSONObject json = fb_.executeFQL(query).getJSONObject(0);
                boolean isVerified = true;
                System.out.println(json.get("is_verified").toString());
                
                if (isVerified)
                    System.out.println(result.getName() + ":" + result.getId() + " <official>");
                else
                    System.out.println(result.getName() + ":" + result.getId());
            }
        } catch (FacebookException ex) {
            Logger.getLogger(Facebook.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws IOException, JSONException {
        Facebook fb = new Facebook();
        
        fb.search();
    }

    
}
