package pokemon.runs.time.leaderboard.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        List<String> normalizedValues = list.stream()
                .filter(value -> value != null)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();

        return normalizedValues.isEmpty() ? null : String.join(",", normalizedValues);
    }

    @Override
    public List<String> convertToEntityAttribute(String joined) {
        if (joined == null || joined.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(Arrays.stream(joined.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList());
    }
}
