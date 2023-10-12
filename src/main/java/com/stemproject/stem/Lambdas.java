package com.stemproject.stem;

import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.ArrayList;
import java.util.Collection;

public class Lambdas {

    private final double[][] normMatrix;

    public Lambdas(double[][] normMatrix) {
        this.normMatrix = normMatrix;
    }

    public double[] getAlphas(){
        double[] alphas = new double[]{0,0,0};

        for(int i=0; i<3;i++){
            for(int j=0; j<3;j++) {

                if(i!=j) alphas[i]+=normMatrix[i][j]+normMatrix[j][i];
            }
            alphas[i]/=alphas.length+1;
        }
        return alphas;
    }

    public double[][] getCoefs(){
        double[] alphas = getAlphas();
        double[][] coefs = new double[3][3];

        coefs[0][0] = 1 - alphas[1];
        coefs[0][1] = -1 + alphas[0];
        coefs[0][2] = 0;
        coefs[1][0] = 1 - alphas[2];
        coefs[1][1] = 0;
        coefs[1][2] = coefs[0][1];
        coefs[2][0] = 0;
        coefs[2][1] = coefs[1][0];
        coefs[2][2] = (-1) * coefs[0][0];
        return coefs;
    }

    public double[] getLambdas(){
        double[][] coefs = getCoefs();

        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1, 1}, 0);
        Collection<LinearConstraint> constraints = new
                ArrayList<>();
        constraints.add(new LinearConstraint(coefs[0],
                Relationship.EQ, 0));
        constraints.add(new LinearConstraint(coefs[1],
                Relationship.EQ, 0));
        constraints.add(new LinearConstraint(coefs[2],
                Relationship.EQ, 0));
        constraints.add(new LinearConstraint(new double[]{1, 1, 1},
                Relationship.EQ, 1));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair optSolution = solver.optimize(new MaxIter(10000), f, new
                        LinearConstraintSet(constraints),
                GoalType.MAXIMIZE, new
                        NonNegativeConstraint(true));

        return optSolution.getPoint();
    }
}
