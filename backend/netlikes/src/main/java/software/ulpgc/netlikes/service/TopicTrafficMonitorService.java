package software.ulpgc.netlikes.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class TopicTrafficMonitorService {

    private final DiscourseService discourseService;
    
    private final ConcurrentHashMap<Integer, ConcurrentLinkedDeque<Long>> topicTraffic = new ConcurrentHashMap<>();
    
    private final Set<Integer> throttledTopics = ConcurrentHashMap.newKeySet();

    private static final int MESSAGES_TO_ENABLE = 15;  
    private static final int MESSAGES_TO_DISABLE = 3;
    private static final int TIME_WINDOW_SECONDS = 60; 
    private static final int SLOW_MODE_DURATION = 300;

    public TopicTrafficMonitorService(DiscourseService discourseService) {
        this.discourseService = discourseService;
    }

    public void registerNewMessage(Integer topicId) {
        long now = Instant.now().getEpochSecond();
        topicTraffic.putIfAbsent(topicId, new ConcurrentLinkedDeque<>());
        ConcurrentLinkedDeque<Long> timestamps = topicTraffic.get(topicId);

        timestamps.addLast(now);

        while (!timestamps.isEmpty() && (now - timestamps.getFirst() > TIME_WINDOW_SECONDS)) {
            timestamps.removeFirst();
        }

        if (timestamps.size() >= MESSAGES_TO_ENABLE && !throttledTopics.contains(topicId)) {
            throttledTopics.add(topicId);
            discourseService.setSlowMode(topicId, SLOW_MODE_DURATION);
            
            String botMessage = "¡Wow, qué rápidos sois escribiendo! 🏃‍♂️💨\n\n" +
                                "He activado el **Modo Lento** (" + (SLOW_MODE_DURATION / 60) + " min) " +
                                "temporalmente para que todos puedan leer los mensajes con calma.";
            discourseService.createBotPost(topicId, botMessage);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void checkAndDisableSlowMode() {
        long now = Instant.now().getEpochSecond();

        for (Integer topicId : throttledTopics) {
            ConcurrentLinkedDeque<Long> timestamps = topicTraffic.getOrDefault(topicId, new ConcurrentLinkedDeque<>());

            while (!timestamps.isEmpty() && (now - timestamps.getFirst() > TIME_WINDOW_SECONDS)) {
                timestamps.removeFirst();
            }

            if (timestamps.size() <= MESSAGES_TO_DISABLE) {
                discourseService.setSlowMode(topicId, 0);
                
                String calmMessage = "El tráfico ha vuelto a la normalidad. 😌\n\n" +
                                     "He **desactivado el Modo Lento**. ¡Podéis seguir charlando a vuestro ritmo!";
                discourseService.createBotPost(topicId, calmMessage);
                
                throttledTopics.remove(topicId);
                topicTraffic.remove(topicId);
            }
        }
    }
}