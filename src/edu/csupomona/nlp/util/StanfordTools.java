/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.AnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Wrap Stanford Core NLP for our own usage
 * TODO: This is a very initial version
 * @author Xing
 */
public class StanfordTools {
    private final StanfordCoreNLP pipeline;
    
    public StanfordTools() {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
    }
    
    public StanfordTools(Properties props) {
        pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Get sentiment for the input sentence
     * @param sentence      Input string sentence
     * @return          Sentiment score
     *                  0 = very negative, 
     *                  1 = negative, 
     *                  2 = neutral, 
     *                  3 = positive,
     *                  4 = very positive
     */
    public int sentiment(String sentence) {
        if (sentence == null || sentence.length() == 0)
            return 2;

        Annotation ano = pipeline.process(sentence);

        for (CoreMap sent : ano.get(SentencesAnnotation.class)) {
            Tree tree = sent.get(AnnotatedTree.class);
            return RNNCoreAnnotations.getPredictedClass(tree);
        }

        return 2;
    }
	
    /**
     * Lemmatize the given word
     * @param word      Input string word
     * @return          Lemmatized word
     */
    public String lemmatize(String word)
    {
        String lemma = null;

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(word);

        // run all Annotators on this text
        pipeline.annotate(document);

        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the
                // list of lemmas
            	lemma = token.get(LemmaAnnotation.class);
            }
        }

        return lemma;
    }
	
    /**
     * POS tag the given input string
     * @param text      Input string text
     * @return          List of POS tagged result.
     *                  word[0]: word from the original text,
     *                  word[1]: POS tag of the word
     */
    public List<String[]> posTag(String text) {
        List<String[]> list = new ArrayList<>();

        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                String[] word = new String[2];

                word[0] = token.get(TextAnnotation.class);
                word[1] = token.get(PartOfSpeechAnnotation.class);

                list.add(word);
            }
        }

        return list;
    }
	
    /**
     * Dependency parse tree 
     * @param text      Input string text
     */
    public void parser(String text) {
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {			
            SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);

            System.out.println(dependencies.toString("plain"));
        }
    }
    
    /**
     * Detect and break input text into sentences
     * @param text      Input text
     * @return          List of sentences
     */
    public List<String> sentence(String text) {
        List<String> listSentence = new ArrayList<>();
        
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {			
            listSentence.add(sentence.toString());
        }
        
        return listSentence;
    }
    
    /**
     * A few sample code to show basic usage of Stanford Core NLP.
     * http://stackoverflow.com/questions/10688739/resolve-coreference-using-stanford-corenlp-unable-to-load-parser-model
     * @param args 
     */
    public static void main(String[] args) {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // read some text in the text variable
        String text = "Mary has a little lamb. She is very cute."; // Add your text here!

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);       
        }

        // this is the parse tree of the current sentence
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);

        // this is the Stanford dependency graph of the current sentence
        SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);

        }

        // This is the coreference link graph
        // Each chain stores a set of mentions that link to each other,
        // along with a method for getting the most representative mention
        // Both sentence and token offsets start at 1!
        Map<Integer, CorefChain> graph = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
        System.out.println(graph);
    }
}
