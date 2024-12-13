package tfg.backend_tfg.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import tfg.backend_tfg.dto.EstudianteDTO;
import tfg.backend_tfg.model.EstudianteCurso;
import tfg.backend_tfg.model.Profesor;
import tfg.backend_tfg.repository.EstudianteCursoRepository;
import tfg.backend_tfg.repository.EstudianteEquipoRepository;
import tfg.backend_tfg.repository.ProfesorCursoRepository;

@Service
public class CursoService {

    @Autowired
    private EstudianteCursoRepository estudianteCursoRepository;

    @Autowired
    private EstudianteEquipoRepository estudianteEquipoRepository;

    @Autowired
    private ProfesorCursoRepository profesorCursoRepository;

    public Map<String, List<EstudianteDTO>> obtenerEstudiantesPorCurso(int cursoId) {
        // Obtener todos los estudiantes del curso
        List<EstudianteCurso> estudiantesCurso = estudianteCursoRepository.findByCursoId(cursoId);
    
        // Crear listas separadas para estudiantes con equipo y sin equipo
        List<EstudianteDTO> estudiantesConEquipo = new ArrayList<>();
        List<EstudianteDTO> estudiantesSinEquipo = new ArrayList<>();
    
        for (EstudianteCurso ec : estudiantesCurso) {
            int estudianteId = ec.getEstudiante().getId();
            boolean tieneEquipo = !estudianteEquipoRepository.findByEstudianteIdAndCursoId(estudianteId, cursoId).isEmpty();
    
            String nombre = ec.getEstudiante().getNombre();
            String correo = ec.getEstudiante().getCorreo();
    
            EstudianteDTO estudianteDTO = new EstudianteDTO(estudianteId, nombre, correo);
    
            if (tieneEquipo) {
                estudiantesConEquipo.add(estudianteDTO);
            } else {
                estudiantesSinEquipo.add(estudianteDTO);
            }
        }
    
        // Crear el mapa de respuesta
        Map<String, List<EstudianteDTO>> respuesta = new HashMap<>();
        respuesta.put("conEquipo", estudiantesConEquipo);
        respuesta.put("sinEquipo", estudiantesSinEquipo);
    
        return respuesta;
    }
    

    public List<EstudianteDTO> obtenerProfesoresDelCurso(int cursoId) {
        // Obtener la lista de profesores del curso
        return profesorCursoRepository.findByCursoId(cursoId).stream()
                .map(profesorCurso -> {
                    Profesor profesor = profesorCurso.getProfesor();
                    return new EstudianteDTO(
                            profesor.getId(),
                            profesor.getNombre(),
                            profesor.getCorreo()
                    );
                })
                .toList();
    }
}
