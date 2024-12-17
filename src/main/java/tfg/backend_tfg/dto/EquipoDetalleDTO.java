package tfg.backend_tfg.dto;

import java.util.List;

public class EquipoDetalleDTO {
    private int id;
    private String nombre;

    // Información del curso
    private int cursoId;
    private String nombreAsignatura;
    private int añoInicio;
    private int cuatrimestre;
    private boolean activo;

    // Información del evaluador
    private int evaluadorId;
    private String evaluadorNombre;
    private String evaluadorCorreo;

    // Lista de estudiantes
    private List<EstudianteDTO> estudiantes;

    // Constructor completo
    public EquipoDetalleDTO(int id, String nombre, int cursoId, String nombreAsignatura,
                                    int añoInicio, int cuatrimestre, boolean activo,
                                    int evaluadorId, String evaluadorNombre, String evaluadorCorreo,
                                    List<EstudianteDTO> estudiantes) {
        this.id = id;
        this.nombre = nombre;
        this.cursoId = cursoId;
        this.nombreAsignatura = nombreAsignatura;
        this.añoInicio = añoInicio;
        this.cuatrimestre = cuatrimestre;
        this.activo = activo;
        this.evaluadorId = evaluadorId;
        this.evaluadorNombre = evaluadorNombre;
        this.evaluadorCorreo = evaluadorCorreo;
        this.estudiantes = estudiantes;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCursoId() {
        return cursoId;
    }

    public void setCursoId(int cursoId) {
        this.cursoId = cursoId;
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

    public int getEvaluadorId() {
        return evaluadorId;
    }

    public void setEvaluadorId(int evaluadorId) {
        this.evaluadorId = evaluadorId;
    }

    public String getEvaluadorNombre() {
        return evaluadorNombre;
    }

    public void setEvaluadorNombre(String evaluadorNombre) {
        this.evaluadorNombre = evaluadorNombre;
    }

    public String getEvaluadorCorreo() {
        return evaluadorCorreo;
    }

    public void setEvaluadorCorreo(String evaluadorCorreo) {
        this.evaluadorCorreo = evaluadorCorreo;
    }

    public List<EstudianteDTO> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(List<EstudianteDTO> estudiantes) {
        this.estudiantes = estudiantes;
    }

}
