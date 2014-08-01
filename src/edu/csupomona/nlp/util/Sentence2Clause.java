/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Xing
 */
public class Sentence2Clause {
    
    private final LexicalizedParser lp;
    
    private final GrammaticalStructureFactory gsf;
    
    private final Tokenizer token;
    
    public Sentence2Clause() {
        lp = LexicalizedParser.loadModel(
            "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
            "-maxLength", "80", "-retainTmpSubcategories");
        
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        gsf = tlp.grammaticalStructureFactory();
        
        token = new Tokenizer();
    }
    
    public void process(String sentence) {
        Tree parse = lp.apply(Sentence.toWordList(token.simpleArray(sentence)));
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependencies();
        
        Iterator<TypedDependency> itr = tdl.iterator();
        while (itr.hasNext()) {
            TypedDependency td = itr.next();
            System.out.println(td.reln().getShortName() + " GOV:" + td.gov().value() + "==" + td.gov().index() + "  DEP:" + td.dep().toString());
            
        }
    }
    
    
    public static void main(String[] args) {
        
        String sentence = "I have this phone linked to my business Exchange account, and use this phone for work regularly .";
        
        Sentence2Clause s2c = new Sentence2Clause();
        
        s2c.process(sentence);
        

        
    }
    

}
