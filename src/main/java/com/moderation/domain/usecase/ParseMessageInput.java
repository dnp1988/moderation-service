package com.moderation.domain.usecase;

import com.google.common.base.Verify;
import com.moderation.domain.entity.MessageInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ParseMessageInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseMessageInput.class);

    public MessageInput parse(String messageInputLine) {
        LOGGER.debug("Parsing messageInputLine ({})", messageInputLine);

        Verify.verifyNotNull(messageInputLine, "messageInputLine cannot be null");

        String[] values = messageInputLine.split(",");

        Verify.verify(values.length == 2, "messageInputLine (%s) is malformed", messageInputLine);

        String userId = values[0].trim();

        Verify.verify(!userId.isBlank(), "userId in line (%s) is blank", messageInputLine);

        String messageText = values[1].trim();

        Verify.verify(!messageText.isBlank(), "messageText in line (%s) is blank", messageInputLine);

        return new MessageInput(userId, messageText);
    }
}
