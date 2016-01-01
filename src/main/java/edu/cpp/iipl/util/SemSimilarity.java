/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cpp.iipl.util;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import java.util.List;

/**
 * Calculate semantic similarity between two words
 * Currently implemented using ws4j
 * ws4j: https://code.google.com/p/ws4j/
 * @author Xing
 */
public class SemSimilarity {
    private final ILexicalDatabase db;
    private RelatednessCalculator rc;
    private List<POS[]> posPairs;
    
    public SemSimilarity() {
        db = new NictWordNet();
        WS4JConfiguration.getInstance().setMFS(true);

        // only support Jcn for now
        rc = new JiangConrath(db);
        posPairs = rc.getPOSPairs();
    }

    public RelatednessCalculator getRc() {
        return rc;
    }

    public void setRc(RelatednessCalculator rc) {
        this.rc = rc;
        posPairs = rc.getPOSPairs();
    }
    
    
    
    /**
     * Calculate semantic similarity between two words
     * @param word1     First word
     * @param word2     Second word
     * @return          Semantic similarity score
     */
    public double calSim(String word1, String word2) {
        double maxScore = -1D;
        double score;

        List<Concept> synsets1;
        List<Concept> synsets2;
        Relatedness relatedness;

        // search through synsets for two words 
        // and find the highest similarity score
        for(POS[] posPair: posPairs) {
            synsets1 = (List<Concept>)db.
                    getAllConcepts(word1, posPair[0].toString());
            synsets2 = (List<Concept>)db.
                    getAllConcepts(word2, posPair[1].toString());

            for(Concept synset1: synsets1) {
                for (Concept synset2: synsets2) {
                    relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
                    score = relatedness.getScore();
                    if (score > maxScore) { 
                        maxScore = score;
                    }
                }
            }
        }

        if (maxScore == -1D) {
            maxScore = 0.0;
        }

        return maxScore;
    }
}
