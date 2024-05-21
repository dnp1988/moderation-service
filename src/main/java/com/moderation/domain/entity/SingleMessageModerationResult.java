package com.moderation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class SingleMessageModerationResult {

    private String userId;

    private String messageText;

    private Double score;

    public SingleMessageModerationResult setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public SingleMessageModerationResult setMessageText(String messageText) {
        this.messageText = messageText;
        return this;
    }

    public SingleMessageModerationResult setScore(Double score) {
        this.score = score;
        return this;
    }
}
