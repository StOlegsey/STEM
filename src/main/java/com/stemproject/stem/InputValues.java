package com.stemproject.stem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class InputValues {

    private List<Integer> YdZ = Arrays.asList(1,4,1,9,9,2,2,8,6,1,7,3);
    private List<Integer> YdB = Arrays.asList(2,8,2,18,18,4,4,16,12,2,14,6);
    private List<Integer> YdK = Arrays.asList(18,10,18,1,1,14,14,5,8,18,6,12);
    private List<Integer> rowsMax = Arrays.asList(62,28,30);
    private List<Integer> colsMax = Arrays.asList(11,25,31,33);

    public List<List<Integer>> mainMatrix(String Yd){

        int rows = 3;
        List<Integer> YdChosen = (Yd == "YdZ") ? YdZ : ((Yd == "YdB") ? YdB : (Yd == "YdK") ? YdK : null);

        List<List<Integer>> matrix =
                IntStream.range(0, rows)
                        .mapToObj(i -> YdChosen.subList(i * (YdChosen.size()/rows), (i + 1) * (YdChosen.size()/rows)))
             .collect(Collectors.toList());
        return matrix;
    }

    public List<Integer> getRowsMax() {
        return rowsMax;
    }

    public List<Integer> getColsMax() {
        return colsMax;
    }
}
