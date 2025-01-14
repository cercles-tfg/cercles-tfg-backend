package tfg.backend_tfg.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@Table(name = "profesor_curso")
public class ProfesorCurso {

    @EmbeddedId
    private ProfesorCursoId id;

    @ManyToOne
    @MapsId("profesorId")
    @JoinColumn(name = "profesor_id", nullable = false)
    private Profesor profesor;

    @ManyToOne
    @MapsId("cursoId")
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    // Constructor adicional que facilita la creación de ProfesorCurso
    public ProfesorCurso(Profesor profesor, Curso curso) {
        this.id = new ProfesorCursoId(profesor.getId(), curso.getId());
        this.profesor = profesor;
        this.curso = curso;
    }

    // Setter para id que puede ser requerido por JPA
    public void setId(ProfesorCursoId id) {
        this.id = id;
    }
}
