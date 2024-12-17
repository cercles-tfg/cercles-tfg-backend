package tfg.backend_tfg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.repository.UsuarioRepository;
import tfg.backend_tfg.services.TaigaService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/taiga")
public class TaigaController {

    private final TaigaService taigaService;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public TaigaController(TaigaService taigaService, UsuarioRepository usuarioRepository) {
        this.taigaService = taigaService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connectTaiga(@RequestBody Map<String, String> requestBody) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(403).body("Usuario no autenticado.");
            }

            String email = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Usuario no encontrado.");
            }

            Usuario usuario = usuarioOpt.get();
            String taigaAuthToken = null;

            if ("normal".equals(requestBody.get("type"))) {
                String username = requestBody.get("username");
                String password = requestBody.get("password");

                if (username == null || password == null) {
                    return ResponseEntity.status(400).body("Faltan credenciales.");
                }

                taigaAuthToken = taigaService.authenticateTaigaUser(username, password);
                if (taigaAuthToken != null) {
                    usuario.setTaigaUsername(username);
                }

            } else if ("github".equals(requestBody.get("type"))) {
                String code = requestBody.get("code");
                if (code == null) {
                    return ResponseEntity.status(400).body("Falta el código de GitHub.");
                }

                taigaAuthToken = taigaService.authenticateTaigaUserWithGitHub(code);
            }

            if (taigaAuthToken == null) {
                return ResponseEntity.status(401).body("No se pudo autenticar al usuario en Taiga.");
            }

            usuario.setTaigaAccessToken(taigaAuthToken);
            usuarioRepository.save(usuario);

            return ResponseEntity.ok("Cuenta de Taiga asociada exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno del servidor.");
        }
    }
}
