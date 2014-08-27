/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool.crawler;

import facebook4j.Comment;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Like;
import facebook4j.PagableList;
import facebook4j.Page;
import facebook4j.Paging;
import facebook4j.Paging.Cursors;
import facebook4j.Post;
import facebook4j.RawAPIResponse;
import facebook4j.Reading;
import facebook4j.ResponseList;
import facebook4j.auth.AccessToken;
import facebook4j.conf.ConfigurationBuilder;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Facebook crawler using Facebook4j
 * @author xing
 */
public class Facebook {
    
    // Facebook4j instance
    private final facebook4j.Facebook fb_;
    
    // time stamp for crawling restriction
    private final Date startTime = new Date();
    
    public Facebook() throws IOException {    
        // read and construct property
        Properties key = new Properties();
        key.load(getClass().getResourceAsStream("/etc/facebook.properties"));
        String appId = key.getProperty("AppID");
        String appSecret = key.getProperty("AppSecret");
        String userToken = key.getProperty("UserToken");
        
        // set authentication key/token
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthAppId(appId)
                .setOAuthAppSecret(appSecret)
                .setOAuthAccessToken(userToken)
                .setOAuthPermissions(key.getProperty("Permissions"));
        
        // create Facebook4j instance
        fb_ = new FacebookFactory(cb.build()).getInstance();
        
        // obtain long-lived token
//        AccessToken newToken = refreshToken(appId, appSecret, userToken);
        
        // replace token
//        fb_.setOAuthAccessToken(newToken);    
        
        // Setup time stamp for crawling starting point
        startTime.setYear(110);
        startTime.setMonth(0);
        startTime.setDate(1);
        System.out.println(startTime.toString());
    }
    
    private AccessToken refreshToken(String appId, String appSecret, String myToken) {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", appId);
        params.put("client_secret", appSecret);
        params.put("grant_type", "fb_exchange_token");
        params.put("fb_exchange_token", myToken);
        
        try {
            // request of an exchange for new token
            RawAPIResponse apiResponse = 
                    fb_.callGetAPI("/oauth/access_token", params);
            
            // set new token for usage
            AccessToken newToken = new AccessToken(apiResponse.asString());
            
            System.out.println("New Token:" + newToken.getToken());

            return newToken;
        } catch (FacebookException ex) {
            Logger.getLogger(Facebook.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
        // otherwise return the original one
        return new AccessToken(myToken);
    }
    
    public void search() throws JSONException {
        try {
            ResponseList<Page> results = fb_.searchPages("samsung");
            for (Page result : results) {
                String query = "select is_verified from page where page_id=" + result.getId();
                JSONObject json = fb_.executeFQL(query).getJSONObject(0);
                boolean isVerified = json.getBoolean("is_verified");
                
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
    
    public void getPosts() {
        try {
            // cursor is the most recommended way from Facebook.
            // but I found offset is quite easy for me to implement.
            ResponseList<Post> posts = fb_.getFeed("114219621960016",
                    new Reading().offset(24));
            int count = 1;
            for (Post post : posts) {
                System.out.println(post.getCreatedTime().toString() + ":"
                        + count + ":"
                        + " messge:" + post.getMessage()
                        + " story:" + post.getStory()
                        + " caption:" + post.getCaption()
                        + " desc:" + post.getDescription()
                        + " name:" + post.getName()
                        + " id:" + post.getId());
                count++;
            }
            
            System.out.println(posts.size());
        } catch (FacebookException ex) {
            Logger.getLogger(Facebook.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void getOtherStuff() {
        try {
            Post post = fb_.getPost("114219621960016_737493369632635");
            System.out.println(post.getMessage());
            
            // TODO: can not get full list also, need to go next page
            PagableList<Comment> comments = post.getComments();
            for (Comment comment: comments) 
                System.out.println(comment.getLikeCount().toString() + ":" 
                        + comment.getMessage());
            
            PagableList<Like> likes = post.getLikes();
            System.out.println(likes.getCount());
            System.out.println(comments.getCount());
            Paging<Like> pageLikes = likes.getPaging();
            Cursors cursors = pageLikes.getCursors();
            
        } catch (FacebookException ex) {
            Logger.getLogger(Facebook.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws IOException, JSONException {
        Facebook fb = new Facebook();
        
//        fb.search();
//        fb.getPosts();
        fb.getOtherStuff();
    }

    
}