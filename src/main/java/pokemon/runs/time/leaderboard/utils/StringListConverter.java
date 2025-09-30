package pokemon.runs.time.leaderboard.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        return list == null || list.isEmpty() ? null : String.join(",", list);
    }

    @Override
    public List<String> convertToEntityAttribute(String joined) {
        if (joined == null || joined.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(joined.split(",")));
    }
}
