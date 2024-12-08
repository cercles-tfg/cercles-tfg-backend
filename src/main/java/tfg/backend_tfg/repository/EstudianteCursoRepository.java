package tfg.backend_tfg.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tfg.backend_tfg.model.Curso;
import tfg.backend_tfg.model.Estudiante;
import tfg.backend_tfg.model.EstudianteCurso;
import tfg.backend_tfg.model.EstudianteCursoId;

@Repository
public interface EstudianteCursoRepository extends JpaRepository<EstudianteCurso, EstudianteCursoId> {

    // Buscar estudiantes por curso utilizando el identificador del curso en la clave compuesta
    List<EstudianteCurso> findByCursoId(int cursoId);

    // Buscar cursos por estudiante utilizando el identificador del estudiante en la clave compuesta
    List<EstudianteCurso> findByEstudianteId(int estudianteId);

    // Contar cuántos estudiantes están asociados a un curso específico
    int countByCursoId(int cursoId);

    // Buscar relación curso estudiante
    Optional<EstudianteCurso> findByEstudianteAndCurso(Estudiante estudiante, Curso cursoExistente);
}
