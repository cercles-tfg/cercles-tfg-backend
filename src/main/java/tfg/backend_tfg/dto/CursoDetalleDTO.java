package tfg.backend_tfg.dto;

import lombok.Data;
import java.util.List;

@Data
public class CursoDetalleDTO {
    private int id;
    private String nombreAsignatura;
    private int añoInicio;
    private int cuatrimestre;
    private boolean activo;
    private List<String> nombresEstudiantesSinGrupo;
    private List<String> correosEstudiantesSinGrupo;
    private List<String> nombresProfesores;
    private List<EquipoDTO> equipos;

    public CursoDetalleDTO(int id, String nombreAsignatura, int añoInicio, int cuatrimestre, boolean activo,
                           List<String> nombresEstudiantesSinGrupo, List<String> correosEstudiantesSinGrupo,
                           List<String> nombresProfesores, List<EquipoDTO> equipos) {
        this.id = id;
        this.nombreAsignatura = nombreAsignatura;
        this.añoInicio = añoInicio;
        this.cuatrimestre = cuatrimestre;
        this.activo = activo;
        this.nombresEstudiantesSinGrupo = nombresEstudiantesSinGrupo;
        this.correosEstudiantesSinGrupo = correosEstudiantesSinGrupo;
        this.nombresProfesores = nombresProfesores;
        this.equipos = equipos;
    }
}
