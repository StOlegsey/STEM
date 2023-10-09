package com.stemproject.stem;

import lombok.Getter;

import java.util.*;
@Getter
public class InputValues {
    Map<String, double[]> Yd = new HashMap<>() {{
        put("Z", new double[] { 1,4,1,9,9,2,2,8,6,1,7,3});
        put("B", new double[] { 2, 8, 2, 18, 18, 4, 4, 16, 12, 2, 14, 6 });
        put("K", new double[] {18,10,18,1,1,14,14,5,8,18,6,12});
    }};

    private double[] rowsMax = new double[] {62,28,30};
    private double[] colsMax = new double[] {11,25,31,33};


}
