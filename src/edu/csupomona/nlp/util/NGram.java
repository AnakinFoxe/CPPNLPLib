/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

import java.util.HashMap;
import java.util.List;

/**
 * Extract n-gram information from text
 * @author Xing
 */
public class NGram {
    private Integer N;
    
    public NGram(Integer N) {
        this.N = N;
    }

    public Integer getN() {
        return N;
    }

    public void setN(Integer N) {
        this.N = N;
    }
    
    /**
     * Extract n-gram from input text and update the HashMap accordingly.
     * Please preprocess the input text before extraction.
     * @param map        HashMap to be updated
     * @param words      Array of string type words
    */
    public void updateNGram(HashMap<String, Integer> map, String[] words) 
            throws NullPointerException {        
        // create n-gram and update the HashMap
        for (int i = 0; i <= words.length - this.N; i++) {
            String ngram = words[i];
            for (int idx = 1; idx < this.N; idx++) 
                ngram += " " + words[i + idx];
            
            MapUtil.updateHashMap(map, ngram);
        } 
    }
    
    /**
     * Extract n-gram from input text and update the HashMap accordingly.
     * Please preprocess the input text before extraction.
     * @param map        HashMap to be updated
     * @param words      List of string type words
    */
    public void updateNGram(HashMap<String, Integer> map, List<String> words) 
            throws NullPointerException {        
        // create n-gram and update the HashMap
        for (int i = 0; i <= words.size() - this.N; i++) {
            String ngram = words.get(i);
            for (int idx = 1; idx < this.N; idx++) 
                ngram += " " + words.get(i + idx);
            
            MapUtil.updateHashMap(map, ngram);
        } 
    }
    
}
