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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Facebook crawler using Facebook4j.
 * @author xing
 */
public class Facebook {
    
    // Facebook4j instance
    private final facebook4j.Facebook fb_;
    
    // time stamp as crawling starting point
    private final Date startTime;
    
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
        
        // set the default start time
        // 2007-1-1, 00:00
        startTime = new Date(107, 0, 1, 0, 0);
    }
    
    /**
     * for storing info of posts and comments.
     */
    private class FacebookEntry {
        String type_;        // P: post, C: comment
        Date createdTime_;   // time of creation
        String id_;          // id of the entry from Facebook
        Integer numLikes_;   // number of likes
        
        public FacebookEntry(String type_, Date createdTime_, 
                String id_, Integer numLikes_) {
            this.type_ = type_;
            this.createdTime_ = createdTime_;
            this.id_ = id_;
            this.numLikes_ = numLikes_;
        }
        
        public String getType() {
            return type_;
        }

        public void setType(String type) {
            this.type_ = type;
        }

        public Date getCreatedTime() {
            return createdTime_;
        }

        public void setCreatedTime(Date createdTime) {
            this.createdTime_ = createdTime;
        }

        public String getId() {
            return id_;
        }

        public void setId(String id) {
            this.id_ = id;
        }

        public Integer getNumLikes() {
            return numLikes_;
        }

        public void setNumLikes(Integer numLikes) {
            this.numLikes_ = numLikes;
        }
    }
    
    /**
     * Request for exchange new long-lived (60 days expiration) access token.
     * @param appId             APP ID      
     * @param appSecret         APP Secret
     * @param userToken         Current User Access Token
     * @return                  new User Access Token (long-lived)
     */
    private AccessToken refreshToken(String appId, String appSecret, 
            String userToken) {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", appId);
        params.put("client_secret", appSecret);
        params.put("grant_type", "fb_exchange_token");
        params.put("fb_exchange_token", userToken);
        
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
        return new AccessToken(userToken);
    }
    
    public List<Page> getPages(String keyword, boolean onlyVerified) 
            throws JSONException {
        List<Page> fullPages = new ArrayList<>();
        try {
            // search pages according to keyword
            ResponseList<Page> pages = fb_.searchPages(keyword);
            for (Page page : pages) {
                // skip unverified pages only this flag is on
                if (onlyVerified) {
                    // is_verified field is only accessable through FQL
                    String query = "select is_verified from page where page_id=" 
                            + page.getId();
                    JSONObject json = fb_.executeFQL(query).getJSONObject(0);
                    boolean isVerified = json.getBoolean("is_verified");
                
                    if (!isVerified)
                        continue;
                }
                
                // retrieve full information of the page 
                Page fullPage = fb_.getPage(page.getId());
                
                fullPages.add(fullPage);
            }
        } catch (FacebookException ex) {
            Logger.getLogger(Facebook.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
        return fullPages;
    }
    
    public List<Post> getPosts(String pageId) {
        List<Post> fullPosts = new ArrayList<>();
        try {
            // cursor is the most recommended way from Facebook.
            // but I found offset is quite easy for me to implement.
            int offset = 0;
            ResponseList<Post> posts;
            while (( posts = fb_.getFeed(pageId, 
                    new Reading().offset(offset))).size() > 0) {
                for (Post post : posts) {
                    // check post creation time
                    if (post.getCreatedTime().before(startTime)) 
                        return fullPosts;
                    
                    // add post to the list
                    fullPosts.add(post);
                }
                
                // move offset
                offset += 25;   // 25 is default size of responselist
            }
            
        } catch (FacebookException ex) {
            Logger.getLogger(Facebook.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
        return fullPosts;
    }
    
    public void getComments() {
        List<Comment> fullComments = new ArrayList<>();
        try {
            Post post = fb_.getPost("114219621960016_737014276347211");
            System.out.println(post.getMessage());
            
            // get first few comments using getComments from post
            PagableList<Comment> comments = post.getComments();
            Paging<Comment> paging;
            do {
                for (Comment comment: comments)
                    fullComments.add(comment);
                
                paging = comments.getPaging();
            } while ((paging != null) && 
                    ((comments = fb_.fetchNext(paging)) != null));
            for (Comment comment : fullComments) {
                System.out.println(comment.getMessage());
            }
            System.out.println(fullComments.size());
            
//            PagableList<Like> likes = post.getLikes();
//            System.out.println(likes.getCount());
//            System.out.println(comments.getCount());
//            Paging<Like> pageLikes = likes.getPaging();
//            Cursors cursors = pageLikes.getCursors();
            
        } catch (FacebookException ex) {
            Logger.getLogger(Facebook.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws IOException, JSONException {
        Facebook fb = new Facebook();
        
//        fb.search();
        List<Post> posts = fb.getPosts("7224956785");
        for (Post post : posts)
            System.out.println(post.getCreatedTime().toString() + ": " 
                            + post.getMessage());
//        fb.getComments();
    }

    
}