/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cpp.iipl.util;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.MaxWordSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.SimpleSeg;
import com.chenlb.mmseg4j.Word;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Chinese word segmentation
 * Applied mmseg4j-core library 
 * https://github.com/chenlb/mmseg4j-core
 * @author Xing
 */
public class ChineseSeg {
    protected Dictionary dic;   // has to be protected 
    
    public ChineseSeg() {
        dic = Dictionary.getInstance(); // initialize dictionary
        
    }
    
    /**
     * Choose the type of segmentation algorithm
     * @param segType    type of segmentation algorithm
     *                   "S": simple
     *                   "C": complex (default)
     *                   "M": max word
     * @return           Segmentation algorithm
    */
    private Seg chooseSeg(String segType) {
        Seg seg;
        switch (segType){
            case "S":
                // simple
                seg = new SimpleSeg(dic);
                break;
            case "M":
                // max word
                seg = new MaxWordSeg(dic);
                break;
            case "C":
                // complex
            default:
                // default using complex segmentation
                seg = new ComplexSeg(dic);
                break;
        }
        return seg;
    }
    
    /**
     * Use MMSeg algorithm to segment input string text into words and phrases
     * @param text       Input string text
     * @param segType    Type of segmentation algorithm.
     *                   "S": simple
     *                   "C": complex (default)
     *                   "M": max word
     * @return           List of words and phrases
     * @throws java.io.IOException
    */
    public List<String> toMMsegWords(String text, String segType) 
            throws IOException {
        List<String> words = new ArrayList<>();
        MMSeg mmSeg = new MMSeg(new StringReader(text), chooseSeg(segType));
        Word word;
        while ((word = mmSeg.next()) != null) 
            words.add(word.getString());
        
        return words;
    }
    
}
