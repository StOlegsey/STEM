package com.stemproject.stem;

import jdk.jfr.Description;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.*;

public class Iteration {

    public Iteration(Integer maxIterations) {
        this.maxIterations = maxIterations;
    }

    public Iteration(Integer maxIterations, Integer iterationNumber, String P_Past, double P_AvgPast) {
        this.maxIterations = maxIterations;
        this.iterationNumber = iterationNumber;
        this.P_Past = P_Past;
        this.P_AvgPast = P_AvgPast;
    }

    final Integer maxIterations;
    Integer iterationNumber = 1;
    final Integer MatrixSize = 12;
     String P_Past = null;
     double P_AvgPast = 0.0;
    InputValues inputValues = new InputValues();
    //Z - Затраты, B - безопасность, K - комфортабельность
    Map<String, double[]> Yd = inputValues.getYd();
    double[] rowMax = inputValues.getRowsMax();
    double[] colsMax = inputValues.getColsMax();
    double[][] optMatrix = new double[3][3];

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

    @Description("If mainCriteria then P and GoalType are unnecessary")
    public double[] getAllColumnForP(String P, GoalType MinMax, boolean mainCriteria)
    {
        double[] Pcol = new double[4];

        double[] function = new double[12];

        GoalType goalType = GoalType.MAXIMIZE;

        if(mainCriteria){
            Lambdas lambdas = new Lambdas(getNormalizedMatrix());
            double[] lambda = lambdas.getLambdas();

            for(int i = 0; i< 12; i++){
                function[i]=Yd.get("Z")[i] * lambda[0] * (-1) + Yd.get("B")[i] * lambda[1] + Yd.get("K")[i] * lambda[2];
            }
        }
        else {
            function = Yd.get(P);
            //Если оптимизируем затраты, то функция -> min(), если максимизируем, то max()
            goalType  = (Objects.equals(P, "Z")) ? (MinMax == GoalType.MAXIMIZE) ? GoalType.MINIMIZE : GoalType.MAXIMIZE : MinMax;
        }

        LinearObjectiveFunction f = new LinearObjectiveFunction(function, 0);
        Collection<LinearConstraint> constraints = new
                ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                Relationship.LEQ, rowMax[0]));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },
                Relationship.LEQ, rowMax[1]));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
                Relationship.LEQ, rowMax[2]));

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
            if(P_AvgPast<0) rel = Relationship.LEQ;

                constraints.add(new LinearConstraint(Yd.get(P_Past),
                        rel, Math.abs(P_AvgPast)));
        }

        SimplexSolver solver = new SimplexSolver();
        PointValuePair optSolution = solver.optimize(new MaxIter(10000), f, new
                        LinearConstraintSet(constraints),
                goalType, new
                        NonNegativeConstraint(true));
        double[] solution;
        solution = optSolution.getPoint();



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

        /*if(iterationNumber==2){
            System.out.println(P);
            for(int i =0;i<12;i++){
                System.out.print(solution[i]+" ");
            }
            System.out.println("\n");
            System.out.println(Pcol[0]);
            System.out.println(Pcol[1]);
            System.out.println(Pcol[2]);
        }*/

        return Pcol;
    }

    public void getOptimizationMatrix(){

        this.optMatrix[0] = getAllColumnForP("Z", GoalType.MAXIMIZE, false);
        this.optMatrix[1] = getAllColumnForP("B", GoalType.MAXIMIZE, false);
        this.optMatrix[2] = getAllColumnForP("K", GoalType.MAXIMIZE, false);

    }

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

    public double[] IterationSolvation(){

        double[] cols = getAllColumnForP("P", GoalType.MAXIMIZE, true);
        double[][] minMax = new double[3][2];
        double[] maxVars = new double[3];

        for(int i =0;i<3;i++){
            minMax[i][0] = Collections.min(Arrays.asList(optMatrix[0][i],optMatrix[1][i],optMatrix[2][i]));
            minMax[i][1] = Collections.max(Arrays.asList(optMatrix[0][i],optMatrix[1][i],optMatrix[2][i]));
            maxVars[i] = minMax[i][1] - cols[i];//удаленность от макимального значения до полученного
            //System.out.println(minMax[i][0]+" - "+minMax[i][1]);
            if(i!=0){   //сравниваем относительно затрат
                maxVars[i] /= (minMax[i][1]-minMax[i][0])/(minMax[0][1]-minMax[0][0]);
            }
        }

        int max = 0;
        for (int i = 0; i < maxVars.length; i++) {
            max = maxVars[i] > maxVars[max] ? i : max;
        }

        String PofMax = max == 0 ? "Z": max == 1 ? "B": "K";
        double P_Avg = (cols[max]+minMax[max][1])/2;

        System.out.println("\nИтерация: "+ iterationNumber+" из "+maxIterations+"\n");

        System.out.println("Матрица оптимизации");
        for(int i =0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(optMatrix[j][i]+" ");
            }
            System.out.print("\n");
        }

        System.out.println("\nЗатраты: " + cols[0]);
        System.out.println("Безопасность: " + cols[1]);
        System.out.println("Комфортабельность: " + cols[2]);
        System.out.println("Главный критерий: " + cols[3]);
        System.out.println("Критерий для оптимизации: " + PofMax+" Пороговое значение: "+P_Avg);

        if(!iterationNumber.equals(maxIterations)) {
            Iteration iteration = new Iteration(maxIterations, iterationNumber+1,PofMax,P_Avg);
            iteration.IterationSolvation();
        }

        return cols;

    }
}
