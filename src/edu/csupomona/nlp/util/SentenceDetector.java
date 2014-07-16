/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Xing
 */
public class SentenceDetector {
    
    private static final BreakIterator breakIter = 
            BreakIterator.getSentenceInstance(Locale.US);;
    
    /**
     * Detect and break input text into sentences
     * @param text      Input text
     * @return          List of sentences
     */
    public static List<String> simple(String text) {
        List<String> sentences = new ArrayList<>();
    
        breakIter.setText(text);
        int start = breakIter.first();
        // loop through each sentence
        for (int end = breakIter.next(); end != BreakIterator.DONE;
                start = end, end = breakIter.next()) {
            sentences.add(text.substring(start,end));
        }
        
        return sentences;
    }
    
}
