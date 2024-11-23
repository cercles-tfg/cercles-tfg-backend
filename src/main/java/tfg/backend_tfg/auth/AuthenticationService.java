package tfg.backend_tfg.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tfg.backend_tfg.model.Estudiante;
import tfg.backend_tfg.model.Profesor;
import tfg.backend_tfg.model.Rol;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.repository.UsuarioRepository;
import tfg.backend_tfg.services.GoogleAuthService;
import tfg.backend_tfg.services.JwtService;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    @Autowired
    private final JwtService jwtService;
    @Autowired
    private final GoogleAuthService googleAuthService;

    // Registro de usuarios (permite el registro de Estudiantes o Profesores)
    public AuthenticationResponse registro(RegisterRequest request) {
        Usuario usuario;
        if (request.getRol() == Rol.Estudiante) {
            usuario = Estudiante.builder()
                    .nombre(request.getNombre())
                    .correo(request.getCorreo())
                    .rol(request.getRol())
                    .build();
        } else if (request.getRol() == Rol.Profesor) {
            usuario = Profesor.builder()
                    .nombre(request.getNombre())
                    .correo(request.getCorreo())
                    .rol(request.getRol())
                    .build();
        } else {
            throw new IllegalArgumentException("Rol desconocido");
        }        

        usuarioRepository.save(usuario);
        var jwtToken = jwtService.generateToken(usuario);
        return new AuthenticationResponse(jwtToken); // Cambiar builder por constructor
    }

    // Método para autenticar con Google
    public AuthenticationResponse authenticate(String googleToken) throws GeneralSecurityException, IOException {
        // Verificar el token de Google y obtener el correo
        System.out.println("HA entrado");
        String email = googleAuthService.obtenerCorreoDesdeToken(googleToken);
        System.out.println("EMaaaail: " + email);
        boolean existe = googleAuthService.usuarioExiste(email);
        System.out.println("Existe? " + existe);
        if (email == null || !existe) {
            throw new IllegalArgumentException("Token de Google no válido o el usuario no está registrado.");
        }
        var usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        var jwtToken = jwtService.generateToken(usuario);
        return new AuthenticationResponse(jwtToken); // Cambiar builder por constructor
    }
}
