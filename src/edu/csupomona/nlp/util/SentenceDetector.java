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
import java.util.Properties;

/**
 *
 * @author Xing
 */
public class SentenceDetector {
    
    private final BreakIterator breakIter_;
    private final StanfordTools stanford_;
    
    public SentenceDetector() {
        breakIter_ = BreakIterator.getSentenceInstance(Locale.US);
        
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit");
        stanford_ = new StanfordTools(props);
    }
    
    /**
     * Detect and break input text into sentences
     * @param text      Input text
     * @return          List of sentences
     */
    public List<String> simple(final String text) {
        List<String> sentences = new ArrayList<>();
    
        breakIter_.setText(text);
        int start = breakIter_.first();
        // loop through each sentence
        for (int end = breakIter_.next(); end != BreakIterator.DONE;
                start = end, end = breakIter_.next()) {
            sentences.add(text.substring(start,end).trim());
        }
        
        return sentences;
    }
    
    /**
     * Using Stanford Core NLP to detect sentences
     * @param text      Input text
     * @return          List of sentences
     */
    public List<String> complex(final String text) {
        return stanford_.sentence(text);
    }
    
}
