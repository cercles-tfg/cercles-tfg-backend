/*package tfg.backend_tfg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/verificar")
    public Map<String, Boolean> verificarUsuario(@RequestBody Map<String, String> request) {
        String correo = request.get("correo");
        boolean existe = usuarioService.existe(correo);
        return Map.of("existe", existe);
    }

    @PostMapping("/crear")
    public ResponseEntity<Usuario> createUsuario(@RequestBody Usuario usuario) {
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{id}")
    public void deleteUsuario(@PathVariable int id) {
        usuarioService.deleteUsuario(id);
    }

    @GetMapping("/{email}/datos")
public ResponseEntity<?> getDatosUsuario(@PathVariable String email) {
    try {
        // Buscar el usuario por el correo electrónico proporcionado
        Optional<Usuario> usuario = usuarioRepository.findByCorreo(email);
        if (usuario == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        // Devolver los datos del usuario (por ejemplo, el gitUsername)
        Map<String, Object> response = new HashMap<>();
        response.put("nombre", usuario.getNombre());
        response.put("correo", usuario.getCorreo());
        response.put("gitUsername", usuario.getGitUsername());

        return ResponseEntity.ok(response);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
    }
}


}
*/