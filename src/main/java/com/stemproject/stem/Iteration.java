package com.stemproject.stem;

import jdk.jfr.Description;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Iteration {

    public Iteration(Integer maxIterations) {
        this.maxIterations = maxIterations;
    }

    private Iteration(Integer maxIterations, Integer iterationNumber, String P_Past, double P_AvgPast, double[][] minMaxValues) {
        this.maxIterations = maxIterations;
        this.iterationNumber = iterationNumber;
        this.P_Past = P_Past;
        this.P_AvgPast = P_AvgPast;
        this.minMaxValues = minMaxValues;
    }

    final Integer maxIterations;
    Integer iterationNumber = 1;
    final Integer MatrixSize = 12;
    String P_Past = null;
    double P_AvgPast = 0.0;
    double[][] minMaxValues = new double[3][3];
    InputValues inputValues = new InputValues();
    //Z - Затраты, B - безопасность, K - комфортабельность
    Map<String, double[]> Yd = inputValues.getYd();
    double[] rowMax = inputValues.getRowsMax();
    double[] colsMax = inputValues.getColsMax();
    double[][] optMatrix = new double[3][3];
@Description("Функция возвращает значение критерия по данным переменным")
    public Double getPMaxByVars(String P, double[] Vars)
    {
        double PMax = 0.0;

        double[] YdChosen = Yd.get(P);

        for(int i = 0; i< MatrixSize; i++)
        {
            PMax += YdChosen[i]*Vars[i];
        }
        return PMax;
    }

    @Description("Если ищем главный критерий, то значение и мин/макс необязательны")
    public double[] getAllColumnForP(String P, GoalType MinMax, boolean mainCriteria)
    {
        double[] Pcol = new double[4];

        double[] function = new double[12];

        GoalType goalType = GoalType.MAXIMIZE;

        if(mainCriteria){
            Lambdas lambdas = new Lambdas(getNormalizedMatrix());
            double[] lambda = lambdas.getLambdas();
            // если ищем главный критерий, то функция - это = PZ* L1 + PB* L2 + PK* L3 -> max
            for(int i = 0; i< 12; i++){
                function[i]=Yd.get("Z")[i] * lambda[0] * (-1) + Yd.get("B")[i] * lambda[1] + Yd.get("K")[i] * lambda[2];
            }
        }
        else {
            function = Yd.get(P);
            //Если оптимизируем затраты, то функция - это модификаторы для критерия; -> min(), если максимизируем, то max()
            goalType  = (Objects.equals(P, "Z")) ? (MinMax == GoalType.MAXIMIZE) ? GoalType.MINIMIZE : GoalType.MAXIMIZE : MinMax;
        }

        //function - функция, которую надо оптимизировать (суммпроизв переменных и модификаторов критериев)
        LinearObjectiveFunction f = new LinearObjectiveFunction(function, 0);
        Collection<LinearConstraint> constraints = new
                ArrayList<>();
                //ограничения для суммы строк
        constraints.add(new LinearConstraint(new double[] { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                Relationship.LEQ, rowMax[0]));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },
                Relationship.LEQ, rowMax[1]));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
                Relationship.LEQ, rowMax[2]));
                //ограничения для суммы столбцов
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0 },
                Relationship.GEQ, colsMax[0]));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0 },
                Relationship.GEQ, colsMax[1]));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0 },
                Relationship.GEQ, colsMax[2]));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1 },
                Relationship.GEQ, colsMax[3]));
        if(iterationNumber!=1){ //если итерация не первая, добавляем условие на оптимальный критерий
            Relationship rel = Relationship.GEQ;
            if(P_AvgPast<0) rel = Relationship.LEQ;//если пороговое значение меньше нуля, то значение должно быть меньше
            constraints.add(new LinearConstraint(Yd.get(P_Past),
                        rel, Math.abs(P_AvgPast)));
        }

        SimplexSolver solver = new SimplexSolver();
        PointValuePair optSolution = solver.optimize(new MaxIter(10000), f, new
                        LinearConstraintSet(constraints), goalType, new
                        NonNegativeConstraint(true)); // ищем оптимальное значение, скармливаем ограничения и главную функцию
        double[] solution;
        solution = optSolution.getPoint();  //Получаем переменные, при которых выходит оптимальное значение

        //ищем по этим переменным значения остальных критериев
        if(mainCriteria){
            Pcol[0] = (-1) * getPMaxByVars("Z", solution);
            Pcol[1] = getPMaxByVars("B", solution);
            Pcol[2] = getPMaxByVars("K", solution);
            Pcol[3] = optSolution.getSecond();
        }

        else {
            switch (P) {
                case "Z" -> {
                    Pcol[0] = (-1) * optSolution.getSecond();
                    Pcol[1] = getPMaxByVars("B", solution);
                    Pcol[2] = getPMaxByVars("K", solution);
                }
                case "B" -> {
                    Pcol[0] = (-1) * getPMaxByVars("Z", solution);
                    Pcol[1] = optSolution.getSecond();
                    Pcol[2] = getPMaxByVars("K", solution);
                }
                case "K" -> {
                    Pcol[0] = (-1) * getPMaxByVars("Z", solution);
                    Pcol[1] = getPMaxByVars("B", solution);
                    Pcol[2] = optSolution.getSecond();
                }
            }
        }
        return Pcol;
    }
@Description("Получаем матрицу оптимизаций")
    public void getOptimizationMatrix(){

        this.optMatrix[0] = getAllColumnForP("Z", GoalType.MAXIMIZE, false);
        this.optMatrix[1] = getAllColumnForP("B", GoalType.MAXIMIZE, false);
        this.optMatrix[2] = getAllColumnForP("K", GoalType.MAXIMIZE, false);

    }
@Description("Получаем нормализированную матрицу")
    public double[][] getNormalizedMatrix(){

        getOptimizationMatrix();

        double[][] normMatrix = new double[3][3];
        double[] Pmin = new double[3];
        Pmin[0] = getAllColumnForP("Z", GoalType.MINIMIZE, false)[0];
        Pmin[1] = getAllColumnForP("B", GoalType.MINIMIZE, false)[1];
        Pmin[2] = getAllColumnForP("K", GoalType.MINIMIZE, false)[2];

        for(int i=0; i<3;i++){
            for(int j=0; j<3;j++){
                normMatrix[i][j] = (optMatrix[j][i] - Pmin[i]) / (optMatrix[i][i] - Pmin[i]);
            }
        }
        return normMatrix;
    }
@Description("Отчет об итерации")
    public double[] IterationSolvation(){

        double[] cols = getAllColumnForP("P", GoalType.MAXIMIZE, true);
        double[] maxVars = new double[3];

        for(int i =0;i<3;i++){
            if(iterationNumber==1) {    //убрать условие, чтобы диапазон для оптимального критерия находился исходя из этой итерации
                minMaxValues[i][0] = Collections.min(Arrays.asList(optMatrix[0][i], optMatrix[1][i], optMatrix[2][i]));
                minMaxValues[i][1] = Collections.max(Arrays.asList(optMatrix[0][i], optMatrix[1][i], optMatrix[2][i]));
            }
            maxVars[i] = minMaxValues[i][1] - cols[i];//удаленность от макимального значения до полученного

            if(i!=0){   //сравниваем относительно затрат
                maxVars[i] /= (minMaxValues[i][1]-minMaxValues[i][0])/(minMaxValues[0][1]-minMaxValues[0][0]);
            }
        }

        int max = 0;
        for (int i = 0; i < maxVars.length; i++) {
            max = maxVars[i] > maxVars[max] ? i : max;
        }

        //Если высчитывать пороговое значение исходя из промежутков, полученных после первой итерации:
        //double P_Avg = (cols[max]+minMaxValues[max][1])/2;
        String PofMax = max == 0 ? "Z": max == 1 ? "B": "K";
        boolean rewrite_to_file = iterationNumber != 1;
        try(FileWriter writer = new FileWriter("STEM.txt", rewrite_to_file))
        {
            writer.append("\nИтерация: "+ iterationNumber+" из "+maxIterations+"\n");
            writer.append("\nМатрица оптимизации\n");
            System.out.println("\nИтерация: "+ iterationNumber+" из "+maxIterations+"\n");
            System.out.println("Матрица оптимизации");

            for(int i =0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    System.out.print(String.format("%.4f",optMatrix[j][i])+" ");
                    writer.append(String.format("%.4f",optMatrix[j][i])+" ");
                }
                System.out.print("\n");
                writer.append("\n");
            }

            System.out.println("\nЗатраты: " + String.format("%.4f",cols[0]));
            System.out.println("Безопасность: " + String.format("%.4f",cols[1]));
            System.out.println("Комфортабельность: " + String.format("%.4f",cols[2]));
            System.out.println("Главный критерий: " + String.format("%.4f",cols[3]));

            writer.append("\nЗатраты: " + String.format("%.4f",cols[0])+"\n");
            writer.append("Безопасность: "+String.format("%.4f", cols[1])+"\n");
            writer.append("Комфортабельность: " + String.format("%.4f",cols[2])+"\n");
            writer.append("Главный критерий: " + String.format("%.4f",cols[3])+"\n");
            writer.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }


        //Если считывать из консоли пороговое значение:
        Scanner in = new Scanner(System.in);
        System.out.println("Это последняя итерация?(Y/N)");
        boolean last_iter = in.next().matches("[Yy]");

        if(!last_iter) {

            System.out.println("Какой критерий оптимизируем?(Z/B/K)");
            PofMax = in.next().toUpperCase();

            System.out.print("Введите пороговое значение (дроби через запятую): ");
            double P_Avg = in.nextDouble();
            //---------------------------------------------------

            System.out.println("Критерий для оптимизации: " + PofMax + " Пороговое значение: " + P_Avg);
            try(FileWriter writer = new FileWriter("STEM.txt", true))
            {
                writer.append("Критерий для оптимизации: " + PofMax + " Пороговое значение: " + P_Avg+"\n");
            }
            catch(IOException ex){
                System.out.println(ex.getMessage());
            }

            if (!iterationNumber.equals(maxIterations)) {
                Iteration iteration = new Iteration(maxIterations, iterationNumber + 1, PofMax, P_Avg, minMaxValues);
                iteration.IterationSolvation();
            }
        }
        in.close();
        return cols;

    }
}
