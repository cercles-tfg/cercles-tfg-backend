package tfg.backend_tfg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.repository.UsuarioRepository;
import tfg.backend_tfg.services.UsuarioService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    @Autowired
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
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


}
