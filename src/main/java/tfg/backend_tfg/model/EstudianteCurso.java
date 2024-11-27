package tfg.backend_tfg.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@Table(name = "estudiante_curso")
public class EstudianteCurso {

    @EmbeddedId
    private EstudianteCursoId id;

    @ManyToOne
    @MapsId("estudianteId")
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne
    @MapsId("cursoId")
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    // Constructor adicional que facilita la creación de EstudianteCurso
    public EstudianteCurso(Estudiante estudiante, Curso curso) {
        this.id = new EstudianteCursoId(estudiante.getId(), curso.getId());
        this.estudiante = estudiante;
        this.curso = curso;
    }

    // Setter para id que puede ser requerido por JPA
    public void setId(EstudianteCursoId id) {
        this.id = id;
    }
}
