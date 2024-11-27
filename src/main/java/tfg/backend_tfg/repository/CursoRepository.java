package tfg.backend_tfg.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tfg.backend_tfg.model.Curso;
import tfg.backend_tfg.model.Profesor;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer> {
    Optional<Curso> findByNombreAsignaturaAndAñoInicioAndCuatrimestreAndActivo(
            String nombreAsignatura, int añoInicio, int cuatrimestre, boolean activo);

    List<Curso> findAllByProfesoresContaining(Profesor profesor);
}
