/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cpp.iipl.util;

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
    
    private final String language_;
    
    public SentenceDetector(String language) {
        language_ = language;
        
        switch (language) {
            case "zh_CN":
                breakIter_ = BreakIterator.getSentenceInstance(Locale.CHINESE);
                break;
            case "es":
                breakIter_ = BreakIterator.getSentenceInstance(new Locale("es"));
                break;
            case "en":
            default:
                // default goes to English
                breakIter_ = BreakIterator.getSentenceInstance(Locale.US);
                break;
        }

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit");
        stanford_ = new StanfordTools(props);
    }
    
    /**
     * Sentence detection for Chinese 
     * @param text      Input text
     * @return          List of sentences
     */
    private List<String> chinese(final String text) {  
        List<String> sentences = new ArrayList<>();  
        
        // preprocess the text  
        String temp = text 
                .replaceAll("~", "。")  
                .replaceAll("～", "。")  
                .replaceAll("！", "。")  
                .replaceAll("!", "。")  
                .replaceAll("？", "。")  
                .replaceAll("﹖", "。")  
                .replaceAll(";", "。")  
                .replaceAll("；", "。")  
                .replaceAll("。+", "。")  
                .replaceAll("\\.\\.", "。")  
                .replaceAll("……", "。");   

        breakIter_.setText(temp);
        int start = breakIter_.first();
        // loop through each sentence
        for (int end = breakIter_.next(); end != BreakIterator.DONE;
                start = end, end = breakIter_.next()) {
            sentences.add(temp.substring(start,end).trim());
        }
        
        return sentences;  
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
        if (language_.equals("zh_CN"))
            return chinese(text);
        else
            return stanford_.sentence(text);
    }
    
}
