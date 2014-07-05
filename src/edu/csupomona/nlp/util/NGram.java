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
    
    private boolean rmStopword = false;
    private boolean useStemmer = false;
    private Integer N;
    
    public NGram(Integer N) {
        this.N = N;
    }

    public boolean isRmStopword() {
        return rmStopword;
    }

    public void setRmStopword(boolean rmStopword) {
        this.rmStopword = rmStopword;
        
        if (!Stopword.isInitialized())
            System.out.println("Stopword class is not initialized yet.");
    }

    public boolean isUseStemmer() {
        return useStemmer;
    }

    public void setUseStemmer(boolean useStemmer) {
        this.useStemmer = useStemmer;
    }

    public Integer getN() {
        return N;
    }

    public void setN(Integer N) {
        this.N = N;
    }
    
    /*
    * Removing special characters and redundant white spaces.
    * @param text Input string text
    * @return String Processed string text
    */
    private String preprocess(String text) {
        // replacing everything except a-z A-Z 0-9 with space
        String newText = text.replaceAll("[^a-zA-Z0-9]+", " ");
        
        // replacing redundant spaces
        newText = newText.replaceAll("[ ]+", " ");
        
        return newText;
    }
    
    /*
    * Update the count of N-gram in the HashMap.
    * @param map HashMap to be updated
    * @param ngram String type N-gram
    * @return Nothing
    */
    private void updateHashMap(HashMap<String, Integer> map, String ngram) 
        throws NullPointerException{
        if (map.containsKey(ngram))
            map.put(ngram, map.get(ngram)+1);   // add one
        else
            map.put(ngram, 1);  // init as one
    }
    
    /*
    * Extract N-gram from input text and update the HashMap accordingly.
    * The input string text will be preprocessed including special characters
    * removal and redundant white space removal.
    * @param map HashMap to be updated
    * @param ngram Input string text
    * @return Nothing
    */
    public void updateNGram(HashMap<String, Integer> map, String text) 
        throws NullPointerException {
        // preprocess input text
        String procText = preprocess(text);
        
        // tokenize input text
        String[] words = procText.split(" ");
        
        // remove stopwords
        if (this.rmStopword && Stopword.isInitialized())
            words = Stopword.rmStopword(words);
        
        // stemming
//        if (this.useStemmer)
        
        // create N-gram and update the HashMap
        for (int i = 0; i <= words.length - this.N; i++) {
            String ngram = words[i];
            for (int idx = 1; idx < this.N; idx++) 
                ngram += " " + words[i + idx];
            
            updateHashMap(map, ngram);
        } 
    }
    
}
