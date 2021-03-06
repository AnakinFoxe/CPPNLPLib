/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cpp.iipl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extract n-gram information from text
 * @author Xing
 */
public class NGram {
    
    private NGram() {
    }
    
    /**
     * Extract n-gram from input text and update the HashMap accordingly.
     * Please preprocess the input text before extraction.
     * @param N         N of n-gram
     * @param map       HashMap to be updated
     * @param words     Array of string type words
    */
    public static void updateNGram(Integer N,
                                   Map<String, Integer> map, String[] words)
            throws NullPointerException {        
        // create n-gram and update the HashMap
        for (int i = 0; i <= words.length - N; i++) {
            String ngram = words[i];
            for (int idx = 1; idx < N; idx++) 
                ngram += " " + words[i + idx];
            
            MapUtil.updateMap(map, ngram);
        } 
    }
    
    /**
     * Extract n-gram from input text and update the HashMap accordingly.
     * Please preprocess the input text before extraction.
     * @param N         N of n-gram
     * @param map       HashMap to be updated
     * @param words     List of string type words
    */
    public static void updateNGram(Integer N,
                                   Map<String, Integer> map, List<String> words)
            throws NullPointerException {        
        // create n-gram and update the HashMap
        for (int i = 0; i <= words.size() - N; i++) {
            String ngram = words.get(i);
            for (int idx = 1; idx < N; idx++) 
                ngram += " " + words.get(i + idx);
            
            MapUtil.updateMap(map, ngram);
        } 
    }

    /**
     * Create n-gram from unigram input text.
     * Using "double (left & right) padding"
     * @param N
     * @param unigrams
     * @return
     */
    public static List<String> unigram2NGram(Integer N, String padding,
                                             List<String> unigrams)
            throws NullPointerException {
        List<String> ngrams = new ArrayList<>();

        if (unigrams.size() == 0)
            return ngrams;

        for (int i = 1 - N; i < unigrams.size(); ++i) {
            String ngram = "";
            for (int j = 0; j < N; ++j) {
                int idx = i + j;
                if ((idx >= 0) && (idx < unigrams.size()))
                    ngram += unigrams.get(idx);
                else
                    ngram += padding;
                ngram += " ";
            }

            ngrams.add(ngram.trim());
        }

        return ngrams;
    }
    
}
