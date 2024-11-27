package tfg.backend_tfg.model;

import java.util.List;

public class CursoRequest {
    private int id;
    private String nombreAsignatura;
    private int añoInicio;
    private int cuatrimestre;
    private boolean activo;
    private List<EstudianteRequest> estudiantes;
    private List<Integer> profesores;

    // Getters y setters
    public Integer getId() {
        return id;
    }
    
    public String getNombreAsignatura() {
        return nombreAsignatura;
    }

    public void setNombreAsignatura(String nombreAsignatura) {
        this.nombreAsignatura = nombreAsignatura;
    }

    public int getAñoInicio() {
        return añoInicio;
    }

    public void setAñoInicio(int añoInicio) {
        this.añoInicio = añoInicio;
    }

    public int getCuatrimestre() {
        return cuatrimestre;
    }

    public void setCuatrimestre(int cuatrimestre) {
        this.cuatrimestre = cuatrimestre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<EstudianteRequest> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(List<EstudianteRequest> estudiantes) {
        this.estudiantes = estudiantes;
    }

    public List<Integer> getProfesores() {
        return profesores;
    }

    public void setProfesores(List<Integer> profesores) {
        this.profesores = profesores;
    }
}
