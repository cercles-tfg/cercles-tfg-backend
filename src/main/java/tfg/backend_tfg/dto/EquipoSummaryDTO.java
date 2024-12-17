package tfg.backend_tfg.dto;

public class EquipoSummaryDTO {
    private int id;
    private String nombre;
    private int cursoId;
    private int evaluadorId;

    public EquipoSummaryDTO(int id, String nombre, int cursoId, int evaluadorId) {
        this.id = id;
        this.nombre = nombre;
        this.cursoId = cursoId;
        this.evaluadorId = evaluadorId;
    }

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

    public int getEvaluadorId() {
        return evaluadorId;
    }

    public void setEvaluadorId(int evaluadorId) {
        this.evaluadorId = evaluadorId;
    }
}
