package tfg.backend_tfg.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import tfg.backend_tfg.model.Curso;
import tfg.backend_tfg.model.Equipo;
import tfg.backend_tfg.model.Estudiante;
import tfg.backend_tfg.model.EstudianteCursoId;
import tfg.backend_tfg.model.EstudianteEquipo;
import tfg.backend_tfg.model.EstudianteEquipoId;
import tfg.backend_tfg.model.Profesor;
import tfg.backend_tfg.model.ProfesorCurso;
import tfg.backend_tfg.repository.CursoRepository;
import tfg.backend_tfg.repository.EquipoRepository;
import tfg.backend_tfg.repository.EstudianteCursoRepository;
import tfg.backend_tfg.repository.EstudianteEquipoRepository;
import tfg.backend_tfg.repository.EstudianteRepository;
import tfg.backend_tfg.repository.ProfesorCursoRepository;
import tfg.backend_tfg.repository.ProfesorRepository;
import tfg.backend_tfg.repository.UsuarioRepository;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private EstudianteEquipoRepository estudianteEquipoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private ProfesorCursoRepository profesorCursoRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private EstudianteCursoRepository estudianteCursoRepository;

    public void validarEstudianteCurso(int estudianteId, int cursoId) {
        // Crear la clave compuesta
        EstudianteCursoId id = new EstudianteCursoId(estudianteId, cursoId);
    
        // Verificar si existe la relación entre estudiante y curso
        boolean perteneceAlCurso = estudianteCursoRepository.existsById(id);
    
        if (!perteneceAlCurso) {
            throw new IllegalArgumentException("El estudiante no pertenece al curso especificado.");
        }
    }
    

    public Equipo crearEquipo(String nombre, int cursoId, int evaluadorId, List<Integer> estudiantesIds) {
        try {
            // Validar que el nombre del equipo sea único en el curso
        if (equipoRepository.existsByNombreAndCursoId(nombre, cursoId)) {
            throw new IllegalArgumentException("Ya existe un equipo con este nombre en el curso.");
        }

        // Validar que el curso existe
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("El curso especificado no existe."));

        // Validar que el profesor existe y pertenece al curso
        
        Profesor profesor_evaluador = profesorRepository.findById(evaluadorId)
            .orElseThrow(() -> new IllegalArgumentException("El profesor especificado no existe."));
        Optional<ProfesorCurso> profesorCursoOpt = profesorCursoRepository.findByProfesorIdAndCursoId(evaluadorId, cursoId);
            if (profesorCursoOpt.isEmpty()) {
                throw new IllegalArgumentException("El profesor no pertenece al curso.");
            }
        

        // Crear el equipo
        Equipo equipo = new Equipo();
        equipo.setNombre(nombre);
        equipo.setCurso(curso);
        equipo.setEvaluador(profesor_evaluador);
        equipo.setValidado(false); // Inicialmente no validado
        equipo = equipoRepository.save(equipo);

        // Asociar los estudiantes al equipo
        for (int estudianteId : estudiantesIds) {
            Estudiante estudiante = estudianteRepository.findById(estudianteId)
                    .orElseThrow(() -> new IllegalArgumentException("El estudiante especificado no existe."));
            if (!curso.getEstudiantes().contains(estudiante)) {
                throw new IllegalArgumentException("El estudiante no pertenece al curso.");
            }

            EstudianteEquipo estudianteEquipo = new EstudianteEquipo();
            estudianteEquipo.setId(new EstudianteEquipoId(equipo.getId(), estudiante.getId()));
            estudianteEquipo.setEquipo(equipo);
            estudianteEquipo.setEstudiante(estudiante);
            estudianteEquipoRepository.save(estudianteEquipo);
        }

        return equipo;
        } catch (DataIntegrityViolationException ex) {
            // Gestionar errores de los triggers de la base de datos
            throw new IllegalArgumentException("Error de base de datos: " + ex.getRootCause().getMessage());
        }
        
    }
}
