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


    // Método para autenticar con Google
    public AuthenticationResponse authenticate(String googleToken) throws GeneralSecurityException, IOException {
        // Verificar el token de Google y obtener el correo
        String email = googleAuthService.obtenerCorreoDesdeToken(googleToken);
        boolean existe = googleAuthService.usuarioExiste(email);
        if (email == null || !existe) {
            throw new IllegalArgumentException("Token de Google no válido o el usuario no está registrado.");
        }
        var usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        var jwtToken = jwtService.generateToken(usuario);
        return new AuthenticationResponse(jwtToken, usuario.getRol(), usuario.getId());
    }
}
