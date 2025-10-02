package com.bookexchange.converter;

import com.bookexchange.entity.BookCondition;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToBookConditionConverter implements Converter<String, BookCondition> {

    @Override
    public BookCondition convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        try {
            return BookCondition.valueOf(source.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid book condition: " + source);
        }
    }
}
