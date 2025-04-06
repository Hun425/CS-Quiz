package com.quizplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 스케줄러 활성화
public class QuizPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuizPlatformApplication.class, args);
	}

}
