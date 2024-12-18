package tfg.backend_tfg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import tfg.backend_tfg.model.Equipo;
import tfg.backend_tfg.model.GitHubUserDetails;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.repository.EquipoRepository;
import tfg.backend_tfg.repository.UsuarioRepository;
import tfg.backend_tfg.services.EquipoService;
import tfg.backend_tfg.services.GithubService;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GithubService githubService;
    @Autowired
    private final UsuarioRepository usuarioRepository;
    @Autowired
    private final EquipoService equipoService;
    
    public GitHubController(GithubService githubService, UsuarioRepository usuarioRepository, EquipoService equipoService) {
        this.githubService = githubService;
        this.usuarioRepository = usuarioRepository;
        this.equipoService = equipoService;
    }


    @GetMapping("/instalacion")
    public ResponseEntity<?> obtenerUrlInstalacion(@RequestParam Integer equipoId) {
        try {
            String callbackUrl = "http://localhost:8080/api/github/callback";
            String githubAppUrl = String.format(
                "https://github.com/apps/CERCLES-APP/installations/new?state=%d&redirect_uri=%s",
                equipoId, callbackUrl
            );
            // Actualizar el estado del equipo
            Equipo equipo = equipoService.obtenerEquipoPorId(equipoId);
            if (equipo != null) {
                equipo.setGithubAppInstalada(true);
                equipoService.actualizarEquipo(equipo);
                System.out.println("GitHub App marcada como instalada para el equipo ID: " + equipoId);
            } else {
                System.err.println("Equipo no encontrado con ID: " + equipoId);
            }
            return ResponseEntity.ok(Map.of("url", githubAppUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al generar la URL de instalación.");
        }
    }
    
    @GetMapping("/callback")
    public ResponseEntity<Void> handleGithubCallback(
            @RequestParam("installation_id") String installationId,
            @RequestParam("state") Integer equipoId) {
        try {
            // Actualizar el estado del equipo
            Equipo equipo = equipoService.obtenerEquipoPorId(equipoId);
            if (equipo != null) {
                equipo.setGithubAppInstalada(true);
                equipoService.actualizarEquipo(equipo);
                System.out.println("GitHub App marcada como instalada para el equipo ID: " + equipoId);
            } else {
                System.err.println("Equipo no encontrado con ID: " + equipoId);
            }
    
            // Redirigir al frontend a la página de equipos
            String redirectUrl = "http://localhost:3000/equipos";
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
        } catch (Exception e) {
            System.err.println("Error en el callback de GitHub: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    



    @PostMapping("/validar-organizacion")
    public ResponseEntity<?> validarOrganizacion(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(403).body(Map.of("error", "Usuario no autenticado"));
            }
            System.out.println("datos "+ request);

            String organizacionUrl = (String) request.get("organizacionUrl");
            List<Integer> miembrosIds = (List<Integer>) request.get("miembrosIds");
            Integer profesorId = Integer.valueOf(request.get("profesorId").toString());

            Map<String, Boolean> resultado = githubService.validarOrganizacion(profesorId, miembrosIds, organizacionUrl);

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @PostMapping("/confirmar-organizacion")
    public ResponseEntity<?> confirmarOrganizacion(@RequestBody Map<String, Object> request) {
        try {
            Integer equipoId = (Integer) request.get("equipoId");
            String organizacionUrl = (String) request.get("organizacionUrl");

            githubService.asignarOrganizacion(equipoId, organizacionUrl);

            return ResponseEntity.ok(Map.of("mensaje", "Organización asignada correctamente."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }


    
    @PostMapping("/callback")
    public ResponseEntity<?> handleGitHubCallback(@RequestBody Map<String, String> requestBody) {
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

            String code = requestBody.get("code");
            if (code == null || code.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "Código de GitHub faltante"));
            }

            Pair<String, String> githubData = githubService.obtenerNombreUsuarioGitHub(code);
            if (githubData == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Error al obtener datos de GitHub"));
            }

            String githubUsername = githubData.getFirst();
            String accessToken = githubData.getSecond();

            Usuario usuario = usuarioOpt.get();
            usuario.setGitUsername(githubUsername);
            usuario.setGithubAccessToken(accessToken); // Token guardado
            usuarioRepository.save(usuario);

            return ResponseEntity.ok(Map.of("message", "Cuenta de GitHub asociada exitosamente"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
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
