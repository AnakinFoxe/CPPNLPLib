/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cpp.iipl.util;

import java.util.HashMap;
import java.util.List;

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
            HashMap<String, Integer> map, String[] words) 
            throws NullPointerException {        
        // create n-gram and update the HashMap
        for (int i = 0; i <= words.length - N; i++) {
            String ngram = words[i];
            for (int idx = 1; idx < N; idx++) 
                ngram += " " + words[i + idx];
            
            MapUtil.updateHashMap(map, ngram);
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
            HashMap<String, Integer> map, List<String> words) 
            throws NullPointerException {        
        // create n-gram and update the HashMap
        for (int i = 0; i <= words.size() - N; i++) {
            String ngram = words.get(i);
            for (int idx = 1; idx < N; idx++) 
                ngram += " " + words.get(i + idx);
            
            MapUtil.updateHashMap(map, ngram);
        } 
    }
    
}
