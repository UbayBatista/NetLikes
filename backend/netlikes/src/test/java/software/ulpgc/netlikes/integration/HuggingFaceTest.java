package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

import software.ulpgc.netlikes.service.HuggingFaceService;

@SpringBootTest
@ActiveProfiles("test")
class HuggingFaceServiceIntegrationTest {

    @Autowired
    private HuggingFaceService huggingFaceService; 

    @Test
    void testHuggingFaceModelIsDeterministic() {
        String gustosUsuario = String.format("Usuario interesado en películas de géneros: %s.", 
                        String.join(", ", "acción", "comedia", "ciencia ficción"));

        String vector1 = huggingFaceService.generateVector(gustosUsuario);
        String vector2 = huggingFaceService.generateVector(gustosUsuario);

        assertEquals(vector1, vector2, "La IA debe devolver el mismo vector exacto para la misma entrada");
        
        System.out.println("Prueba superada: Los dos vectores son clones matemáticos.");
    }
}