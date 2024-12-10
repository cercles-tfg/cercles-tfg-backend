package tfg.backend_tfg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tfg.backend_tfg.model.Equipo;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Integer> {
    boolean existsByNombreAndCursoId(String nombre, int cursoId);

    List<Equipo> findByCursoId(int cursoId);

    List<Equipo> findByEvaluadorId(int evaluadorId);
}

