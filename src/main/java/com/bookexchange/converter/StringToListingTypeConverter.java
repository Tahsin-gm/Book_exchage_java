package com.bookexchange.converter;

import com.bookexchange.entity.ListingType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToListingTypeConverter implements Converter<String, ListingType> {

    @Override
    public ListingType convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        try {
            return ListingType.valueOf(source.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid listing type: " + source);
        }
    }
}
