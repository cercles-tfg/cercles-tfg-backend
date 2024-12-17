package tfg.backend_tfg.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tfg.backend_tfg.model.Rol;

@Data
@Builder

@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private Rol rol;
    private int id;

    public AuthenticationResponse(String token, Rol rol, int id) {
        this.token = token;
        this.rol = rol; 
        this.id = id;
    }
}
