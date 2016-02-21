/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cpp.iipl.util;

/**
 * Preprocess text to remove unnecessary characters.
 * Note: only suitable for English
 * @author Xing
 */
public class Preprocessor {
    
    private static String removeHtmlTag(String line) {
        return line.replaceAll("<.+?>", " ");
    }

    private static String removeUrl(String line) {
        String parsed = line.replaceAll("http[s]?://[\\w\\d\\./]+", " ");
        parsed = parsed.replaceAll("([a-zA-Z0-9]+\\.)+[a-zA-Z]+", " ");
        
        return parsed;
    }

    private static String removeHtmlCode(String line) {
        return line.replaceAll("&#[0-9]+", " ");
    }
    
    private static String removeEmail(String line) {
        return line.replaceAll("[a-zA-Z0-9\\.]+@[a-zA-Z0-9\\.]+[a-zA-Z]", "email address");
    }

    
    private static String removeBetweenBrackets(String line) {
        return line.replaceAll("\\(.+?\\)", " ");
    }
    
    private static String removePunctuation(String line) {
        String parsed = line.replaceAll(" ([0-9]+)\"", "$1 inch");
        parsed = parsed.replaceAll("\"", " ");
        parsed = parsed.replaceAll("\\*", " ");
        parsed = parsed.replaceAll("\\$([0-9]+)", "$1 dollars");
        parsed = parsed.replaceAll(" @[ ]?", " at ");

        parsed = parsed.replaceAll("[\\-+_/]", "");
        
        parsed = parsed.replaceAll("([,?!])", " $1 ");
        parsed = parsed.replaceAll("([a-zA-Z])\\.", "$1 \\. ");
        parsed = parsed.replaceAll("\\.([a-zA-Z])", " \\. $1");

        parsed = parsed.replaceAll("\\.[ \\.]*\\.", "\\.");
        parsed = parsed.replaceAll(",[ ,]*,", ",");

        return parsed;
    }
    
    private static String removeIrregularSymbols(String line) {
        return line.replaceAll("[^a-zA-Z0-9,.?!%&']", " ");
    }
    
    private static String removeSpaces(String line) {
        return line.replaceAll("[ ]+", " ");
    }

    /**
     * Removing HTML tag, HTML code, URL and punctuation.
     * @param text       Input string text
     * @return           Processed string text
    */
    public static String complex(String text) {
        // specific complexities
        String parsed = removeHtmlTag(text);
        parsed = removeUrl(parsed);
        parsed = removeHtmlCode(parsed);
        parsed = removeEmail(parsed);
        
        // general stuff
        parsed = removeBetweenBrackets(parsed);
        parsed = removePunctuation(parsed);
        parsed = removeIrregularSymbols(parsed);
        
        // final processing
        parsed = removeSpaces(parsed);
        
        return parsed.toLowerCase();
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
