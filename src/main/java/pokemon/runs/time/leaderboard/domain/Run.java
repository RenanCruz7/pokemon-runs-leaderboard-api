package pokemon.runs.time.leaderboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pokemon.runs.time.leaderboard.utils.DurationConverter;
import pokemon.runs.time.leaderboard.utils.StringListConverter;
import java.time.Duration;
import java.util.List;

@Table(name="runs")
@Getter
@Setter
@Entity(name="runs")
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
}
