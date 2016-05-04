package edu.cpp.iipl.tool.feature.extractor;

import java.util.List;

/**
 * Created by xing on 4/22/16.
 */
public class Overlap {


    public static int numOfStr1InStr2(String str1, String str2) {
        if (str1 == null || str2 == null || str1.length() == 0 || str2.length() == 0)
            return 0;

        int idx = 0;
        int count = 0;
        while (idx < str2.length() && (idx = str2.indexOf(str1, idx)) != -1) {
            ++count;
            idx += str1.length();
        }

        return count;
    }

    public static int numOfStr1InStr2(List<String> str1, List<String> str2) {
        if (str1 == null || str2 == null || str1.size() == 0 || str2.size() == 0 || str1.size() > str2.size())
            return 0;

        int count = 0;
        for (int i = 0; i < str2.size(); ++i) {
            if (str1.get(0).equals(str2.get(i))) {
                for (int j = 1; j < str1.size(); ++j) {
                    if ((i + j >= str2.size()) || (!str1.get(j).equals(str2.get(i + j))))
                        break;

                    if (i == str1.size() - 1) {
                        ++count;
                        i += str1.size() - 1;
                    }
                }
            }
        }

        return count;
    }


    public static int numOfWordInWords(String word, String[] words) {
        if (word == null || words == null || word.length() == 0 || words.length == 0)
            return 0;

        int count = 0;
        for (String token : words)
            if (token.equals(word))
                ++count;

        return count;
    }

    public static int numOfWordInWords(String word, List<String> words) {
        if (word == null || words == null || word.length() == 0 || words.size() == 0)
            return 0;

        int count = 0;
        for (String token : words)
            if (token.equals(word))
                ++count;

        return count;
    }

    public static int numOfWords1InWords2(String[] words1, String[] words2) {
        if (words1 == null || words2 == null || words1.length == 0 || words2.length == 0)
            return 0;

        int count = 0;
        for (String word : words1)
            count += numOfWordInWords(word, words2);

        return count;
    }

    public static int numOfWords1InWords2(List<String> words1, List<String> words2) {
        if (words1 == null || words2 == null || words1.size() == 0 || words2.size() == 0)
            return 0;

        int count = 0;
        for (String word : words1)
            count += numOfWordInWords(word, words2);

        return count;
    }

    public static double ratioOfWords1InWords2(String[] words1, String[] words2) {
        if (words1 == null || words2 == null || words1.length == 0 || words2.length == 0)
            return 0;

        int numOfOverlap = numOfWords1InWords2(words1, words2);
        int lenOfWords2 = words2.length;

        return (double) numOfOverlap / lenOfWords2;
    }

    public static double ratioOfWords1InWords2(List<String> words1, List<String> words2) {
        if (words1 == null || words2 == null || words1.size() == 0 || words2.size() == 0)
            return 0;

        int numOfOverlap = numOfWords1InWords2(words1, words2);
        int lenOfWords2 = words2.size();

        return (double) numOfOverlap / lenOfWords2;
    }


}
