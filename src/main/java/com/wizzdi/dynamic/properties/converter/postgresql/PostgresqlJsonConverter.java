package com.wizzdi.dynamic.properties.converter.postgresql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wizzdi.dynamic.properties.converter.JsonConverterImplementation;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.Map;

@Converter(autoApply = false)
@Component
public class PostgresqlJsonConverter implements JsonConverterImplementation, AttributeConverter<Map<String, Object>, Object> {

    private static final long serialVersionUID = 1L;
    private static ObjectMapper mapper = new ObjectMapper();
    private static TypeReference<Map<String, Object>> type= new TypeReference<>() {};

    @Override
    public Object convertToDatabaseColumn(Map<String, Object> objectValue) {
        try {
            PGobject out = new PGobject();
            out.setType("jsonb");
            String value = mapper.writeValueAsString(objectValue);
            out.setValue(value);
            return out;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to serialize to json field ", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(Object dataValue) {
        try {
            if (dataValue instanceof PGobject && ((PGobject) dataValue).getType().equals("jsonb")) {
                String value = ((PGobject) dataValue).getValue();
                return mapper.readValue(value,type);
            }
            return null;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to deserialize to json field ", e);
        }
    }
}