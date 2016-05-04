package edu.cpp.iipl.tool.feature;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 4/22/16.
 */
public class Scaling {

    public static List<List<Double>> scaleFeatures(List<List<Double>> features) {
        List<List<Double>> scaledFeatures = new ArrayList<>();

        if (features == null || features.size() == 0)
            return scaledFeatures;

        // calculate mean
        double[] mean = new double[features.get(0).size()];
        for (List<Double> feature : features) {
            for (int i = 0; i < mean.length; ++i)
                mean[i] += feature.get(i);
        }
        for (int i = 0; i < mean.length; ++i)
            mean[i] /= features.size();

        // calculate standard deviation
        double[] sd = new double[mean.length];
        for (List<Double> feature : features) {
            for (int i = 0; i < feature.size(); ++i) {
                double dev = feature.get(i) - mean[i];
                sd[i] += dev * dev;
            }
        }
        for (int i = 0; i < sd.length; ++i)
            sd[i] = Math.sqrt(sd[i] / features.size());

        // standardization
        for (List<Double> feature : features) {
            List<Double> scaledFeature = new ArrayList<>();

            for (int i = 0; i < feature.size(); ++i) {
                double numerator = feature.get(i) - mean[i];
                double denominator = sd[i];

                if (denominator != 0)
                    scaledFeature.add(numerator / denominator);
                else
                    scaledFeature.add(0.0);
            }

            scaledFeatures.add(scaledFeature);
        }

        return scaledFeatures;
    }
}
