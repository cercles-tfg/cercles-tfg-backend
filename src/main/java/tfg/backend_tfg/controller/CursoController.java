package tfg.backend_tfg.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import tfg.backend_tfg.dto.CursoDetalleDTO;
import tfg.backend_tfg.dto.EquipoSummaryDTO;
import tfg.backend_tfg.dto.EstudianteDTO;
import tfg.backend_tfg.model.Curso;
import tfg.backend_tfg.model.CursoRequest;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.repository.CursoRepository;
import tfg.backend_tfg.repository.EstudianteCursoRepository;
import tfg.backend_tfg.repository.UsuarioRepository;
import tfg.backend_tfg.services.CursoService;
import tfg.backend_tfg.services.EquipoService;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteCursoRepository estudianteCursoRepository;

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private CursoService cursoService;

    @PreAuthorize("hasAuthority('PROFESOR')")
    @PostMapping("/uploadEstudiantes")
    public ResponseEntity<?> uploadEstudiantes(@RequestParam("file") MultipartFile file) {
        try {
            // Validar autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
            }
            return cursoService.uploadEstudiantes(file);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el archivo: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('PROFESOR')")
    @PostMapping("/crear")
    public ResponseEntity<?> crearCurso(@RequestBody CursoRequest cursoRequest) {
        try {
            // Validar autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
            }

            // Llamar al servicio para manejar la lógica de creación
            String profesorEmail = authentication.getName(); // Obtener el email del profesor autenticado
            ResponseEntity<?> response = cursoService.crearCurso(cursoRequest, profesorEmail);

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el curso: " + e.getMessage());
        }
    }



    @PreAuthorize("hasAuthority('PROFESOR')")
    @PostMapping("/cambiarEstado")
    public ResponseEntity<?> cambiarEstadoCurso(@RequestBody CursoRequest cursoRequest) {
        try {
            // Obtener la información de autenticación desde el SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
            }
            return cursoService.cambiarEstadoCurso(cursoRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cambiar el estado del curso: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('PROFESOR')")
    @PostMapping("/verificarCursoExistente")
    public ResponseEntity<?> verificarCursoExistente(@RequestBody CursoRequest cursoRequest) {
        try {
            return cursoService.verificarCursoExistente(cursoRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al verificar el curso existente: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROFESOR')")
    public ResponseEntity<?> obtenerCursosProfesor() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
            }
            String email = authentication.getName();
            return ResponseEntity.ok(cursoService.obtenerCursosProfesor(email));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener los cursos.");
        }
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROFESOR')")
    public ResponseEntity<?> obtenerDetalleCurso(@PathVariable Integer id) {
        try {
            // Verificar autenticación del usuario
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
            }

            String email = authentication.getName();

            CursoDetalleDTO cursoDetalle = cursoService.obtenerDetalleCurso(id, email);

            return ResponseEntity.ok(cursoDetalle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener los detalles del curso.");
        }
    }




    @PreAuthorize("hasAuthority('PROFESOR')")
    @PutMapping("/{id}/modificar_curso")
    public ResponseEntity<?> modificarCurso(@PathVariable Integer id, @RequestBody CursoRequest cursoRequest) {
        try {
            // Validar autenticación del usuario
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
            }
    
            // Buscar el curso por ID
            Optional<Curso> cursoExistenteOpt = cursoRepository.findById(id);
            if (!cursoExistenteOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Curso no encontrado.");
            }
    
            Curso cursoExistente = cursoExistenteOpt.get();
    
            // Modificar datos básicos del curso
            ResponseEntity<?> response = cursoService.modificarDatosCurso(cursoExistente, cursoRequest);
            if (response.getStatusCode() != HttpStatus.OK) {
                return response;
            }
    
            // Añadir estudiantes al curso
            response = cursoService.añadirEstudiantes(cursoExistente, cursoRequest.getEstudiantesAñadir());
            if (response.getStatusCode() != HttpStatus.OK) {
                return response;
            }

            // Borrar estudiantes del curso
            cursoService.borrarEstudiantes(cursoExistente, cursoRequest.getEstudiantesBorrar());
            
            // Añadir profesores al curso
            response = cursoService.añadirProfesores(cursoExistente, cursoRequest.getProfesoresAñadir());
            if (response.getStatusCode() != HttpStatus.OK) {
                return response;
            }

            // Borrar profesores del curso
            cursoService.borrarProfesores(cursoExistente, cursoRequest.getProfesoresBorrar());


            // Guardar los cambios finales del curso
            cursoRepository.save(cursoExistente);
    
            return ResponseEntity.ok("Curso modificado exitosamente.");
    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al modificar el curso: " + e.getMessage());
        }
    }



    @PreAuthorize("hasAuthority('ESTUDIANTE') or hasAuthority('PROFESOR')")
    @GetMapping("/{cursoId}/equipos")
    public ResponseEntity<List<EquipoSummaryDTO>> obtenerEquiposPorCurso(@PathVariable int cursoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        try {
            // Llamar al servicio para obtener los equipos como DTOs
            List<EquipoSummaryDTO> equipos = equipoService.obtenerEquiposPorCurso(cursoId);
            return ResponseEntity.ok(equipos);
        } catch (IllegalArgumentException e) {
            // Manejar errores específicos
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            // Manejar errores genéricos
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PreAuthorize("hasAuthority('ESTUDIANTE')")
    @GetMapping("/{id}/estudiantes") 
    public ResponseEntity<Map<String, List<EstudianteDTO>>> obtenerEstudiantesPorCurso(@PathVariable int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        // Obtener el ID del usuario desde el token JWT
        String correoAutenticado = authentication.getName();
        Usuario usuarioAutenticado = usuarioRepository.findByCorreo(correoAutenticado)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado."));

        // Verificar que el usuario autenticado es parte del curso
        boolean estaEnCurso = estudianteCursoRepository.findByEstudianteIdAndCursoId(usuarioAutenticado.getId(), id)
                .isPresent();
        if (!estaEnCurso) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        try {
            // Delegar la lógica al servicio
            Map<String, List<EstudianteDTO>> estudiantesPorCurso = cursoService.obtenerEstudiantesPorCurso(id);
            return ResponseEntity.ok(estudiantesPorCurso);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PreAuthorize("hasAuthority('ESTUDIANTE')")
    @GetMapping("/{id}/profesores")
    public ResponseEntity<List<EstudianteDTO>> obtenerProfesoresDelCurso(@PathVariable int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        // Obtener el ID del usuario desde el token JWT
        String correoAutenticado = authentication.getName();
        Usuario usuarioAutenticado = usuarioRepository.findByCorreo(correoAutenticado)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado."));

        // Comprobar que el usuario es estudiante del curso
        boolean esEstudianteDelCurso = estudianteCursoRepository.existsByEstudianteIdAndCursoId(usuarioAutenticado.getId(), id);
        if (!esEstudianteDelCurso) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        try {
            List<EstudianteDTO> profesores = cursoService.obtenerProfesoresDelCurso(id);
            return ResponseEntity.ok(profesores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasAuthority('PROFESOR')")
    @DeleteMapping("/{id}/borrar")
    public ResponseEntity<String> borrarCurso(@PathVariable int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
        }
        try {
            cursoService.borrarCurso(id);
            return ResponseEntity.ok("Curso borrado exitosamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al borrar el curso.");
        }
    }


}