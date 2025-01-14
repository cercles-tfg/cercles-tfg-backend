package tfg.backend_tfg.dto;

import lombok.Data;
import java.util.Map;

@Data
public class EquipoDTO {
    private String nombreEquipo;
    private int id_equipo;
    private int idProfe;
    private boolean validado;
    private Map<String,String> miembros;

    public EquipoDTO(String nombreEquipo, int id_equipo, int idProfe, boolean validado, Map<String,String> miembros) {
        this.nombreEquipo = nombreEquipo;
        this.id_equipo = id_equipo;
        this.idProfe = idProfe;
        this.validado = validado;
        this.miembros = miembros;
    }
}
