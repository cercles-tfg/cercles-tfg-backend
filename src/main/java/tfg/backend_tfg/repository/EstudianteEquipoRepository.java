package tfg.backend_tfg.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tfg.backend_tfg.model.Equipo;
import tfg.backend_tfg.model.EstudianteEquipo;
import tfg.backend_tfg.model.EstudianteEquipoId;

@Repository
public interface EstudianteEquipoRepository extends JpaRepository<EstudianteEquipo, EstudianteEquipoId> {
    List<EstudianteEquipo> findByEquipoId(int equipoId);

    @Query("SELECT ee.equipo FROM EstudianteEquipo ee WHERE ee.estudiante.id = :estudianteId")
    List<Equipo> findEquiposByEstudianteId(@Param("estudianteId") int estudianteId);

    @Query("SELECT ee FROM EstudianteEquipo ee WHERE ee.equipo.curso.id = :cursoId")
    List<EstudianteEquipo> findByCursoId(@Param("cursoId") int cursoId);

    @Query("SELECT ee FROM EstudianteEquipo ee WHERE ee.estudiante.id = :estudianteId AND ee.equipo.curso.id = :cursoId")
    List<EstudianteEquipo> findByEstudianteIdAndCursoId(@Param("estudianteId") int estudianteId, @Param("cursoId") int cursoId);

    @Modifying
    @Query("DELETE FROM EstudianteEquipo ee WHERE ee.equipo.id = :equipoId")
    void deleteByEquipoId(@Param("equipoId") int equipoId);

    @Query("SELECT ee FROM EstudianteEquipo ee WHERE ee.estudiante.id = :estudianteId AND ee.equipo.id = :equipoId")
    Optional<EstudianteEquipo> findByEstudianteIdAndEquipoId(@Param("estudianteId") int estudianteId, @Param("equipoId") int equipoId);

    @Query("SELECT COUNT(ee) FROM EstudianteEquipo ee WHERE ee.equipo.id = :equipoId")
    long countByEquipoId(@Param("equipoId") int equipoId);


}
