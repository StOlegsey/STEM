package com.stemproject.stem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StemApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(StemApplication.class, args);

        Iteration iteration = new Iteration();
        double[] firstIter = iteration.IterationSolvation();

        System.out.println("Затраты: " + firstIter[0]);
        System.out.println("Безопасность: " + firstIter[1]);
        System.out.println("Комфортабельность: " + firstIter[2]);
        System.out.println("Гланый критерий: " + firstIter[3]);

    }



}
