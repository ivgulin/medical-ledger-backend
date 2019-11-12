package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.government.document.Document;
import com.mokujin.ssi.service.CredentialService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CredentialServiceImpl implements CredentialService {

    private final ObjectMapper objectMapper;

    @Override
    public String getCredential(Document document) {
        List<Field> fields = Arrays.stream(document.getClass().getDeclaredFields()).collect(Collectors.toList());

        ObjectNode credentialNode = objectMapper.createObjectNode();

        fields.forEach(f -> {
            f.setAccessible(true);
            try {
                ObjectNode attribute = objectMapper.createObjectNode();
                attribute.put("raw", f.get(document).toString());
                attribute.put("encoded", String.valueOf(Math.abs(new Random().nextLong())));
                credentialNode.set(f.getName().toLowerCase(), attribute);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        return credentialNode.toString();
    }
}
