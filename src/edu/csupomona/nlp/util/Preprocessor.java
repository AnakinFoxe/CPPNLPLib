/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

/**
 * Preprocess text to remove unnecessary characters
 * @author Xing
 */
public class Preprocessor {
    
    private static String removeHtmlTag(String line) {
        return line.replaceAll("<.+?>", "");
    }

    private static String removeUrl(String line) {
        return line.replaceAll("http[s]?://[\\w\\d\\./]+", "");
    }

    private static String removePunctuation(String line) {
        String parsed = line.replaceAll("([0-9]+)\"", "\1 inch");
        parsed = parsed.replaceAll("\"", "");
        parsed = parsed.replaceAll("\\*", "");
        parsed = parsed.replaceAll("\\$([0-9]+)", "\1 dollars");
        parsed = parsed.replaceAll(" @ ", " at ");

        return parsed;
    }

    private static String removeHtmlCode(String line) {
        return line.replaceAll("&#[0-9]+", "");
    }

    /**
     * Removing HTML tag, HTML code, URL and punctuation.
     * @param text       Input string text
     * @return           Processed string text
    */
    public static String complex(String text) {
        String parsed = removeHtmlTag(text);
        parsed = removeUrl(parsed);
        parsed = removeHtmlCode(parsed);
        parsed = removePunctuation(parsed);

        return parsed;
    }
    
    
    /**
     * Removing special characters and redundant white spaces.
     * @param text       Input string text
     * @return           Processed string text
    */
    public static String simple(String text) {
        // replacing everything except a-z A-Z 0-9 with space
        String newText = text.replaceAll("[^a-zA-Z0-9]+", " ");
        
        // replacing redundant spaces
        newText = newText.replaceAll("[ ]+", " ");
        
        return newText;
    }
    
}
