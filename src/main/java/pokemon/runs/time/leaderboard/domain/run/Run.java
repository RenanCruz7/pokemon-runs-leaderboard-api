package pokemon.runs.time.leaderboard.domain.run;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.utils.DurationConverter;
import pokemon.runs.time.leaderboard.utils.StringListConverter;

import java.time.LocalDateTime;
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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
