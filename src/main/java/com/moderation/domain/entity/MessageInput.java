package com.moderation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class MessageInput {

    private String userId;

    private String messageText;
}
