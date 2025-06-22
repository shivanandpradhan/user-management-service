package com.snp.dev.user_management_service.serializers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.snp.dev.user_management_service.dto.EmailMessage;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class EmailMessageSerializer implements Serializer<EmailMessage> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No configuration needed
    }

    @Override
    public byte[] serialize(String topic, EmailMessage message) {
        try {
            if (message == null) {
                return null;
            }
            return objectMapper.writeValueAsBytes(message);
        } catch (Exception e) {
            throw new SerializationException("Error serializing EmailMessage", e);
        }
    }

    @Override
    public void close() {
        // Nothing to close
    }
}