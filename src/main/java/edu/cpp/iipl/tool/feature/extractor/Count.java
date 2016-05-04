package edu.cpp.iipl.tool.feature.extractor;

import java.util.List;

/**
 * Created by xing on 4/22/16.
 */
public class Count {

    public static int numOfWords(String[] words) {
        if (words != null)
            return words.length;
        else
            return 0;
    }

    public static int numOfWords(List<String> words) {
        if (words != null)
            return words.size();
        else
            return 0;
    }
}
