package software.ulpgc.netlikes.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.ulpgc.netlikes.tmdbApi.FilmSyncScheduler;
import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public FilmSyncScheduler filmSyncScheduler() {
        return mock(FilmSyncScheduler.class);
    }
}