package com.stemproject.stem;

import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Iteration {

    InputValues inputValues = new InputValues();

    List<List<Integer>> YdZ = inputValues.mainMatrix("YdZ");
    List<List<Integer>> YdB = inputValues.mainMatrix("YdB");
    List<List<Integer>> YdK = inputValues.mainMatrix("YdK");
    List<List<Integer>> Variables =
            IntStream.range(0, 3)
                    .mapToObj(i -> new ArrayList<Integer>() {
                        {
                            add(0);
                            add(0);
                            add(0);
                            add(0);
                        }})
                    .collect(Collectors.toList());

    public Integer getPMax(String P)
    {
        Integer PMax;
        List<List<Integer>> YdChosen = (P == "PZ") ? YdZ : ((P == "PB") ? YdB : (P == "PK") ? YdK : null);
        Integer MatrixSize = 12;

        PMax  = IntStream.range(0, MatrixSize)
                .map(i -> {
                    Integer v1 = Variables.stream().flatMap(a -> a.stream()).collect(Collectors.toCollection(ArrayList::new)).get(i);
                    Integer v2 = YdChosen.stream().flatMap(a -> a.stream()).collect(Collectors.toCollection(ArrayList::new)).get(i);
                    return v1*v2;
                })
                .sum();
        return PMax;
    }

    public ArrayList<Integer> OptimizationMatrixColumn(){

        Integer PmaxZ = getPMax("PZ");
        Integer PmaxB = getPMax("PB");
        Integer PmaxK = getPMax("PK");

        ArrayList<Integer> Pmax = new ArrayList<>();

        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                for(int y = (inputValues.getRowsMax().get(i) < inputValues.getColsMax().get(j) ?
                        inputValues.getRowsMax().get(i) : inputValues.getColsMax().get(j)); y>=0; y--)
                {
                    Variables.get(i).set(j, y);
                    Integer Pcurr = getPMax("PB");

                    System.out.println("i: "+i+" j: "+j+" = "+Pcurr);
                    System.out.println(Variables);
                    if(Pcurr > PmaxB) {
                        PmaxB = Pcurr;
                        PmaxK = getPMax("PK");
                        PmaxZ = getPMax("PZ");
                    }
                }
            }
        }

        Pmax.add(PmaxZ * (-1));
        Pmax.add(PmaxB);
        Pmax.add(PmaxK);

        return Pmax;
    }
    Integer stageMain = 0;
    public void generateCombinations(int row, int col, int stage) throws Exception {
        //if(stageMain>1000000) {throw new Exception("1000");}
        if (row == 3) {
            System.out.println("базовый случай: достигнут конец массива");
            // здесь можно обработать полученную комбинацию
            return;
        }
        // перебираем все возможные значения для текущей ячейки
        for (int i = 0; i <= (inputValues.getRowsMax().get(row) < inputValues.getColsMax().get(col) ?
                inputValues.getRowsMax().get(row) : inputValues.getColsMax().get(col)); i++) {
            Variables.get(row).set(col, i);
            stage++;
            stageMain++;
            System.out.println("row: "+ row + " col: "+ col + " stage: "+ stageMain);
            System.out.println(Variables.get(0));
            System.out.println(Variables.get(1));
            System.out.println(Variables.get(2));
            if (col == 3) {
                System.out.println("если достигнут конец строки, переходим на следующую строку");
                generateCombinations(row + 1, 0, stage);
            } else {
                System.out.println("иначе переходим на следующий столбец");
                generateCombinations( row, col + 1, stage);
            }
        }
    }


}
