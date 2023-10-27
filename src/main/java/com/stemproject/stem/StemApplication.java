package com.stemproject.stem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

@SpringBootApplication
public class StemApplication {

    public static void main(String[] args) {
        SpringApplication.run(StemApplication.class, args);

        System.out.println("Ввод в input.txt (значения через запятую)" +
                "\n1-я строка - удельные затраты" +
                "\n2-я строка - удельная безопасность" +
                "\n3-я строка - удельная комфортабельность" +
                "\n4-я строка - ограничения по строкам" +
                "\n5-я строка - ограничения по столбцам" +
                "\nВывод в STEM.txt");

        Iteration iteration = new Iteration(15);
        iteration.IterationSolvation();

    }
}
