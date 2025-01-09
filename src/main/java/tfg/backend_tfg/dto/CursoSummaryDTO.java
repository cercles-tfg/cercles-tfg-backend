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
    private int numeroEquipos;
    private int numeroEstudiantesSinEquipo;

    public CursoSummaryDTO(int id, String nombreAsignatura, int añoInicio, int cuatrimestre, boolean activo, int numeroEstudiantes, int numeroEquipos,
    int numeroEstudiantesSinEquipo) {
        this.id = id;
        this.nombreAsignatura = nombreAsignatura;
        this.añoInicio = añoInicio;
        this.cuatrimestre = cuatrimestre;
        this.activo = activo;
        this.numeroEstudiantes = numeroEstudiantes;
        this.numeroEquipos = numeroEquipos;
        this.numeroEstudiantesSinEquipo = numeroEstudiantesSinEquipo;
    }
}
