package com.stemproject.stem;

import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.*;

public class Iteration {

    static Integer MatrixSize = 12;
    InputValues inputValues = new InputValues();
    //Z - Затраты, B - безопасность, K - комфортабельность
    Map<String, double[]> Yd = inputValues.getYd();
    double[] rowMax = inputValues.getRowsMax();
    double[] colsMax = inputValues.getColsMax();


    public Double getPMaxByVars(String P, double[] Vars)
    {
        Double PMax = 0.0;

        double[] YdChosen = Yd.get(P);

        for(int i = 0; i< MatrixSize; i++)
        {
            PMax += YdChosen[i]*Vars[i];
        }
        return PMax;
    }

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
            goalType  = (P == "Z") ? (MinMax == GoalType.MAXIMIZE) ? GoalType.MINIMIZE : GoalType.MAXIMIZE : MinMax;
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
                case "Z":
                    Pcol[0] = (-1) * optSolution.getSecond();
                    Pcol[1] = getPMaxByVars("B", solution);
                    Pcol[2] = getPMaxByVars("K", solution);
                    break;
                case "B":
                    Pcol[0] = (-1) * getPMaxByVars("Z", solution);
                    Pcol[1] = optSolution.getSecond();
                    Pcol[2] = getPMaxByVars("K", solution);
                    break;
                case "K":
                    Pcol[0] = (-1) * getPMaxByVars("Z", solution);
                    Pcol[1] = getPMaxByVars("B", solution);
                    Pcol[2] = optSolution.getSecond();
                    break;
            }
        }

        return Pcol;
    }

    public double[][] getOptimizationMatrix(){

        double[][] optMatrix = new double[3][3];

        optMatrix[0] = getAllColumnForP("Z", GoalType.MAXIMIZE, false);
        optMatrix[1] = getAllColumnForP("B", GoalType.MAXIMIZE, false);
        optMatrix[2] = getAllColumnForP("K", GoalType.MAXIMIZE, false);

        return optMatrix;
    }

    public double[][] getNormalizedMatrix(){

        double[][] optMatrix = getOptimizationMatrix();
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

       return getAllColumnForP("P", GoalType.MAXIMIZE, true);

    }
}
