package software.ulpgc.netlikes.tmdbApi;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("!test")
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class FilmSyncScheduler {

    private final InitialFilmLoadService initialLoadService;
    // TODO: crear un método para sincronizar periódicamente con TMDb y actualizar la base de datos

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onStartup() {
        log.info("Aplicación lista — comprobando si se necesita carga inicial...");
        initialLoadService.loadAll();
    }
}