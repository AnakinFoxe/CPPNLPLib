/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Xing
 */
public class Tokenizer {
    
    public Tokenizer() {
        
    }
    
    public List<String> simpleList(String sentence) {
        return new ArrayList<>(Arrays.asList(sentence.trim().split(" ")));
    }
    
    public String[] simpleArray(String sentence) {
        return sentence.trim().split(" ");
    }
    
}