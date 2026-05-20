package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import software.ulpgc.netlikes.service.HuggingFaceService;

@SpringBootTest
@ActiveProfiles("test")
class HuggingFaceIT {

    @Autowired
    private HuggingFaceService huggingFaceService; 

    @Test
    void shouldGenerateDeterministicVectors() {
        String gustosUsuario = String.format("Usuario interesado en películas de géneros: %s.", 
                        String.join(", ", "acción", "comedia", "ciencia ficción"));

        String vector1 = huggingFaceService.generateVector(gustosUsuario);
        String vector2 = huggingFaceService.generateVector(gustosUsuario);

        assertEquals(vector1, vector2, "La IA debe devolver el mismo vector exacto para la misma entrada");
        
        System.out.println("Prueba superada: Los dos vectores son clones matemáticos.");
    }

    @Test
    void shouldGenerateDifferentVectorsForDifferentInputs() {
        String gustosUsuario1 = String.format("Usuario interesado en películas de géneros: %s.", 
                        String.join(", ", "acción", "comedia", "ciencia ficción"));
        String gustosUsuario2 = String.format("Usuario interesado en películas de géneros: %s.", 
                        String.join(", ", "drama", "romance", "thriller"));

        String vector1 = huggingFaceService.generateVector(gustosUsuario1);
        String vector2 = huggingFaceService.generateVector(gustosUsuario2);

        assertNotEquals(vector1, vector2, "La IA debe devolver vectores diferentes para entradas distintas");
        
        System.out.println("Prueba superada: Los dos vectores son diferentes.");
    }
}