package tfg.backend_tfg.controller;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tfg.backend_tfg.dto.CrearEquipoDTO;
import tfg.backend_tfg.model.Equipo;
import tfg.backend_tfg.model.Estudiante;
import tfg.backend_tfg.repository.EstudianteRepository;
import tfg.backend_tfg.repository.UsuarioRepository;
import tfg.backend_tfg.services.EquipoService;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {
    
    @Autowired
    private EquipoService equipoService;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PreAuthorize("hasAuthority('ESTUDIANTE')")
    @PostMapping("/crear")
    public ResponseEntity<String> crearEquipo(@RequestBody CrearEquipoDTO crearEquipoDTO) {
        // Validar autenticación del usuario
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
        }
        System.out.println("Datos recibidos:");
    System.out.println("Nombre: " + crearEquipoDTO.getNombre());
    System.out.println("Curso ID: " + crearEquipoDTO.getCursoId());
    System.out.println("Evaluador ID: " + crearEquipoDTO.getEvaluadorId());
    System.out.println("Estudiantes IDs: " + crearEquipoDTO.getEstudiantesIds());
        
        // Obtener el correo electrónico del usuario autenticado
        String correo = authentication.getName();

        // Buscar el ID del estudiante por correo
        int estudianteId = usuarioRepository.findByCorreo(correo).get().getId();
            //.orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado o no es un estudiante."));

        // Validar que el estudiante pertenece al curso
        try {
            System.out.println("datos: " + crearEquipoDTO.getCursoId());
            equipoService.validarEstudianteCurso(estudianteId, crearEquipoDTO.getCursoId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        // Crear el equipo
        try {
            equipoService.crearEquipo(
                crearEquipoDTO.getNombre(),
                crearEquipoDTO.getCursoId(),
                crearEquipoDTO.getEvaluadorId(),
                crearEquipoDTO.getEstudiantesIds()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body("Equipo creado exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el equipo: " + e.getMessage());
        }
    }
}
