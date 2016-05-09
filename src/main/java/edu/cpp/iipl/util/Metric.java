package edu.cpp.iipl.util;

import java.util.List;

/**
 * Created by xing on 4/27/16.
 */
public class Metric {

    public static double meanSquaredError(List<Double> labels, List<Double> predicts) {
        if (labels == null || predicts == null
        || predicts.size() != labels.size() || predicts.size() == 0)
            return -1;

        double mse = 0;

        for (int i = 0; i < labels.size(); ++i)
            mse += Math.pow(labels.get(i) - predicts.get(i), 2);
        mse /= labels.size();

        return mse;
    }

    public static double meanSquaredError(double[] labels, double[] predicts) {
        if (labels == null || predicts == null
        || predicts.length != labels.length || predicts.length == 0)
            return -1;

        double mse = 0;

        for (int i = 0; i < labels.length; ++i)
            mse += Math.pow(labels[i] - predicts[i], 2);
        mse /= labels.length;

        return mse;
    }

    public static double quadraticWeightedKappa(List<Integer> labels, List<Integer> predicts) {
        if (predicts.size() != labels.size() || predicts.size() == 0)
            return -1;

        int min = Math.min(minOfAll(labels), minOfAll(predicts));
        int max = Math.max(maxOfAll(labels), maxOfAll(predicts));

        int[][] cm = confusionMatrix(labels, predicts, min, max);

        int[] histogramLabel = histogram(labels, min, max);
        int[] histogramPredicts = histogram(predicts, min, max);

        int numOfLabels = max - min + 1;
        int numOfItems = labels.size();

        double numerator = 0;
        double denominator = 0;

        for (int i = 0; i < numOfLabels; ++i)
            for (int j = 0; j < numOfLabels; ++j) {
                double expected = histogramLabel[i] * histogramPredicts[j] / (double)numOfItems;
                double d = Math.pow(i - j, 2) / Math.pow(numOfLabels - 1, 2.0);
                numerator += d * cm[i][j] / numOfItems;
                denominator += d * expected / numOfItems;
            }

        return 1.0 - numerator / denominator;
    }

    public static int[][] confusionMatrix(List<Integer> labels, List<Integer> predicts, int min, int max) {
        int numOfLabels = max - min + 1;
        int[][] cm = new int[numOfLabels][numOfLabels];

        for (int i = 0; i < labels.size(); ++i)
            cm[labels.get(i) - min][predicts.get(i) - min] += 1;

        return cm;
    }

    public static int[] histogram(List<Integer> labels, int min, int max) {
        int numOfLabels = max - min + 1;
        int[] histogram = new int[numOfLabels];

        for (int label : labels)
            histogram[label - min] += 1;

        return histogram;
    }

    private static int minOfAll(List<Integer> nums) {
        int min = Integer.MAX_VALUE;

        if (nums == null || nums.size() == 0)
            return min;

        for (int num : nums)
            if (min > num)
                min = num;

        return min;
    }

    private static int maxOfAll(List<Integer> nums) {
        int max = Integer.MIN_VALUE;

        if (nums == null || nums.size() == 0)
            return max;

        for (int num : nums)
            if (max < num)
                max = num;

        return max;
    }
}
