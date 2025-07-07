package com.quizplatform.core.dto.question;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OptionDto {
    private String key;
    private String value;
}