package pokemon.runs.time.leaderboard.utils;

import java.time.Duration;

public final class RunTimeParser {

    private static final String RUN_TIME_PATTERN = "\\d{1,2}:[0-5]\\d";

    private RunTimeParser() {
    }

    public static Duration parse(String value) {
        if (value == null || !value.matches(RUN_TIME_PATTERN)) {
            throw new IllegalArgumentException("Run time deve estar no formato HH:MM, com minutos entre 00 e 59");
        }

        String[] parts = value.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return Duration.ofHours(hours).plusMinutes(minutes);
    }

    public static String format(Duration duration) {
        if (duration == null) return "00:00";
        return String.format("%02d:%02d", duration.toHours(), duration.toMinutesPart());
    }
}
