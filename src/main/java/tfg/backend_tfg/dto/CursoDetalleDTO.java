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
    private List<String> nombresEstudiantes;
    private List<String> nombresProfesores;

    public CursoDetalleDTO(int id, String nombreAsignatura, int añoInicio, int cuatrimestre, boolean activo, List<String> nombresEstudiantes, List<String> nombresProfesores) {
        this.id = id;
        this.nombreAsignatura = nombreAsignatura;
        this.añoInicio = añoInicio;
        this.cuatrimestre = cuatrimestre;
        this.activo = activo;
        this.nombresEstudiantes = nombresEstudiantes;
        this.nombresProfesores = nombresProfesores;
    }
}
