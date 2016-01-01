/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cpp.iipl.tool.translator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;


/**
 *
 * @author Xing
 */
public class Google {
    
    // RESTful service URL
    private final String MAIN_URL = 
            "https://www.googleapis.com/language/translate/v2?";
    private String accessKey = "key=";
    private String q = "&q=";
    private String source = "&source=";
    private String target = "&target=";
    
    /**
     * Create a Google object for Englisht to Simplied Chinese translation
     * @param accessKey     Public API key of your Google project
     */
    public Google(String accessKey) {
        this.accessKey += accessKey;
        this.source += "en";
        this.target += "zh-CN";
    }
    
    /**
     * Create a Google object with specified source and target language
     * For language code, please refer to:
     * https://developers.google.com/translate/v2/using_rest#language-params
     * @param accessKey     Public API key of your Google project
     * @param source        Source language code
     * @param target        Target language code
     */
    public Google(String accessKey, String source, String target) {
        this.accessKey += accessKey;
        this.source += source;
        this.target += target;
    }
    
    /**
     * Translate input text input target language
     * @param text      Input text (should be be in source language)
     * @return          Translated text (in target language)
     * @throws UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     */
    public String translate(String text) 
            throws UnsupportedEncodingException, 
            MalformedURLException, IOException {
        String query = this.q + URLEncoder.encode(text, "UTF-8");
        
        // construct the complete URL for GET
        URL url = new URL(this.MAIN_URL 
                + this.accessKey + query + this.source + this.target);
        
        int maxRetry = 10;  // retry a few more times
        for (int retry = 1; retry <= maxRetry; retry++) {
            try {
                // UTF-8 is required for multi-lingual support
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(url.openStream(), "UTF-8"));

                // construct response from Google server
                String line;
                String response ="";
                while ((line = br.readLine()) != null) 
                    response += line;

                // obtain the translated result from JSON string
                JSONObject json = new JSONObject(response);
                String translatedText = json.getJSONObject("data")
                        .getJSONArray("translations").getJSONObject(0)
                        .getString("translatedText");
                
                return translatedText;
            } catch (IOException e) {
                System.out.println("Problem with Google Translation. Retrying "
                        + retry + "/" + maxRetry);
                
                try {
                    Thread.sleep(3000 + 5000 * retry);  // incremental waiting
                } catch (InterruptedException ex) {
                    Logger.getLogger(Google.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        }
        
        throw new IOException("Failed after " + maxRetry 
                + " times retries. Abort");
    }
    
}
