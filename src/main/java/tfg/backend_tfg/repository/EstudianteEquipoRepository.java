package tfg.backend_tfg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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

}
