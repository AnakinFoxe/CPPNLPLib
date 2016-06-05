package edu.cpp.iipl.tool.feature.extractor;

import java.util.*;

/**
 * Created by xing on 4/22/16.
 */
public class TfIdf {

    private Set<String> dict;

    private int[] docLens;

    private Map<String, float[]> tfIdf;


    public TfIdf(Set<String> dict, List<List<String>> documents) {
        this.dict = dict;
        this.docLens = new int[documents.size()];

        Map<String, int[]> tf = getTf(documents);

        tfIdf = getTfIdf(tf);
    }

    public Map<String, int[]> getTf(List<List<String>> documents) {
        Map<String, int[]> tf = new HashMap<>();

        int numOfDocs = documents.size();


        // update TF matrix with token count
        for (int i = 0; i < documents.size(); ++i) {
            for (String token : documents.get(i)) {
                // only count tokens in the dictionary
                if (!dict.contains(token))
                    continue;

                int[] vector;
                if (tf.containsKey(token)) {
                    vector = tf.get(token);
                }else
                    vector = new int[numOfDocs];

                ++vector[i];
                tf.put(token, vector);
            }
        }

        return tf;
    }

    public Map<String, float[]> getTfIdf(Map<String, int[]> tf) {
        Map<String, float[]> tfIdf = new HashMap<>();

        for (String token : tf.keySet()) {
            // count df
            int df = 0;
            int[] vector = tf.get(token);
            for (int i = 0; i < vector.length; ++i)
                if (vector[i] > 0) {
                    ++df;
                    ++docLens[i];
                }

            // compute idf
            float idf = df != 0 ? (float) Math.log((float) vector.length / df) : 0;

            // compute tf-idf
            float[] tfIdfVector = tfIdf.containsKey(token) ?
                    tfIdf.get(token) : new float[vector.length];
            for (int i = 0; i < vector.length; ++i) {
                // compute logarithmic scaled tf
                float ltf = vector[i] > 0 ? 1 + (float) Math.log(vector[i]) : 0;
                tfIdfVector[i] = ltf * idf;
            }

            tfIdf.put(token, tfIdfVector);
        }

        return tfIdf;
    }

    // input document vs document with docId of this class
    // using the approximation algorithm in Figure 7.1 of <Information Retrieval>
    public float cosineSimilarity(int docId, List<String> document) {
        if (docId >= docLens.length) {
            System.out.println("Invalid docId: " + docId);
            return Float.MIN_VALUE;
        }

        float cosine = 0;
        float docLen = docLens[docId];

        for (String token : document)
            if (tfIdf.containsKey(token))
                cosine += tfIdf.get(token)[docId];

        if (docLen != 0)
            cosine /= docLen;
        else
            return 0;

        return cosine;
    }

}
