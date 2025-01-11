package tfg.backend_tfg.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import tfg.backend_tfg.model.Estudiante;
import tfg.backend_tfg.model.Profesor;
import tfg.backend_tfg.model.Rol;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(int id) {
        return usuarioRepository.findById(id);
    }

    public Usuario saveUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void deleteUsuario(int id) {
        usuarioRepository.deleteById(id);
    }

    public boolean existe(String correo) {
        return usuarioRepository.findByCorreo(correo) != null;
    }

    public ResponseEntity<?> crearUsuario(String nombre, String correo, Rol rol) {
        try {
            // Verificar si ya existe un usuario con el correo dado
            Optional<Usuario> usuarioExistente = usuarioRepository.findByCorreo(correo);

            if (usuarioExistente.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuario con el correo ya existe");
            }

            Usuario usuario;
            if (rol == Rol.Estudiante) {
                usuario = Estudiante.builder()
                        .correo(correo)
                        .nombre(nombre)
                        .rol(rol)
                        .build();
            } else if (rol == Rol.Profesor) {
                usuario = Profesor.builder()
                        .correo(correo)
                        .nombre(nombre)
                        .rol(rol)
                        .build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rol inválido");
            }

            // Guardar el nuevo usuario en la base de datos
            usuarioRepository.save(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuario);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el usuario: " + e.getMessage());
        }
    }

}
