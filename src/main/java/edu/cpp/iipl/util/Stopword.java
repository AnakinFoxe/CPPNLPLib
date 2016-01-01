/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cpp.iipl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Determine and remove stopwords
 * @author Xing
 */
public class Stopword {
    private HashSet<String> stopwords;
    private Locale locale;

    /**
     * Initialize the HashSet with build-in stopword list
     * @param language   Chose the language of stopwords
     *                  "zh_CN": Simplified Chinese
     *                  "en": English
     *                  "es": Spanish
    */
    public Stopword(String language) {
        this.stopwords = new HashSet<>();
        
        String swPath;
        switch (language) {
            case "zh_CN":
                // chinese stopwords
                swPath = "/stopwords/zh_CN.txt";
                locale = new Locale("zh");
                break;
            case "es":
                // spanish stopwords
                swPath = "/stopwords/es.txt";
                locale = new Locale("es");
                break;
            case "en":
                // english stopwords
            default:
                // default goes to english stopwords
                swPath = "/stopwords/en.txt";
                locale = new Locale("en");
                break;
        }
        
        // read the file inside jar
        readStopwordFile(swPath);
        
    }
    
    /**
     * Initialize the HashSet with given stopword list file
     * @param language      Chose the Locale. E.g. "es" for Spanish
     * @param swPath        Path to the stopword list file 
     */
    public Stopword(String language, String swPath) {
        this.stopwords = new HashSet<>();
        
        // set Locale
        locale = new Locale(language);
        
        // read the file from providing path
        readStopwordFile(swPath);
    }
    
    /**
     * Read stopword list from file of given path
     * @param swPath        Path to the stopword list file
     */
    private void readStopwordFile(String swPath) {
        // use getResourceAsStream in Maven project will search the path
        // starting from resources/ folder
        InputStreamReader isrSW = new InputStreamReader(
                getClass().getResourceAsStream(swPath));
        BufferedReader brSW = new BufferedReader(isrSW);
        String sw;

        try {
            while((sw = brSW.readLine()) != null){
                String raw = sw.trim();
                
                // index <= 1 is used for dealing with unicode...
                if (raw.contains("#") && raw.indexOf("#") <= 1) {
                    // display comment lines
//                    System.out.println(raw);
                    continue;
                }
                
                if (raw.length() > 0) // no white space
                    stopwords.add(raw);   
            }
        } catch (IOException ex) {
            Logger.getLogger(Stopword.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Check if the input word is a stopword
     * @param word       Input string word
     * @return           True: is stopword, False: not stopword
    */
    public boolean isStopword(String word) {
        return stopwords.contains(word.replaceAll("\\s+", "")
                .toLowerCase(locale));
    }

    /**
     * Remove stopwords from input sentence
     * @param sentence       List of string words
     * @return               List of string words with stopwords been removed
    */
    public List<String> rmStopword(List<String> sentence) {
        List<String> newSent = new ArrayList<>();

        for (String w : sentence) {
            if (isStopword(w)) 
                continue;
            newSent.add(w);
        }

        return newSent;
    }

    /**
     * Remove stopwords from input sentence
     * @param sentence       Array of string words
     * @return               Array of string words with stopwords been removed
    */
    public String[] rmStopword(String[] sentence) {
        List<String> newSent = new ArrayList<>();

        for (String w : sentence) {
            if (isStopword(w))
                continue;
            newSent.add(w);
        }

        return newSent.toArray(new String[0]);
    }
    
}
