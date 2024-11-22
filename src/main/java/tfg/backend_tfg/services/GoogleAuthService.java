package tfg.backend_tfg.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tfg.backend_tfg.repository.UsuarioRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String clientId;

    private final UsuarioRepository usuarioRepository;
    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService(UsuarioRepository usuarioRepository) throws GeneralSecurityException, IOException {
        this.usuarioRepository = usuarioRepository;

        // Configurar el GoogleIdTokenVerifier una vez en el constructor
        this.verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public String obtenerCorreoDesdeToken(String googleToken) {
        try {
            // Utiliza el verificador que ya está configurado en el constructor
            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                return email;
            } else {
                throw new IllegalArgumentException("Token de Google no válido.");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Error al validar el token de Google: " + e.getMessage(), e);
        }
    }

    public boolean usuarioExiste(String correo) {
        return usuarioRepository.findByCorreo(correo).isPresent();
    }
}
