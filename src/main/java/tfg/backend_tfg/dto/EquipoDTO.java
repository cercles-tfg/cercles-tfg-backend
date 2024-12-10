package tfg.backend_tfg.dto;

import lombok.Data;
import java.util.List;

@Data
public class EquipoDTO {
    private String nombreEquipo;
    private List<String> miembros;

    public EquipoDTO(String nombreEquipo, List<String> miembros) {
        this.nombreEquipo = nombreEquipo;
        this.miembros = miembros;
    }
}
