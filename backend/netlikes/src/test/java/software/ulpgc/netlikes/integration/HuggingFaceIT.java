package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;

import software.ulpgc.netlikes.service.HuggingFaceService;

@SpringBootTest
@ActiveProfiles("test")
class HuggingFaceIT {

    @Autowired
    private HuggingFaceService huggingFaceService; 

    @Test
    @DisplayName("Should generate the same vector when the same input is provided")
    void should_GenerateSameVector_when_SameInputIsProvided() {
        String gustosUsuario = String.format("Usuario interesado en películas de géneros: %s.", 
                        String.join(", ", "acción", "comedia", "ciencia ficción"));

        String vector1 = huggingFaceService.generateVector(gustosUsuario);
        String vector2 = huggingFaceService.generateVector(gustosUsuario);

        assertEquals(vector1, vector2, "La IA debe devolver el mismo vector exacto para la misma entrada");
    }

    @Test
    @DisplayName("Should generate different vectors when different inputs are provided")
    void should_GenerateDifferentVectors_when_InputsAreDifferent() {
        String gustosUsuario1 = String.format("Usuario interesado en películas de géneros: %s.", 
                        String.join(", ", "acción", "comedia", "ciencia ficción"));
        String gustosUsuario2 = String.format("Usuario interesado en películas de géneros: %s.", 
                        String.join(", ", "drama", "romance", "thriller"));

        String vector1 = huggingFaceService.generateVector(gustosUsuario1);
        String vector2 = huggingFaceService.generateVector(gustosUsuario2);

        assertNotEquals(vector1, vector2, "La IA debe devolver vectores diferentes para entradas distintas");
    }
}