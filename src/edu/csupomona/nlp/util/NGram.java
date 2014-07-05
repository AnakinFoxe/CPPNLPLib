/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

import java.util.HashMap;

/**
 *
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
    
    
    /*
    * Extract N-gram from input text and update the HashMap accordingly.
    * The input string text will be preprocessed including special characters
    * removal and redundant white space removal.
    * @param map HashMap to be updated
    * @param ngram Input string text
    * @return Nothing
    */
    public void updateNGram(HashMap<String, Integer> map, String[] words) 
        throws NullPointerException {        
        // create N-gram and update the HashMap
        for (int i = 0; i <= words.length - this.N; i++) {
            String ngram = words[i];
            for (int idx = 1; idx < this.N; idx++) 
                ngram += " " + words[i + idx];
            
            MapUtil.updateHashMap(map, ngram);
        } 
    }
    
}
