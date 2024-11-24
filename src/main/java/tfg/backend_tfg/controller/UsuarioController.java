package tfg.backend_tfg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.data.util.Pair;


import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.repository.UsuarioRepository;
import tfg.backend_tfg.services.GithubService;
import tfg.backend_tfg.services.TaigaService;
import tfg.backend_tfg.services.UsuarioService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final GithubService githubService;
    private final TaigaService taigaService;
    @Autowired
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository, GithubService githubService, TaigaService taigaService) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.githubService = githubService;
        this.taigaService = taigaService;
    }

    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
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

    @PostMapping("/github/callback")
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


    @PostMapping("/taiga/connect")
public ResponseEntity<?> connectTaiga(@RequestBody Map<String, String> requestBody) {
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

        Usuario usuario = usuarioOpt.get();

        // Intentar autenticar el usuario con Taiga usando username/password o GitHub token
        String taigaAuthToken = null;

        if (requestBody.containsKey("username") && requestBody.containsKey("password")) {
            // Autenticación con username y password
            String taigaUsername = requestBody.get("username");
            String taigaPassword = requestBody.get("password");
            taigaAuthToken = taigaService.authenticateTaigaUser(taigaUsername, taigaPassword);

            if (taigaAuthToken != null) {
                usuario.setTaigaUsername(taigaUsername);
            }

        } else if (requestBody.containsKey("githubAccessToken")) {
            // Autenticación con GitHub access token
            String githubAccessToken = requestBody.get("githubAccessToken");
            taigaAuthToken = taigaService.authenticateTaigaUserWithGitHub(githubAccessToken);
        }

        if (taigaAuthToken == null) {
            return ResponseEntity.status(401).body("No se pudo autenticar al usuario en Taiga.");
        }

        // Actualizar el usuario con el taigaAccessToken obtenido
        usuario.setTaigaAccessToken(taigaAuthToken);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Cuenta de Taiga asociada exitosamente");
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
    }
}






}
