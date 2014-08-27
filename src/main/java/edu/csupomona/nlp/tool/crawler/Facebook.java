/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool.crawler;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.json.JsonObject;
import com.restfb.types.Page;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Facebook crawler using RestFB.
 * Originally used Facebook4j but it has problem supporting FQL.
 * @author xing
 */
public class Facebook {
    
//    private final facebook4j.Facebook fb_;
    
    private final FacebookClient fb_;
    
    public Facebook() throws IOException {
        // read and construct property
        Properties key = new Properties();
        key.load(getClass().getResourceAsStream("/etc/facebook.properties"));
        
        // initialization 
        // using access token obtained from Graph API Explorer (weird way)
        fb_ = new DefaultFacebookClient(key.getProperty("myToken"));
    }
    
    public void search(String keyword) {
        // construct search query for pages using keyword
        Connection<Page> pageSearch = fb_.fetchConnection("search", Page.class, 
                Parameter.with("q", keyword), Parameter.with("type", "page"));
        
        // get response data
        List<Page> results = pageSearch.getData();
        
        for (Page result : results) {
            // check if it is official (verified) page
            String query = "select is_verified from page where page_id=" 
                    + result.getId();
            List<JsonObject> isVerified = 
                    fb_.executeFqlQuery(query, JsonObject.class);
            if (isVerified.get(0).getBoolean("is_verified")) {
                System.out.println(result.getName() + ": " + result.getId());
            } else {
                System.out.println(result.getName());
            }
            
        }
    }
    
    public static void main(String[] args) throws IOException {
        Facebook fb = new Facebook();
        
        fb.search("samsung");
    }

    
}
