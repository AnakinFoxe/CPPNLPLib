/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.MaxWordSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.SimpleSeg;
import com.chenlb.mmseg4j.Word;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Chinese word segmentation
 * @author Xing
 */
public class ChineseSeg {
    protected Dictionary dic;   // has to be protected 
    
    public ChineseSeg() {
        dic = Dictionary.getInstance();
        
    }
    
    private Seg chooseSeg(String segType) {
        Seg seg;
        switch (segType){
            case "S":
                // simple
                System.out.println("Simple Seg");
                seg = new SimpleSeg(dic);
                break;
            case "M":
                // max word
                System.out.println("MaxWord Seg");
                seg = new MaxWordSeg(dic);
                break;
            case "C":
                // complex
            default:
                // default using complex segmentation
                System.out.println("Complex Seg");
                seg = new ComplexSeg(dic);
                break;
        }
        return seg;
    }
    
    public List<String> toMMsegWords(String text, String segType) 
            throws IOException {
        List<String> words = new ArrayList<>();
        MMSeg mmSeg = new MMSeg(new StringReader(text), chooseSeg(segType));
        Word word = null;
        while ((word = mmSeg.next()) != null) 
            words.add(word.getString());
        
        return words;
    }
    
}
