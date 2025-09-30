package pokemon.runs.time.leaderboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pokemon.runs.time.leaderboard.domain.Run;
import pokemon.runs.time.leaderboard.dto.CreateRunDTO;
import pokemon.runs.time.leaderboard.repository.RunRepository;

@Service
public class RunService {
    @Autowired
    private RunRepository runRepository;

    public Run createRun(CreateRunDTO data) {
        Run run = new Run(data);
        return runRepository.save(run);
    }
}
