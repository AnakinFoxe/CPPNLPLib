/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

/**
 *
 * @author Xing
 */
public class Preprocessor {
    
    
    /*
    * Removing special characters and redundant white spaces.
    * @param String Input string text
    * @return String Processed string text
    */
    public static String Simple(String text) {
        // replacing everything except a-z A-Z 0-9 with space
        String newText = text.replaceAll("[^a-zA-Z0-9]+", " ");
        
        // replacing redundant spaces
        newText = newText.replaceAll("[ ]+", " ");
        
        return newText;
    }
    
}
