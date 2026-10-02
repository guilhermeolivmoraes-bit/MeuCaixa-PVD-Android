package com.oliveira.meucaixa.model.repository;

import androidx.room.TypeConverter;
import java.math.BigDecimal;

public class Converters {
    @TypeConverter
    public static BigDecimal fromString(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    @TypeConverter
    public static String toString(BigDecimal value) {
        return value == null ? null : value.toString();
    }
}
