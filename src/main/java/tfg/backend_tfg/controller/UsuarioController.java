package tfg.backend_tfg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.data.util.Pair;

import tfg.backend_tfg.model.Estudiante;
import tfg.backend_tfg.model.Profesor;
import tfg.backend_tfg.model.Rol;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.model.UsuarioRequest;
import tfg.backend_tfg.repository.UsuarioRepository;
import tfg.backend_tfg.services.GithubService;
import tfg.backend_tfg.services.UsuarioService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final GithubService githubService;
    @Autowired
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository, GithubService githubService) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.githubService = githubService;
    }

    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @PreAuthorize("hasAuthority('profesor')")
    @PostMapping("/crear")
    public ResponseEntity<?> crearUsuario(@RequestBody UsuarioRequest usuarioRequest) {
        // Verificar autenticación del usuario
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
        }
        
        // Llamar al servicio para crear el usuario
        return usuarioService.crearUsuario(usuarioRequest);
    }

    @GetMapping("/profesores")
    public ResponseEntity<?> getAllProfesores() {
        try {
            // Verificar autenticación del usuario
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
            }

            // Obtener el listado de profesores
            List<Usuario> profesores = usuarioRepository.findAllByRol(Rol.Profesor);
            List<Map<String, Object>> response = profesores.stream().map(profesor -> {
                Map<String, Object> profesorData = new HashMap<>();
                profesorData.put("id", profesor.getId());
                profesorData.put("nombre", profesor.getNombre());
                return profesorData;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener los profesores: " + e.getMessage());
        }
    }

    

    @GetMapping("/{id}")
    public Optional<Usuario> getUsuarioById(@PathVariable int id) {
        return usuarioService.getUsuarioById(id);
    }

    /* 
    @DeleteMapping("/{id}")
    public void deleteUsuario(@PathVariable int id) {
        usuarioService.deleteUsuario(id);
    }*/

    @GetMapping("/datos")
    public ResponseEntity<?> getDatosUsuario() {
        try {
            System.out.println("AL MENOS ENTRA");
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

            // Devolver los datos del usuario
            Usuario usuario = usuarioOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("nombre", usuario.getNombre());
            response.put("correo", usuario.getCorreo());
            response.put("gitUsername", usuario.getGitUsername());
            response.put("taigaUsername", usuario.getTaigaUsername());

            System.out.println("Datooos " + response);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }

    @PostMapping("/github/callback") //MOVER A GITHUBCONTROLLER
    public ResponseEntity<?> handleGitHubCallback(@RequestBody Map<String, String> requestBody) {
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

            // Obtener el código de autorización de GitHub
            String code = requestBody.get("code");
            System.out.println("codee: " + code);
            if (code == null || code.isEmpty()) {
                return ResponseEntity.status(400).body("Código de GitHub no proporcionado");
            }

            // Obtener el nombre de usuario de GitHub y el access token usando el código
            Pair<String, String> githubData = githubService.obtenerNombreUsuarioGitHub(code);
            if (githubData == null) {
                return ResponseEntity.status(500).body("No se pudo obtener el nombre de usuario o el access token de GitHub.");
            }

            String githubUsername = githubData.getFirst();
            String accessToken = githubData.getSecond();

            // Actualizar el usuario con el gitUsername y accessToken obtenido
            Usuario usuario = usuarioOpt.get();
            usuario.setGitUsername(githubUsername);
            usuario.setGithubAccessToken(accessToken);
            usuarioRepository.save(usuario);

            return ResponseEntity.ok("Cuenta de GitHub asociada exitosamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }




}
