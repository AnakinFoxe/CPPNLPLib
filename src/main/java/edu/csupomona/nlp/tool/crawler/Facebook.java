/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool.crawler;

import facebook4j.FacebookFactory;

/**
 * Facebook crawler using Facebook4j
 * @author xing
 */
public class Facebook {
    
    private final facebook4j.Facebook fb;
    
    public Facebook() {
        fb = new FacebookFactory().getInstance();
    }

    
}
