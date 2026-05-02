package software.ulpgc.netlikes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.RequiredArgsConstructor;

import software.ulpgc.netlikes.dto.NotifyResponseDTO;
import software.ulpgc.netlikes.model.Notify;
import software.ulpgc.netlikes.model.NotifyId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.NotifyRepository;
import software.ulpgc.netlikes.repository.UserRepository;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotifyService {

    private final NotifyRepository notifyRepository;
    private final UserRepository userRepository;
    
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String email) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        emitters.computeIfAbsent(email, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(email, emitter));
        emitter.onTimeout(() -> removeEmitter(email, emitter));
        emitter.onError((e) -> removeEmitter(email, emitter));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Conexión SSE establecida para " + email));
        } catch (Exception e) {
            removeEmitter(email, emitter);
        }

        return emitter;
    }

    private void removeEmitter(String email, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(email);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(email);
            }
        }
    }

    public void createFollowNotification(String senderEmail, String receiverEmail) {
        User sender = userRepository.findById(senderEmail).orElseThrow();
        User receiver = userRepository.findById(receiverEmail).orElseThrow();

        String message = sender.getName() + " quiere seguirte.";
        NotifyId id = new NotifyId(senderEmail, receiverEmail, message);

        Notify notification = new Notify();
        notification.setId(id);
        notification.setUserSender(sender);
        notification.setUserReceiver(receiver);
        notification.setDate(new Date(System.currentTimeMillis()));
        notification.setRead(false);
        notification.setType(Notify.Type.FOLLOWREQUEST);

        notifyRepository.save(notification);

        NotifyResponseDTO dto = toDTO(notification);
        List<SseEmitter> userEmitters = emitters.get(receiverEmail);
        
        if (userEmitters != null) {
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name("NEW_NOTIFICATION").data(dto));
                } catch (Exception e) {
                    removeEmitter(receiverEmail, emitter);
                }
            }
        }
    }

    public List<NotifyResponseDTO> getUserNotifications(String receiverEmail) {
        return notifyRepository.findByUserReceiverEmailOrderByDateDesc(receiverEmail)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String receiverEmail) {
        return notifyRepository.countByUserReceiverEmailAndReadFalse(receiverEmail);
    }

    @Transactional
    public void markAllAsRead(String receiverEmail) {
        notifyRepository.markAllAsReadForUser(receiverEmail);
    }

    private NotifyResponseDTO toDTO(Notify notify) {
        NotifyResponseDTO dto = new NotifyResponseDTO();
        dto.setSenderEmail(notify.getUserSender().getEmail());
        dto.setSenderName(notify.getUserSender().getName());
        dto.setSenderProfilePicture(notify.getUserSender().getProfilePicture());
        dto.setMessage(notify.getId().getMessage());
        dto.setDate(notify.getDate());
        dto.setRead(notify.isRead());
        dto.setType(notify.getType().name());
        return dto;
    }

    @Transactional
    public void deleteFollowNotification(String senderEmail, String receiverEmail) {
        // 1. Borramos de la base de datos
        notifyRepository.deleteByUserSenderEmailAndUserReceiverEmailAndType(
            senderEmail, 
            receiverEmail, 
            Notify.Type.FOLLOWREQUEST
        );

        // 2. Avisamos al frontend por SSE
        List<SseEmitter> userEmitters = emitters.get(receiverEmail);
        if (userEmitters != null) {
            // Enviamos un objeto simple con los datos necesarios para identificar qué borrar
            Map<String, String> data = new HashMap<>();
            data.put("senderEmail", senderEmail);
            data.put("type", Notify.Type.FOLLOWREQUEST.name());

            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                        .name("DELETE_NOTIFICATION") // Nombre del evento que escuchará Angular
                        .data(data));
                } catch (Exception e) {
                    removeEmitter(receiverEmail, emitter);
                }
            }
        }
    }
}