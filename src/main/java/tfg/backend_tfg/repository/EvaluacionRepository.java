package tfg.backend_tfg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tfg.backend_tfg.model.Evaluacion;

import java.util.List;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Integer> {
    List<Evaluacion> findByCursoId(Integer cursoId);

    @Query("SELECT e FROM Evaluacion e WHERE e.curso.id IN (SELECT ec.curso.id FROM Equipo ec WHERE ec.id = :equipoId)")
    List<Evaluacion> findByEquipoId(@Param("equipoId") Integer equipoId);
}