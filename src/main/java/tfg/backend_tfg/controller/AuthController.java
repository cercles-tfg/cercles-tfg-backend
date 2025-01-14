package tfg.backend_tfg.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import tfg.backend_tfg.auth.AuthenticationService;
import tfg.backend_tfg.auth.AuthenticationResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final AuthenticationService authService;

    // Endpoint para iniciar sesión con Google
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String googleToken = payload.get("googleToken");
        try {
            // Autenticar usando el token proporcionado
            AuthenticationResponse response = authService.authenticate(googleToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error en la autenticación: " + e.getMessage());
        }
    }

}
