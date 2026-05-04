package software.ulpgc.netlikes.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

class SsoPayload {
    public String sso;
    public String sig;
    public String email;
    public String username;

    public String getSso() { return sso; }
    public void setSso(String sso) { this.sso = sso; }
    public String getSig() { return sig; }
    public void setSig(String sig) { this.sig = sig; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

@RestController
@RequestMapping("/auth")
@CrossOrigin(
    origins = "https://net-likes-bay.vercel.app", 
    allowCredentials = "true", 
    allowedHeaders = "*", 
    methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS}
)
public class SsoController {

    @Value("${discourse.sso.secret}")
    private String ssoSecret;

    @PostMapping("/sso/process")
    public ResponseEntity<?> processSso(@RequestBody SsoPayload payload) {
        try {
            // Log para ver qué nos llega (mira la consola de tu servidor)
            System.out.println("SSO recibido: " + payload.getSso());

            // 1. Validar firma
            String calculatedSig = calculateHmacSha256(payload.getSso(), ssoSecret);
            if (!calculatedSig.equalsIgnoreCase(payload.getSig())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Firma inválida");
            }

            // 2. Decodificar y extraer nonce
            String decodedSso = new String(Base64.getDecoder().decode(URLDecoder.decode(payload.getSso(), StandardCharsets.UTF_8)));
            String nonce = extractParam(decodedSso, "nonce");
            
            if (nonce == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se encontró el nonce en el payload");
            }

            // 3. Datos reales de Angular
            String email = payload.getEmail();
            String username = payload.getUsername();
            String externalId = email; 

            // 4. Firmar respuesta
            String reply = "nonce=" + nonce + "&email=" + email + "&external_id=" + externalId + "&username=" + username;
            String base64Reply = Base64.getEncoder().encodeToString(reply.getBytes(StandardCharsets.UTF_8));
            String signature = calculateHmacSha256(base64Reply, ssoSecret);

            String redirectUrl = "https://netlikes.duckdns.org/session/sso_login?sso=" + base64Reply + "&sig=" + signature;
            
            return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
        } catch (Exception e) {
            e.printStackTrace(); // Esto te imprimirá el error real en la consola de Spring Boot
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }

    /**
     * Mantenemos el GET antiguo por si acaso, pero el importante ahora es el POST
     */
    @GetMapping("/sso")
    public ResponseEntity<Void> sso(@RequestParam String sso, @RequestParam String sig) {
        String calculatedSig = calculateHmacSha256(sso, ssoSecret);
        if (!calculatedSig.equalsIgnoreCase(sig)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String decodedSso = new String(Base64.getDecoder().decode(URLDecoder.decode(sso, StandardCharsets.UTF_8)));
        String nonce = extractParam(decodedSso, "nonce");

        String email = "jose@ejemplo.com"; 
        String username = "JoseNetlikes";

        String reply = "nonce=" + nonce + "&email=" + email + "&external_id=" + email + "&username=" + username;
        String base64Reply = Base64.getEncoder().encodeToString(reply.getBytes(StandardCharsets.UTF_8));
        String signature = calculateHmacSha256(base64Reply, ssoSecret);

        String redirectUrl = "https://netlikes.duckdns.org/session/sso_login?sso=" + base64Reply + "&sig=" + signature;
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .build();
    }

    private String calculateHmacSha256(String data, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : signedBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error al firmar", e);
        }
    }

    private String extractParam(String payload, String param) {
        for (String p : payload.split("&")) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals(param)) return kv[1];
        }
        return null;
    }
}