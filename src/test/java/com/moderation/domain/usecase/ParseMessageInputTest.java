package com.moderation.domain.usecase;

import com.google.common.base.VerifyException;
import com.moderation.domain.entity.MessageInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class ParseMessageInputTest {

    private ParseMessageInput useCase = new ParseMessageInput();

    @Test
    public void testParseMessageInput() {
        String userId = "1";
        String messageText = "Message";
        String messageInputLine = String.format("%s,%s", userId, messageText);

        MessageInput messageInput = useCase.parse(messageInputLine);

        assertEquals(messageText, messageInput.getMessageText());
        assertEquals(userId, messageInput.getUserId());
    }

    @ParameterizedTest
    @MethodSource("testParseMessageInputErrorParams")
    public void testParseMessageInputError(String messageInputLine) {
        assertThrowsExactly(VerifyException.class, () -> useCase.parse(messageInputLine));
    }

    private static Stream<String> testParseMessageInputErrorParams() {
        return Stream.of(
                null,
                "1,Message,ExtraValue",
                ",Message",
                "1,"
        );
    }

}
