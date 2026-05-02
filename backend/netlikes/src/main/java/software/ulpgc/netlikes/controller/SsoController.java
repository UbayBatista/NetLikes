package software.ulpgc.netlikes.controller;
import org.springframework.http.MediaType;
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

@RestController
@RequestMapping("/auth")
public class SsoController {

    @Value("${discourse.sso.secret}")
    private String ssoSecret;

    @GetMapping("/sso")
    public ResponseEntity<String> sso(@RequestParam String sso, @RequestParam String sig) {
        
        String calculatedSig = calculateHmacSha256(sso, ssoSecret);
        if (!calculatedSig.equalsIgnoreCase(sig)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String decodedSso = new String(Base64.getDecoder().decode(URLDecoder.decode(sso, StandardCharsets.UTF_8)));
        String nonce = extractParam(decodedSso, "nonce");

        // C. IDENTIFICACIÓN: Aquí es donde tú nos dices quién es el usuario.
        // Por ahora lo dejamos con datos fijos para probar que el puente funciona.
        // Luego lo conectaremos con tu sistema de login real.
        String email = "jose@ejemplo.com"; 
        String externalId = "1"; // Un ID único (puede ser el ID de tu DB)
        String username = "JoseNetlikes";

        String reply = "nonce=" + nonce + "&email=" + email + "&external_id=" + externalId + "&username=" + username;
        String base64Reply = Base64.getEncoder().encodeToString(reply.getBytes(StandardCharsets.UTF_8));
        String signature = calculateHmacSha256(base64Reply, ssoSecret);

        String redirectUrl = "https://netlikes.duckdns.org/session/sso_login?sso=" + base64Reply + "&sig=" + signature;

        String html = "<html><body style='background:#111; color:white; text-align:center; padding-top:50px;'>" +
            "<h2>Sincronizando con Netlikes...</h2>" +
            "<script>" +
            "  // 1. Intentamos la redirección en un iframe invisible para forzar la cookie" +
            "  var img = new Image();" +
            "  img.src = '" + redirectUrl + "';" + 
            "  " +
            "  // 2. Después de un momento, cerramos la ventana" +
            "  setTimeout(function() {" +
            "    window.location.href = '" + redirectUrl + "';" + // Redirigimos la principal por si acaso
            "    setTimeout(function() { window.close(); }, 1500);" +
            "  }, 500);" +
            "</script></body></html>";

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
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
