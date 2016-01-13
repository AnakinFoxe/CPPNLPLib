
package edu.cpp.iipl.util;

import com.google.gson.Gson;

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

        // get response content from ws4j webserver
        String content = getResponseFromUrl(url);

        // parse to Response object
        try {
            Response response = gson.fromJson(content, Response.class);

            if (response.getMeasure().equals(measure) && response.getResult().size() > 0)
                return response.getResult().get(0).getScore();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1D;    // -1D as "invalid" or "not similar at all" in ws4j
    }

    public double getWup(String word1, String word2) throws IOException {
        return getSimilarityScore("wup", word1, word2);
    }

    public double getJcn(String word1, String word2) throws IOException {
        return getSimilarityScore("jcn", word1, word2);
    }

    public double getLch(String word1, String word2) throws IOException {
        return getSimilarityScore("lch", word1, word2);
    }

    public double getLin(String word1, String word2) throws IOException {
        return getSimilarityScore("lin", word1, word2);
    }

    public double getRes(String word1, String word2) throws IOException {
        return getSimilarityScore("res", word1, word2);
    }

    public double getPath(String word1, String word2) throws IOException {
        return getSimilarityScore("path", word1, word2);
    }

    public double getLesk(String word1, String word2) throws IOException {
        return getSimilarityScore("lesk", word1, word2);
    }

    public double getHso(String word1, String word2) throws IOException {
        return getSimilarityScore("hso", word1, word2);
    }
}
