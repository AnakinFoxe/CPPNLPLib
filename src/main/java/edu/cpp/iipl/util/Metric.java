package edu.cpp.iipl.util;

import java.util.List;

/**
 * Created by xing on 4/27/16.
 */
public class Metric {

    public static double MeanSquaredError(List<Double> labels, List<Double> predicts) {
        if (labels == null || predicts == null
        || predicts.size() != labels.size() || predicts.size() == 0)
            return -1;

        double mse = 0;

        for (int i = 0; i < labels.size(); ++i)
            mse += Math.pow(labels.get(i) - predicts.get(i), 2);
        mse /= labels.size();

        return mse;
    }

    public static double MeanSquaredError(double[] labels, double[] predicts) {
        if (labels == null || predicts == null
        || predicts.length != labels.length || predicts.length == 0)
            return -1;

        double mse = 0;

        for (int i = 0; i < labels.length; ++i)
            mse += Math.pow(labels[i] - predicts[i], 2);
        mse /= labels.length;

        return mse;
    }

    public static double QuadraticWeightedKappa(List<Double> labels, List<Double> predicts) {
        if (predicts.size() != labels.size())
            return -1;

        double kappa = 0;

        double min = Math.min(minOfAll(labels), minOfAll(predicts));
        double max = Math.max(maxOfAll(labels), maxOfAll(predicts));


        return kappa;
    }

    public static double[][] ConfusionMatrix(List<Double> labels, List<Double> predicts, double min, double max) {
        int numOfLabels = (int)(max - min + 1);
        double[][] cm = new double[numOfLabels][numOfLabels];

        for (Double label : labels) {
            for (Double predict : predicts) {

            }
        }

        return cm;
    }


    private static double minOfAll(List<Double> nums) {
        double min = Double.MIN_VALUE;

        if (nums == null || nums.size() == 0)
            return min;

        for (Double num : nums)
            if (min > num)
                min = num;

        return min;
    }

    private static double maxOfAll(List<Double> nums) {
        double max = Double.MAX_VALUE;

        if (nums == null || nums.size() == 0)
            return max;

        for (Double num : nums)
            if (max < num)
                max = num;

        return max;
    }
}
