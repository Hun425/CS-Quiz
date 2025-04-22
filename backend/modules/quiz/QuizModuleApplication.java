package com.quizplatform.quiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableJpaAuditing
@EnableKafka
@EntityScan(basePackages = {"com.quizplatform.quiz.adapter.out.persistence.entity"})
@EnableJpaRepositories(basePackages = {"com.quizplatform.quiz.adapter.out.persistence"})
public class QuizModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizModuleApplication.class, args);
    }
}