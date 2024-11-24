package tfg.backend_tfg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import tfg.backend_tfg.model.GitHubUserDetails;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.repository.UsuarioRepository;
import tfg.backend_tfg.services.GithubService;
import tfg.backend_tfg.services.TaigaService;

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

    @GetMapping("/detalles")
    public ResponseEntity<?> obtenerDetallesTaiga() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(403).body("Usuario no autenticado.");
            }

            String email = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();
            String accessToken = usuario.getTaigaAccessToken();
            if (accessToken == null || accessToken.isEmpty()) {
                return ResponseEntity.status(400).body("No se ha encontrado un access token de Taiga asociado.");
            }

            // Llamar al servicio para obtener detalles adicionales del usuario de Taiga
            // Por ahora, podrías simplemente devolver el token o hacer alguna llamada a Taiga.

            return ResponseEntity.ok("Detalles obtenidos exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }
}
