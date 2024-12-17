package tfg.backend_tfg.dto;

import lombok.Data;

@Data
public class CursoSummaryDTO {
    private int id;
    private String nombreAsignatura;
    private int añoInicio;
    private int cuatrimestre;
    private boolean activo;
    private int numeroEstudiantes;

    public CursoSummaryDTO(int id, String nombreAsignatura, int añoInicio, int cuatrimestre, boolean activo, int numeroEstudiantes) {
        this.id = id;
        this.nombreAsignatura = nombreAsignatura;
        this.añoInicio = añoInicio;
        this.cuatrimestre = cuatrimestre;
        this.activo = activo;
        this.numeroEstudiantes = numeroEstudiantes;
    }
}
