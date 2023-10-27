package com.stemproject.stem;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
@Getter
@Setter
public class InputValues {

  public InputValues() {
    String fileName = "input.txt";
    double[][] arrays = new double[5][];

    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String line;
      int i = 0;
      while ((line = reader.readLine()) != null && i < arrays.length) {
        String[] values = line.split(",");
        arrays[i] = new double[values.length];
        for (int j = 0; j < values.length; j++) {
          arrays[i][j] = Integer.parseInt(values[j]);
        }
        i++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    this.setYd(new HashMap<>() {{
      put("Z", arrays[0]);
      put("B", arrays[1]);
      put("K", arrays[2]);
    }});
    this.setRowsMax(arrays[3]);
    this.setColsMax(arrays[4]);

  }
    Map<String, double[]> Yd = new HashMap<>();
    private double[] rowsMax = new double[3];
    private double[] colsMax = new double[4];

  /*  Map<String, double[]> Yd = new HashMap<>() {{
          put("Z", new double[] {1,4,1,9,9,2,2,8,6,1,7,3});
          put("B", new double[] {2, 8, 2, 18, 18, 4, 4, 16, 12, 2, 14, 6 });
          put("K", new double[] {18,10,18,1,1,14,14,5,8,18,6,12});
      }};

      private double[] rowsMax = new double[] {62,28,30};
      private double[] colsMax = new double[] {11,25,31,33};
  */

}
