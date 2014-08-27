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
import facebook4j.Post;
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
                .setOAuthAppId(key.getProperty("AppID"))
                .setOAuthAppSecret(key.getProperty("AppSecret"))
                .setOAuthAccessToken(key.getProperty("myToken"))
                .setOAuthPermissions(key.getProperty("Permissions"));
        
        // create Facebook4j instance
        fb_ = new FacebookFactory(cb.build()).getInstance();

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
            // TODO: won't be able to display all the post
            // need to figure out how to get next page
            ResponseList<Post> posts = fb_.getPosts("114219621960016");
            for (Post post : posts) {
                System.out.println(post.getCreatedTime().toString() + ":" 
                        + " messge:" + post.getMessage()
                        + " story:" + post.getStory()
                        + " caption:" + post.getCaption()
                        + " desc:" + post.getDescription()
                        + " name:" + post.getName()
                        + " id:" + post.getId());
            }
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