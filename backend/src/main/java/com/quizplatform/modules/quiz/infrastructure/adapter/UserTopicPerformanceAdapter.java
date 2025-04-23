package com.quizplatform.modules.quiz.infrastructure.adapter;

import com.quizplatform.modules.quiz.infrastructure.repository.TagRepository;
import com.quizplatform.modules.user.application.port.out.TopicPerformanceData;
import com.quizplatform.modules.user.application.port.out.UserTopicPerformancePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserTopicPerformanceAdapter implements UserTopicPerformancePort {

    private final TagRepository tagRepository;

    @Override
    public List<TopicPerformanceData> getTopicPerformanceData(Long userId) {
        // Fetch raw performance data using the repository method
        List<Object[]> tagPerformances = tagRepository.getTagPerformanceByUserId(userId);

        // Map the Object[] array to TopicPerformanceData DTOs
        List<TopicPerformanceData> result = new ArrayList<>();
        for (Object[] row : tagPerformances) {
            // Extract and convert data carefully, handling potential nulls
            Long tagId = ((Number) row[0]).longValue();
            String tagName = (String) row[1];
            Long quizzesTaken = ((Number) row[2]).longValue();
            Double averageScore = row[3] != null ? ((Number) row[3]).doubleValue() : null; // Keep null if DB returns null
            Double correctRate = row[4] != null ? ((Number) row[4]).doubleValue() : null;   // Keep null if DB returns null

            result.add(new TopicPerformanceData(
                    tagId,
                    tagName,
                    quizzesTaken,
                    averageScore,
                    correctRate
            ));
        }
        return result;
    }
} 