/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Xing
 */
public class Sentence2Clause {
    
//    private final LexicalizedParser lp;
//    
//    private final GrammaticalStructureFactory gsf;
//    
//    private final Tokenizer token;
    
    private final StanfordCoreNLP pipeline;
    
    public Sentence2Clause() {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, parse");
        pipeline = new StanfordCoreNLP(props);
    }
    
    // the results of LexicalizedParser are not accurate 
//    public Sentence2Clause(String maxLength) {
//        lp = LexicalizedParser.loadModel(
//            "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
//            "-maxLength", maxLength, "-retainTmpSubcategories");
//        
//        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
//        gsf = tlp.grammaticalStructureFactory();
//        
//        token = new Tokenizer();
//    }
    
//    public void process(String sentence) {
//        Tree parse = lp.apply(Sentence.toWordList(token.simpleArray(sentence)));
//        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
//        Collection<TypedDependency> tdl = gs.typedDependencies();
//        
//        Iterator<TypedDependency> itr = tdl.iterator();
//        while (itr.hasNext()) {
//            TypedDependency td = itr.next();
//            System.out.println(td.reln().getShortName() 
//                    + " GOV:" + td.gov().value() + "==" + td.gov().index() 
//                    + "  DEP:" + td.dep().toString());
//        }
//    }
    
    
    public void process(String text) {
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);
        
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        
        for(CoreMap sentence: sentences) {
            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = 
                    sentence.get(BasicDependenciesAnnotation.class);
            System.out.println(dependencies.toString("plain"));
            
            for (SemanticGraphEdge edge : dependencies.getEdgeSet()) {
                System.out.println(edge.getRelation().getShortName() + ": "
                        + edge.getGovernor().value() + "(" + edge.getGovernor().index() + ") => " 
                        + edge.getDependent().value() + "(" + edge.getDependent().index() + ")");
                
            }
            
        }
    }
    
    
    public static void main(String[] args) {
        
//        String sentence = "I have this phone linked to my business Exchange account, and use this phone for work regularly .";
//        String sentence = "John, who was the CEO of a company, played golf.";
        String sentence = "I admire the fact that you are honest";
        
        Sentence2Clause s2c = new Sentence2Clause();
        
        s2c.process(sentence);
        

        
    }
    

}
