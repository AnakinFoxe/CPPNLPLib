package edu.cpp.iipl.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Does not have the training part
 *
 * Created by xing on 2/26/16.
 */
public class Word2Vec {

    private final Map<String, double[]> vectorsMap;

    public Word2Vec() {
        vectorsMap = new HashMap<>();
    }

    public void readVectors(String path) throws IOException {
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);

        try {
            // get num of words & num of vectors from the first line
            String line = br.readLine();
            if (line == null)
                return;
            String[] items = line.trim().split(" ");
            int numOfWords = Integer.parseInt(items[0]);
            int numOfVectors = Integer.parseInt(items[1]);

            // read all the vectors
            for (int w = 0; w < numOfWords; ++w) {
                if ((line = br.readLine()) == null)
                    return;

                items = line.trim().split(" ");
                String word = items[0];
                double length = 0;
                double[] vectors = new double[numOfVectors];
                for (int v = 0; v < numOfVectors; ++v) {
                    vectors[v] = Double.parseDouble(items[v + 1]);
                    length += vectors[v] * vectors[v];
                }
                length = Math.sqrt(length);
                for (int v = 0; v < numOfVectors; ++v)
                    vectors[v] /= length;

                vectorsMap.put(word, vectors);
            }

        } finally {
            br.close();
            fr.close();
        }
    }


    public double[] getVectors(String word) {
        return vectorsMap.get(word);
    }


    public double getSimilarity(String word1, String word2) {
        double[] vectors1 = vectorsMap.get(word1);
        double[] vectors2 = vectorsMap.get(word2);

        double sim = 0;
        if (vectors1 != null && vectors2 != null) {
            for (int v = 0; v < vectors1.length; ++v)
                sim += vectors1[v] * vectors2[v];
        } else
            sim = -1;   // TODO: not sure about this one

        // for now, all the words should present in the vectors map
        // because the testing documents will be part of the corpus

        return sim;
    }

    public class Pair {
        public String word;
        public double sim;

        public Pair(String word, double sim) {
            this.word = word;
            this.sim = sim;
        }
    }

    public List<Pair> getClosestWords(String word, int num) {
        // use priority queue to find the words have highest similarity
        // with the given word
        PriorityQueue<Pair> pq = new PriorityQueue<>(10, new Comparator<Pair>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                if (o2.sim > o1.sim)
                    return 1;
                else if (o2.sim < o1.sim)
                    return -1;
                else
                    return 0;
            }
        });

        for (String otherWord : vectorsMap.keySet()) {
            if (!otherWord.equals(word)) {
                double sim = getSimilarity(word, otherWord);
                pq.add(new Pair(otherWord, sim));
            }
        }

        List<Pair> closest = new ArrayList<>();
        for (int w = 0; w < num; ++w)
            closest.add(pq.poll());

        return closest;
    }

}
