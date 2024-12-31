package tfg.backend_tfg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import tfg.backend_tfg.dto.MetricasUsuarioDTO;
import tfg.backend_tfg.model.Equipo;
import tfg.backend_tfg.model.Estudiante;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.repository.EquipoRepository;
import tfg.backend_tfg.repository.EstudianteCursoRepository;
import tfg.backend_tfg.repository.EstudianteRepository;
import tfg.backend_tfg.repository.UsuarioRepository;
import tfg.backend_tfg.services.EquipoService;
import tfg.backend_tfg.services.GithubService;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GithubService githubService;
    @Autowired
    private final UsuarioRepository usuarioRepository;
    @Autowired
    private final EstudianteRepository estudianteRepository;
    @Autowired
    private final EquipoRepository equipoRepository;
    @Autowired
    private final EquipoService equipoService;

    @Value("${professorat-amep.token}")
    private String profAmepToken;
    
    public GitHubController(GithubService githubService, UsuarioRepository usuarioRepository, EstudianteRepository estudianteRepository, EquipoRepository equipoRepository, EquipoService equipoService) {
        this.githubService = githubService;
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.equipoRepository = equipoRepository;
        this.equipoService = equipoService;
    }

    @PostMapping("/validar-organizacion")
    public ResponseEntity<?> validarOrganizacion(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(403).body(Map.of("error", "Usuario no autenticado"));
            }

            String organizacionUrl = (String) request.get("organizacionUrl");
            List<Integer> miembrosIds = (List<Integer>) request.get("miembrosIds");
            Integer profesorId = Integer.valueOf(request.get("profesorId").toString());

            Map<String, Boolean> resultado = githubService.validarOrganizacion(profesorId, miembrosIds, organizacionUrl);

            return ResponseEntity.ok(resultado);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al comunicarse con la API de GitHub: " + e.getResponseBodyAsString()
            ));
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


    @PreAuthorize("hasAuthority('PROFESOR')")
    @GetMapping("/metrics/{organizacion}")
    public ResponseEntity<?> obtenerMetricas(@PathVariable String organizacion, @RequestParam List<Integer> estudiantesIds) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(403).body(Map.of("error", "Usuario no autenticado"));
            }

            // Obtener usuarios GitHub de los estudiantes
            List<String> usuarios = estudianteRepository.findAllById(estudiantesIds)
                    .stream()
                    .map(Estudiante::getGitUsername)
                    .filter(username -> username != null && !username.isEmpty())
                    .collect(Collectors.toList());

            // Llamada al servicio con el token personal
            List<MetricasUsuarioDTO> metricas = githubService.obtenerMetricasOrganizacion(
                    organizacion, usuarios, profAmepToken, estudiantesIds
            );

            return ResponseEntity.ok(metricas);
        } catch (Exception e) {
            e.printStackTrace(); // Log completo del error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
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
            String code = requestBody.get("code");

            if (code == null || code.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "Código de GitHub faltante"));
            }

            Map<String, String> result = githubService.handleGitHubCallback(email, code);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }

    @GetMapping("/user-data")
    public ResponseEntity<?> obtenerDatosGitHub() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(403).body(Map.of("error", "Usuario no autenticado"));
            }

            String email = authentication.getName();
            Map<String, Object> datosUsuario = githubService.obtenerDatosUsuarioGitHub(email);

            if (datosUsuario == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Datos no encontrados para este usuario."));
            }

            return ResponseEntity.ok(datosUsuario);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }

    @DeleteMapping("/disconnect")
    public ResponseEntity<?> desconectarGitHub() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(403).body(Map.of("error", "Usuario no autenticado"));
            }

            String email = authentication.getName();
            githubService.desconectarGitHub(email);
            return ResponseEntity.ok(Map.of("message", "GitHub desconectado exitosamente"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }

}
