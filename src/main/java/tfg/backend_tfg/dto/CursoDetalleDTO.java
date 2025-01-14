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
    private String githubAsignatura;
    private String tokenGithub;
    private List<String> nombresEstudiantesSinGrupo;
    private List<String> correosEstudiantesSinGrupo;
    private List<String> gruposEstudiantesSinGrupo;
    private List<String> nombresProfesores;
    private List<EquipoDTO> equipos;

    public CursoDetalleDTO(int id, String nombreAsignatura, int añoInicio, int cuatrimestre, boolean activo, String githubAsignatura, String tokenGithub,
                           List<String> nombresEstudiantesSinGrupo, List<String> correosEstudiantesSinGrupo,
                           List<String> gruposEstudiantesSinGrupo, List<String> nombresProfesores, List<EquipoDTO> equipos) {
        this.id = id;
        this.nombreAsignatura = nombreAsignatura;
        this.añoInicio = añoInicio;
        this.cuatrimestre = cuatrimestre;
        this.activo = activo;
        this.githubAsignatura = githubAsignatura;
        this.tokenGithub = tokenGithub;
        this.nombresEstudiantesSinGrupo = nombresEstudiantesSinGrupo;
        this.correosEstudiantesSinGrupo = correosEstudiantesSinGrupo;
        this.gruposEstudiantesSinGrupo = gruposEstudiantesSinGrupo;
        this.nombresProfesores = nombresProfesores;
        this.equipos = equipos;
    }
}
