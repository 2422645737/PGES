package org.example.pges;

import org.apdplat.word.WordSegmenter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class PgesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PgesApplication.class, args);
    }

}