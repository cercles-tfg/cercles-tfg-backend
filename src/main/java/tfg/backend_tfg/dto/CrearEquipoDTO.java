package tfg.backend_tfg.dto;

import java.util.List;

public class CrearEquipoDTO {

    private String nombre;
    private int cursoId;
    private int evaluadorId;
    private List<Integer> estudiantesIds;

    public String getNombre() {
        return nombre;
    }

    public int getCursoId () {
        return cursoId;
    }

    public int getEvaluadorId() {
        return evaluadorId;
    }

    public List<Integer> getEstudiantesIds() {
        return estudiantesIds;
    }
}
