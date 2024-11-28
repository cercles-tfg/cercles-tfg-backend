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

    private List<EstudianteRequest> estudiantesAñadir;
    private List<EstudianteRequest> estudiantesBorrar;

    private List<ProfesorRequest> profesoresAñadir;
    private List<ProfesorRequest> profesoresBorrar;


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

    public List<EstudianteRequest> getEstudiantesAñadir(){
        return estudiantesAñadir;
    }

    public void setEstudiantesAñadir(List<EstudianteRequest> estudiantesAñadir) {
        this.estudiantesAñadir = estudiantesAñadir;
    }

    public List<EstudianteRequest> getEstudiantesBorrar(){
        return estudiantesBorrar;
    }

    public void setEstudiantesBorrar(List<EstudianteRequest> estudiantesBorrar) {
        this.estudiantesBorrar = estudiantesBorrar;
    }

    public List<ProfesorRequest> getProfesoresAñadir() {
        return profesoresAñadir;
    }

    public void getProfesoresAñadir(List<ProfesorRequest> profesoresAñadir) {
        this.profesoresAñadir = profesoresAñadir;
    }

    public List<ProfesorRequest> getProfesoresBorrar() {
        return profesoresBorrar;
    }

    public void getProfesoresBorrar(List<ProfesorRequest> profesoresBorrar) {
        this.profesoresBorrar = profesoresBorrar;
    }
}
