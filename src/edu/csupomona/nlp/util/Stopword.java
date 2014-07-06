/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Xing
 */
public class Stopword {
    private static HashSet<String> stopwords;
    private static boolean initialized = false;

    public static boolean isInitialized() {
        return initialized;
    }

    public static void setInitialized(boolean initialized) {
        Stopword.initialized = initialized;
    }
	
    /*
    * Initialize the HashSet which contains every stopword
    * @param swFilePath The path to the file contains stopword in each line
    *                   NOTE: Must convert the file into UTF-8 encoding
    * @return Nothing
    */
    public static void init(String swFilePath) throws IOException {
        FileReader swFile = new FileReader(swFilePath);
        
        // since we need to be compatible for non-English words
        // the input file must be converted to UTF-8
        if (!swFile.getEncoding().equals("UTF8")) {
            System.err.println("Please convert " + swFilePath + " to UTF-8!");
            System.err.println("Current encoding: " + swFile.getEncoding());
            return;
        }
        
        BufferedReader swReader = new BufferedReader(swFile);
        String sw;

        stopwords = new HashSet<>();
        while((sw = swReader.readLine()) != null){
            stopwords.add(sw.replaceAll("\\s+", ""));   // no white space
        }
        swFile.close();

        setInitialized(true); // set the flag
    }

    /*
    * Check if the input word is a stopword
    * @param word Input string word
    * @param boolean True: is stopword, False: not stopword
    */
    public static boolean isStopword(String word) {
        return (initialized 
                && stopwords.contains(word.replaceAll("\\s+", "").toLowerCase()));
    }

    /*
    * Remove stopwords from input sentence
    * @param sentence List of string words
    * @param List List of string words with stopwords been removed
    */
    public static List<String> rmStopword(List<String> sentence) {
        List<String> newSent = new ArrayList<>();

        for (String w : sentence) {
            if (isStopword(w)) 
                continue;
            newSent.add(w);
        }

        return newSent;
    }

    /*
    * Remove stopwords from input sentence
    * @param sentence Array of string words
    * @param Array Array of string words with stopwords been removed
    */
    public static String[] rmStopword(String[] sentence) {
        List<String> newSent = new ArrayList<>();

        for (String w : sentence) {
            if (isStopword(w))
                continue;
            newSent.add(w);
        }

        String[] arrNewSent = newSent.toArray(new String[0]);

        return arrNewSent;
    }
    
}
