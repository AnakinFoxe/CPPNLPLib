/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.util;

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
import edu.stanford.nlp.util.CoreMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Wrap Stanford Core NLP for our own usage
 * TODO: This is a very initial version
 * @author Xing
 */
public class StanfordTools {
    private static StanfordCoreNLP pipeline;

    public static void init() {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
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
    public static int sentiment(String sentence) {
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
    public static String lemmatize(String word)
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
    public static List<String[]> posTag(String text) {
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
    public static void parser(String text) {
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {			
            SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);

            System.out.println(dependencies.toString("plain"));
        }
    }
    
    public static List<String> sentence(String text) {
        List<String> listSentence = new ArrayList<>();
        
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {			
            listSentence.add(sentence.toString());
        }
        
        return listSentence;
    }
}
