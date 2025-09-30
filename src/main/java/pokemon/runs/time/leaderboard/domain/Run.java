package pokemon.runs.time.leaderboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pokemon.runs.time.leaderboard.dto.CreateRunDTO;
import pokemon.runs.time.leaderboard.utils.DurationConverter;
import pokemon.runs.time.leaderboard.utils.StringListConverter;
import java.time.Duration;
import java.util.List;

@Table(name="runs")
@Getter
@Setter
@Entity(name="runs")
@NoArgsConstructor
public class Run {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String game;
    @Convert(converter = DurationConverter.class)
    private Duration runTime;
    private int pokedexStatus;
    @Convert(converter = StringListConverter.class)
    private List<String> pokemonTeam;
    private String observation;


    public Run(CreateRunDTO data) {
        this.game = data.game();
        this.runTime = parseDuration(data.runTime());
        this.pokedexStatus = data.pokedexStatus();
        this.pokemonTeam = data.pokemonTeam();
        this.observation = data.observation();
    }

    private Duration parseDuration(String hhmm) {
        if (hhmm == null || !hhmm.matches("\\d{1,2}:\\d{2}")) return Duration.ZERO;
        String[] parts = hhmm.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return Duration.ofHours(hours).plusMinutes(minutes);
    }
}
