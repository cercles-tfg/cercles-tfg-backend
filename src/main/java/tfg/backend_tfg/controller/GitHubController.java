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

import java.util.Optional;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GithubService githubService;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public GitHubController(GithubService githubService, UsuarioRepository usuarioRepository) {
        this.githubService = githubService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/detalles")
    public ResponseEntity<?> obtenerDetallesGitHub() {
        try {
            // Obtener la información de autenticación desde el SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(403).body("Usuario no autenticado.");
            }

            // Obtener el correo del usuario autenticado
            String email = authentication.getName();

            // Buscar el usuario en la base de datos usando el correo electrónico
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }

            // Obtener el access token para GitHub (que deberíamos haber guardado previamente)
            Usuario usuario = usuarioOpt.get();
            String accessToken = usuario.getGithubAccessToken(); // Asegúrate de tener este campo en el modelo Usuario

            if (accessToken == null || accessToken.isEmpty()) {
                return ResponseEntity.status(400).body("No se ha encontrado un access token de GitHub asociado.");
            }

            // Obtener detalles adicionales del usuario de GitHub
            GitHubUserDetails githubDetails = githubService.obtenerDetallesAdicionalesUsuarioGitHub(accessToken);
            if (githubDetails == null) {
                return ResponseEntity.status(500).body("No se pudieron obtener los detalles del usuario de GitHub.");
            }

            return ResponseEntity.ok(githubDetails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }
}
