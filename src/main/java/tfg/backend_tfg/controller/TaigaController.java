package tfg.backend_tfg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
                return ResponseEntity.status(403).body(Map.of("error", "Usuario no autenticado"));
            }

            String email = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            }

            Usuario usuario = usuarioOpt.get();
            String type = requestBody.get("type");
            Map<String, Object> taigaData;

            if ("normal".equals(type)) {
                String username = requestBody.get("username");
                String password = requestBody.get("password");
                if (username == null || password == null) {
                    return ResponseEntity.status(400).body(Map.of("error", "Credenciales faltantes"));
                }
                taigaData = taigaService.authenticateTaigaUser(username, password);

            } else if ("github".equals(type)) {
                // Verifica si el usuario tiene el GitHub token
                if (usuario.getGithubAccessToken() == null) {
                    return ResponseEntity.status(400).body(Map.of("error", "Token de GitHub no disponible. Conéctate a GitHub primero."));
                }
                taigaData = taigaService.authenticateTaigaUserWithGitHub(usuario.getGithubAccessToken());
            } else {
                return ResponseEntity.status(400).body(Map.of("error", "Tipo de autenticación inválido"));
            }

            if (taigaData == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Autenticación fallida en Taiga"));
            }

            // Actualiza los datos en la base de datos
            usuario.setTaigaId((Integer) taigaData.get("id"));
            usuario.setTaigaUsername((String) taigaData.get("username"));
            usuarioRepository.save(usuario);

            return ResponseEntity.ok(Map.of("message", "Cuenta de Taiga asociada correctamente"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }
}
