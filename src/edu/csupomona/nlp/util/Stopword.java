/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

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
     * Initialize the HashSet which contains every stopword
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
                swPath = "/res/stopwords/zh_CN.txt";
                locale = new Locale("zh");
                break;
            case "es":
                // spanish stopwords
                swPath = "/res/stopwords/es.txt";
                locale = new Locale("es");
                break;
            case "en":
                // english stopwords
            default:
                // default goes to english stopwords
                swPath = "/res/stopwords/en.txt";
                locale = new Locale("en");
                break;
        }
        
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
                    System.out.println(raw);
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
