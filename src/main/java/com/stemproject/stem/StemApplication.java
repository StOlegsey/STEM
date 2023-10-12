package com.stemproject.stem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StemApplication {

    public static void main(String[] args) {
        SpringApplication.run(StemApplication.class, args);

        Iteration iteration = new Iteration(10);
        iteration.IterationSolvation();


    }



}
