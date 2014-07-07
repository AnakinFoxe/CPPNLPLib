/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Xing
 */
public class Stopword {
    private HashSet<String> stopwords;

    /*
    * Initialize the HashSet which contains every stopword
    * @param String Chose the language of stopwords
    *               "C": Chinese
    *               "E": English
    */
    public Stopword(String language) {
        this.stopwords = new HashSet<>();
        
        String swPath;
        switch (language) {
            case "C":
                // chinese stopwords
                swPath = "/res/stopwords/stopwords_c.txt";
                break;
            case "E":
                // english stopwords
            default:
                // default goes to english stopwords
                swPath = "/res/stopwords/stopwords_e.txt";
                break;
        }
        
        InputStreamReader isrSW = new InputStreamReader(
                getClass().getResourceAsStream(swPath));
        BufferedReader brSW = new BufferedReader(isrSW);
        String sw;

        try {
            while((sw = brSW.readLine()) != null){
                stopwords.add(sw.replaceAll("\\s+", ""));   // no white space
            }
        } catch (IOException ex) {
            Logger.getLogger(Stopword.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
    }

    /*
    * Check if the input word is a stopword
    * @param String Input string word
    * @param boolean True: is stopword, False: not stopword
    */
    public boolean isStopword(String word) {
        return stopwords.contains(word.replaceAll("\\s+", "").toLowerCase());
    }

    /*
    * Remove stopwords from input sentence
    * @param List<String> List of string words
    * @param List<String> List of string words with stopwords been removed
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

    /*
    * Remove stopwords from input sentence
    * @param sentence Array of string words
    * @param Array Array of string words with stopwords been removed
    */
    public String[] rmStopword(String[] sentence) {
        List<String> newSent = new ArrayList<>();

        for (String w : sentence) {
            if (isStopword(w))
                continue;
            newSent.add(w);
        }

        String[] arrNewSent = newSent.toArray(new String[0]);

        return arrNewSent;
    }
    
}
