package tfg.backend_tfg.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@Table(name = "curso")
public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nombre_asignatura", nullable = false)
    private String nombreAsignatura;

    @Column(name = "año_inicio", nullable = false)
    private int añoInicio;

    @Column(nullable = false)
    private int cuatrimestre;

    @Column(nullable = false)
    private boolean activo;

    // Relación con profesores a través de ProfesorCurso
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfesorCurso> profesores;

    // Relación muchos a muchos con estudiantes a través de EstudianteCurso
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstudianteCurso> estudiantes;
}
