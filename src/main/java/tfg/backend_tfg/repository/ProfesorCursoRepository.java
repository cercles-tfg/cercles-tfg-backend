package tfg.backend_tfg.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tfg.backend_tfg.model.Curso;
import tfg.backend_tfg.model.Profesor;
import tfg.backend_tfg.model.ProfesorCurso;
import tfg.backend_tfg.model.ProfesorCursoId;

@Repository
public interface ProfesorCursoRepository extends JpaRepository<ProfesorCurso, ProfesorCursoId> {

    // Buscar profesores por curso utilizando el identificador del curso en la clave compuesta
    List<ProfesorCurso> findByCursoId(int cursoId);

    // Buscar cursos por profesor utilizando el identificador del profesor en la clave compuesta
    List<ProfesorCurso> findByProfesorId(int profesorId);

    // Contar cuántos profesores están asociados a un curso específico
    int countByCursoId(int cursoId);

    // Buscar relación curso profesor por ids
    Optional<ProfesorCurso> findByProfesorIdAndCursoId(int id1, int id2);

    boolean existsByProfesorIdAndCursoId(int evaluadorId, int cursoId);

    
}
