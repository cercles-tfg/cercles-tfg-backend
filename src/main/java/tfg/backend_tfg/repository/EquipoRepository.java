package tfg.backend_tfg.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tfg.backend_tfg.model.Curso;
import tfg.backend_tfg.model.Equipo;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Integer> {
    boolean existsByNombreAndCursoId(String nombre, int cursoId);

    List<Equipo> findByCursoId(int cursoId);
    
    @Query("SELECT e.curso FROM Equipo e WHERE e.id = :equipoId")
    Curso findByEquipoId(@Param("equipoId") int equipoId);

    List<Equipo> findByEvaluadorId(int evaluadorId);

}

