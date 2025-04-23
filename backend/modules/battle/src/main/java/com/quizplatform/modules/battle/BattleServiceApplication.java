package com.quizplatform.modules.battle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan("com.quizplatform.modules.battle.domain.entity")
@EnableJpaRepositories("com.quizplatform.modules.battle.infrastructure.repository")
@ComponentScan(basePackages = {
    "com.quizplatform.modules.battle", 
    "com.quizplatform.modules.common.config"
})
public class BattleServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BattleServiceApplication.class, args);
    }
}
