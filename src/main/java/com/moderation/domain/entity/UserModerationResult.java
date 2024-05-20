package com.moderation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserModerationResult {

    private String userId;

    private Long totalMessages;

    private Double avgScore;
}
