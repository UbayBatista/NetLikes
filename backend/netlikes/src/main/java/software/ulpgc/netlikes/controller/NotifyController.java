package software.ulpgc.netlikes.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.RequiredArgsConstructor;

import software.ulpgc.netlikes.dto.NotifyResponseDTO;
import software.ulpgc.netlikes.service.NotifyService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    @GetMapping(value = "/stream/{email}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToNotifications(@PathVariable String email) {
        return notifyService.subscribe(email);
    }

    @GetMapping("/{email}")
    public ResponseEntity<List<NotifyResponseDTO>> getMyNotifications(@PathVariable String email) {
        return ResponseEntity.ok(notifyService.getUserNotifications(email));
    }

    @GetMapping("/{email}/unread-count")
    public ResponseEntity<?> getUnreadCount(@PathVariable String email) {
        long count = notifyService.getUnreadCount(email);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{email}/read-all")
    public ResponseEntity<?> markAllAsRead(@PathVariable String email) {
        notifyService.markAllAsRead(email);
        return ResponseEntity.ok(Map.of("status", "Todas las notificaciones marcadas como leídas"));
    }
}