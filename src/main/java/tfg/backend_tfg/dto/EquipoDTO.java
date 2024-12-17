package tfg.backend_tfg.dto;

import lombok.Data;
import java.util.List;

@Data
public class EquipoDTO {
    private String nombreEquipo;
    private int id_equipo;
    private int idProfe;
    private List<String> miembros;

    public EquipoDTO(String nombreEquipo, int id_equipo, int idProfe, List<String> miembros) {
        this.nombreEquipo = nombreEquipo;
        this.id_equipo = id_equipo;
        this.idProfe = idProfe;
        this.miembros = miembros;
    }
}
