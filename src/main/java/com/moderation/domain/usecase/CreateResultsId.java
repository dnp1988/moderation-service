package com.moderation.domain.usecase;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreateResultsId {

    public String create() {
        return UUID.randomUUID().toString();
    }
}
