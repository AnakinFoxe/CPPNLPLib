
package edu.cpp.iipl.util;

import com.google.gson.Gson;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.*;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * Calculate semantic similarity between two words
 * Currently implemented using ws4j web demo
 * ws4j: https://code.google.com/p/ws4j/
 * ws4j web demo: http://ws4jdemo.appspot.com/
 * @author Xing
 */
public class SemSimilarity {

    private class Result {
        private int input1_num;     // total number of synsets
        private int input2_num;
        private String input1;
        private String input2;
        private double score;
        private double time;

        public double getScore() { return score; }
        public double getTime() { return time; }
    }

    private class Response {
        private List<Result> result;
        private String measure;

        public List<Result> getResult() { return result; }
        public String getMeasure() { return measure; }
    }

    private Gson gson = new Gson();


    private final RelatednessCalculator hso;
    private final RelatednessCalculator lch;
    private final RelatednessCalculator lesk;
    private final RelatednessCalculator wup;
    private final RelatednessCalculator res;
    private final RelatednessCalculator jcn;
    private final RelatednessCalculator lin;
    private final RelatednessCalculator path;

    private boolean isOffline = false;

    public SemSimilarity(boolean isOffline) {
        hso = new HirstStOnge(new NictWordNet());
        lch = new LeacockChodorow(new NictWordNet());
        lesk = new Lesk(new NictWordNet());
        wup = new WuPalmer(new NictWordNet());
        res = new Resnik(new NictWordNet());
        jcn = new JiangConrath(new NictWordNet());
        lin = new Lin(new NictWordNet());
        path = new Path(new NictWordNet());

        WS4JConfiguration.getInstance().setMFS(true);

        this.isOffline = isOffline;
    }

    // get response (JSON) from url
    private String getResponseFromUrl(String strUrl) throws IOException {
        URL url = new URL(strUrl);
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(url.openStream(), "UTF-8"))) {
            int read;
            char[] chars = new char[1024];
            while ((read = br.read(chars)) != -1)
                sb.append(chars, 0, read);
        }

        return sb.toString();
    }

    // construct url to query http://ws4jdemo.appspot.com/
    private String getUrl(String measure, String word1, String word2) {
        word1 = word1.replaceAll("#", "%23");
        word2 = word2.replaceAll("#", "%23");
        return  "http://ws4jdemo.appspot.com/ws4j?measure=" + measure
                + "&args=" + word1 + "%3A%3A" + word2 + "&trace=0";
    }

    // base method for getting similarity score
    private double getSimilarityScore(String measure, String word1, String word2) throws IOException {
        // construct url
        String url = getUrl(measure, word1, word2);

        while (true) {
            try {
                // get response content from ws4j webserver
                String content = getResponseFromUrl(url);

                // parse to Response object
                Response response = gson.fromJson(content, Response.class);

                if (response.getMeasure().equals(measure) && response.getResult().size() > 0)
                    return response.getResult().get(0).getScore();
                else
                    throw new Exception("Something wrong");
            } catch (Exception e) {
                System.out.println(e.getMessage());

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public double getWup(String word1, String word2) throws IOException {
        if (isOffline)
            return wup.calcRelatednessOfWords(word1, word2);
        else
            return getSimilarityScore("wup", word1, word2);
    }

    public double getJcn(String word1, String word2) throws IOException {
        if (isOffline)
            return jcn.calcRelatednessOfWords(word1, word2);
        else
            return getSimilarityScore("jcn", word1, word2);
    }

    public double getLch(String word1, String word2) throws IOException {
        if (isOffline)
            return lch.calcRelatednessOfWords(word1, word2);
        else
            return getSimilarityScore("lch", word1, word2);
    }

    public double getLin(String word1, String word2) throws IOException {
        if (isOffline)
            return lin.calcRelatednessOfWords(word1, word2);
        else
            return getSimilarityScore("lin", word1, word2);
    }

    public double getRes(String word1, String word2) throws IOException {
        if (isOffline)
            return res.calcRelatednessOfWords(word1, word2);
        else
            return getSimilarityScore("res", word1, word2);
    }

    public double getPath(String word1, String word2) throws IOException {
        if (isOffline)
            return path.calcRelatednessOfWords(word1, word2);
        else
            return getSimilarityScore("path", word1, word2);
    }

    public double getLesk(String word1, String word2) throws IOException {
        if (isOffline)
            return lesk.calcRelatednessOfWords(word1, word2);
        else
            return getSimilarityScore("lesk", word1, word2);
    }

    public double getHso(String word1, String word2) throws IOException {
        if (isOffline)
            return hso.calcRelatednessOfWords(word1, word2);
        else
            return getSimilarityScore("hso", word1, word2);
    }
}
