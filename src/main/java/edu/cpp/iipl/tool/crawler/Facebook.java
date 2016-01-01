/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cpp.iipl.tool.crawler;

import facebook4j.Comment;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Like;
import facebook4j.PagableList;
import facebook4j.Page;
import facebook4j.Paging;
import facebook4j.Post;
import facebook4j.RawAPIResponse;
import facebook4j.Reading;
import facebook4j.ResponseList;
import facebook4j.auth.AccessToken;
import facebook4j.conf.ConfigurationBuilder;
import facebook4j.internal.org.json.JSONException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private Date startTime_;
    
    // maximum times of retries
    private int maxRetries_;
    
    // base dir
    private final String BASE_DIR_ = "./data/";
    
    // step seconds
    private final int STEP_SEC_ = 7;
    
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
        AccessToken newToken = refreshToken(appId, appSecret, userToken);
        
        // replace token
        fb_.setOAuthAccessToken(newToken);  
        
        // set the default start time
        // 2007-6-1, 00:00 
        // the year of Apple iPhone
        startTime_ = new Date(107, 5, 1, 0, 0);
        
        // set the default maximum number of retries
        maxRetries_ = 10;
        
        // trace
        System.out.println("Start Time Stamp: " + startTime_.toString() 
                        + ", Maximum Retries: " + maxRetries_);
    }

    public Date getStartTime() {
        return startTime_;
    }

    public void setStartTime(Date startTime) {
        this.startTime_ = startTime;
    }

    public int getMaxRetries() {
        return maxRetries_;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries_ = maxRetries;
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
    
    /**
     * Will be frequently used for avoiding Facebook's 600 calls in 600s limit.
     * Not really cares much about the exception.
     */
    private void pause(int sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException iex) {
        }
    }
    
    /**
     * Get all the Pages match the searching keyword.
     * @param keyword               Keyword for search
     * @param onlyVerified          Return only verified pages (currently NA)
     * @return                      HashMap of Pages
     * @throws JSONException
     */
    public HashMap<String, Page> getPages(String keyword, boolean onlyVerified) 
            throws JSONException {
        HashMap<String, Page> fullPages = new HashMap<>();
        int totalLikes = 0;
        try {
            // search pages according to keyword
            ResponseList<Page> pages = fb_.searchPages(keyword);
            System.out.println(pages.size());
            int idx = 0;
            for (Page page : pages) {
                if (onlyVerified) {
                    // TOTALLY GAVE UP DUE TO UNKNOWN REASON OF UNABLE TO 
                    // ACCESS FQL WITH APP ACCESS TOKEN OR USER ACCESS TOKEN
                    // is_verified field is only accessable through FQL
//                    String query = "select is_verified from page where page_id=" 
//                            + page.getId();
//                    JSONObject json = fb_.executeFQL(query).getJSONObject(0);
//                    boolean isVerified = json.getBoolean("is_verified");
//                
//                    // reduce speed
//                    pause(1);
//                    
//                    if (!isVerified)
//                        continue;
                }
                
                // retrieve full information of the page 
                Page fullPage = fb_.getPage(page.getId());
                
                fullPages.put(fullPage.getId(), fullPage);
                
                // records number of likes
                totalLikes += fullPage.getLikes();
                
                // to reduce speed
//                pause(1);
                
                System.out.println(idx++);
            }
        } catch (FacebookException ex) {
            Logger.getLogger(Facebook.class.getName())
                    .log(Level.SEVERE, null, ex);
        } 
        
        
        
        // post processing. only keep pages with number of likes above average 
        int average = totalLikes / fullPages.size();
        System.out.println("Average=" + average);
        List<String> removePageIds = new ArrayList<>();
        for (String pageId : fullPages.keySet()) 
            if (fullPages.get(pageId).getLikes() < average) {
                System.out.println("RM: " + fullPages.get(pageId).getName() 
                        + " [L=" + fullPages.get(pageId).getLikes().toString() + "]");
                removePageIds.add(pageId);
            }
        
        for (String pageId : removePageIds)
            fullPages.remove(pageId);
        
        return fullPages;
    }
    
    /**
     * Get all the Posts posted on the wall of the page by the page owner.
     * @param page                  Page to be parsed
     * @return                      HashMap of Posts
     */
    public HashMap<String, Post> getPosts(Page page) {
        /**
        * /feed or /posts?
        * /feed includes everything posted on the wall of page
        * which includes other users' posts
        * /posts are solely posted by the owner of the page
        * 
        * cursor, time or offset?
        * Facebook recommends cursor
        * offset is easy to implement but somehow couldn't get all the posts
        * using time stamp so far is the best choice
        */
        HashMap<String, Post> fullPosts = new HashMap<>();
        ResponseList<Post> posts = null;
        // start getting posts of the page
        for (int n = 1; n <= maxRetries_; ++n) {
            try {
                posts = fb_.getPosts(page.getId(), 
                    new Reading().since(startTime_));
            } catch (FacebookException ex) {    // exception & retry
                Logger.getLogger(Facebook.class.getName())
                    .log(Level.SEVERE, null, ex);
                pause(STEP_SEC_*n);
                System.out.println("Starting retry... " 
                        + n + "/" + maxRetries_);
                continue;
            } 
            break;
        }
        
        // eventually failed
        if (posts == null)
            return fullPosts;
        
        // get rest of posts
        Paging<Post> paging;
        do {
            for (Post post : posts) 
                if (post.getMessage() != null) {
                    // seems getting next page of posts will in fact
                    // ignore the starting time I used...
                    if (post.getCreatedTime().before(startTime_))
                        return fullPosts;

                    // add post to the list
                    fullPosts.put(post.getId(), post);
                }
            
            // get next page
            paging = posts.getPaging();

            // to reduce speed
            pause(1);

            // trace
            if (posts.size() > 0)
                System.out.println(posts.get(0).getCreatedTime().toString() 
                    + ": " + fullPosts.size());
            
            // get next page
            if (paging != null) 
                for (int n = 1; n <= maxRetries_; ++n) {
                    try {
                        posts = fb_.fetchNext(paging);
                    } catch (FacebookException ex) {    // exception & retry
                        Logger.getLogger(Facebook.class.getName())
                            .log(Level.SEVERE, null, ex);
                        pause(STEP_SEC_*n);
                        System.out.println("Starting retry... " 
                                + n + "/" + maxRetries_);
                        continue;
                    } 
                    break;
                }
            
        } while ((paging != null) && (posts != null));
        
        return fullPosts;
    }
    
    /**
     * Get all the Comments for the Post. 
     * The replies to the comments are not included, because Facebook does not
     * provide such API query.
     * @param post              Post to be parsed
     * @return                  HashMap of Comments
     */
    public HashMap<String, Comment> getComments(Post post) {
        HashMap<String, Comment> fullComments = new HashMap<>();
        
        // get first few comments using getComments from post
        PagableList<Comment> comments = post.getComments();
        Paging<Comment> paging;
        do {
            // NOTE: so far didn't figure out how to get replies 
            // for the comments
            for (Comment comment: comments)
                fullComments.put(comment.getId(), comment);

            // get next page
            // NOTE: somehow few comments will not be included.
            // however, this won't affect much on our research
            paging = comments.getPaging();

            // to reduce speed
            pause(1);
            
            // trace
            System.out.println("Getting comments... " + fullComments.size());
            
            // get next page
            if (paging != null)
                for (int n = 1; n <= maxRetries_; ++n) {
                    try {
                        comments = fb_.fetchNext(paging);
                    } catch (FacebookException ex) {    // exception & retry
                        Logger.getLogger(Facebook.class.getName())
                            .log(Level.SEVERE, null, ex);
                        pause(STEP_SEC_*n);
                        System.out.println("Starting retry... " 
                                + n + "/" + maxRetries_);
                        continue;
                    } 
                    break;
                }
        } while ((paging != null) && (comments != null));
        
        return fullComments;
    }
    
    /**
     * Get all the Likes for the Post.
     * Specific for getting likes of the post. As for comment, getLikeCount() 
     * could be used.
     * @param post              Post to be parsed
     * @return                  HashMap of Likes
     */
    public HashMap<String, Like> getLikes(Post post) {
        HashMap<String, Like> fullLikes = new HashMap<>();
        PagableList<Like> likes = post.getLikes();
        Paging<Like> paging;

        // trace
        System.out.print("Getting Likes... ");
        
        do {
            for (Like like : likes)
                fullLikes.put(like.getId(), like);

            // get next page
            paging = likes.getPaging();

            // to reduce speed
            pause(1);
            
            
            
            // get next page
            if (paging != null)
                for (int n = 1; n <= maxRetries_; ++n) {
                    try {
                        likes = fb_.fetchNext(paging);
                    } catch (FacebookException ex) {    // exception & retry
                        Logger.getLogger(Facebook.class.getName())
                            .log(Level.SEVERE, null, ex);
                        pause(STEP_SEC_*n);
                        System.out.println("Starting retry... " 
                                + n + "/" + maxRetries_);
                        continue;
                    } 
                    break;
                }
        } while ((paging != null) && (likes != null));
            
        // trace
        System.out.print(fullLikes.size() + "\n");
        
        return fullLikes;
    }
    
    /**
     * Start crawling Facebook using keyword.
     * Crawled data includes information for Posts and their Comments.
     * Will be stored in the file for each Page.
     * @param keyword               Keyword for searching
     * @throws JSONException
     * @throws IOException
     */
    public void crawl(String keyword) throws JSONException, IOException {
        //trace
        System.out.println("Start crawling with keyword=" + keyword);
        
        // get pages according to keyword
        HashMap<String, Page> pages = getPages(keyword, true);
        
        // crawl each page
        for (String pageId : pages.keySet()) {
            String filename = pageId + "_" 
                    + pages.get(pageId).getName().replaceAll(" ", "_") + "_"
                    + pages.get(pageId).getLikes().toString()
                    + ".txt";
            
            String fullPath = BASE_DIR_ + keyword + "/" + filename;
            System.out.println(fullPath);
            
            // start writing
            FileWriter fw = new FileWriter(fullPath);
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                // get posts from the page
                HashMap<String, Post> posts = getPosts(pages.get(pageId));
                
                for (String postId : posts.keySet()) {
                    Post post = posts.get(postId);
                    
                    // get likes
                    HashMap<String, Like> likes = getLikes(post);
                    
                    int shareCount = (post.getSharesCount()!=null ? 
                                        post.getSharesCount() : 0);
                    // write post information
                    String line = "[P]["                            // type
                        + post.getCreatedTime().toString() + "]["   // date
                        + post.getId() + "]["                       // id
                        + likes.size() + "]["                       // number of likes
                        + shareCount + "]:"                         // number of shares
                        + post.getMessage()                         // content
                        + "\n";
                    System.out.println(line);
                    bw.write(line);
                    
                    // get comments
                    HashMap<String, Comment> comments = getComments(post);
                    
                    // write comment information
                    for (String comId : comments.keySet()) {
                        Comment comment = comments.get(comId);
                        
                        line = "[C]["                                       // type
                            + comment.getCreatedTime().toString() + "]["    // date
                            + comment.getId() + "]["                        // id
                            + comment.getLikeCount().toString() + "]:"      // number of likes
                            + comment.getMessage()
                            + "\n";
                        System.out.println(line);
                        bw.write(line);
                    }
                }
                
                // save the time stamp
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                bw.write("#### Finished at: " 
                        + df.format(Calendar.getInstance().getTime()) 
                        + " ####\n");
            }
        }
    }
    
}