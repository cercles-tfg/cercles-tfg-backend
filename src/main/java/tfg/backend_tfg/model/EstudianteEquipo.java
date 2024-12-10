package tfg.backend_tfg.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@Table(name = "estudiante_equipo")
public class EstudianteEquipo {

    @EmbeddedId
    private EstudianteEquipoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idEquipo")
    @JoinColumn(name = "id_equipo")
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idEstudiante")
    @JoinColumn(name = "id_estudiante")
    private Estudiante estudiante;

    // Constructor adicional que facilita la creación de EstudianteCurso
        public EstudianteEquipo(Estudiante estudiante, Equipo equipo) {
            this.id = new EstudianteEquipoId(estudiante.getId(), equipo.getId());
            this.estudiante = estudiante;
            this.equipo = equipo;
        }

    // Getters y Setters
    public EstudianteEquipoId getId() {
        return id;
    }

    public void setId(EstudianteEquipoId id) {
        this.id = id;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    
}

