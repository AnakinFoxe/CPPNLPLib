/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cpp.iipl.tool.preprocessor;

import java.util.ArrayList;
import java.util.List;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.frenchStemmer;
import org.tartarus.snowball.ext.spanishStemmer;

/**
 * Using Snowball stemmer 
 * http://snowball.tartarus.org/
 * Currently only support English, Spanish and French stemmers
 * @author Xing
 */
public class Stemmer {
    private SnowballStemmer snowball;
    
    /**
     * Initialize a snowball stemmer for stemming
     * @param language      Chose the language of stemmer
     *                      "en": English
     *                      "es": Spanish
     *                      "fr": French
     */
    public Stemmer(String language) {
        switch (language) {
            case "es":
                snowball = new spanishStemmer();
                break;
            case "fr":
                snowball = new frenchStemmer();
                break;
            case "en":
            default:
                // default goes to English stemmer
                snowball = new englishStemmer();
//                System.out.println("Default: using English stemmer.");
                break;
        }
    }
    
    /**
     * Stem a single word
     * @param word          The word to be stemmed
     * @return              Stemmed word
     */
    public String stemWord(String word) {
        snowball.setCurrent(word);
        snowball.stem();
        return snowball.getCurrent();
    }
    
    /**
     * Stem the list of words
     * @param words         Input list of words
     * @return              Stemmed list of words
     */
    public List<String> stemWords(List<String> words) {
        List<String> stemmed = new ArrayList<>();
        
        for (String word : words)
            stemmed.add(stemWord(word));
        
        return stemmed;
    }
    
    /**
     * Stem the array of words
     * @param words         Input array of words
     * @return              Stemmed array of words
     */
    public String[] stemWords(String[] words) {
        List<String> stemmed = new ArrayList<>();
        
        for (String word : words) 
            stemmed.add(stemWord(word));
        
        return stemmed.toArray(new String[stemmed.size()]);
    }
    
}
