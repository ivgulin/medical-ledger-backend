package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.government.document.Document;
import com.mokujin.ssi.model.government.document.NationalDocument;
import com.mokujin.ssi.service.CredentialService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@AllArgsConstructor
public class CredentialServiceImpl implements CredentialService {

    private final ObjectMapper objectMapper;

    @Override
    public String getCredential(Document document) {
        List<Field> fields = Arrays.stream(document.getClass().getDeclaredFields()).collect(Collectors.toList());

        Class<?> superclass = document.getClass().getSuperclass() == NationalDocument.class
                ? document.getClass().getSuperclass().getSuperclass()
                : document.getClass().getSuperclass();

        fields.addAll(Arrays.stream(superclass.getDeclaredFields()).collect(Collectors.toList()));

        ObjectNode credentialNode = objectMapper.createObjectNode();

        fields.forEach(f -> {
            f.setAccessible(true);
            try {
                ObjectNode attribute = objectMapper.createObjectNode();
                Object value = f.get(document);
                attribute.put("raw", value.toString());
                attribute.put("encoded", String.valueOf(Math.abs(new Random().nextLong())));
                credentialNode.set(f.getName(), attribute);
            } catch (Exception e) {
                log.error("Exception was thrown: " + e);
                throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
            }
        });

        return credentialNode.toString();
    }
}
