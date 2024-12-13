package tfg.backend_tfg.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import tfg.backend_tfg.dto.CursoSummaryDTO;
import tfg.backend_tfg.dto.EquipoDTO;
import tfg.backend_tfg.dto.EquipoDetalleDTO;
import tfg.backend_tfg.dto.EquipoSummaryDTO;
import tfg.backend_tfg.dto.EstudianteDTO;
import tfg.backend_tfg.model.Curso;
import tfg.backend_tfg.model.CursoRequest;
import tfg.backend_tfg.model.Equipo;
import tfg.backend_tfg.model.Estudiante;
import tfg.backend_tfg.model.EstudianteCurso;
import tfg.backend_tfg.model.EstudianteRequest;
import tfg.backend_tfg.model.Profesor;
import tfg.backend_tfg.model.ProfesorCurso;
import tfg.backend_tfg.model.ProfesorRequest;
import tfg.backend_tfg.model.Rol;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.model.UsuarioRequest;
import tfg.backend_tfg.repository.CursoRepository;
import tfg.backend_tfg.repository.EquipoRepository;
import tfg.backend_tfg.repository.EstudianteCursoRepository;
import tfg.backend_tfg.repository.EstudianteEquipoRepository;
import tfg.backend_tfg.repository.ProfesorCursoRepository;
import tfg.backend_tfg.repository.UsuarioRepository;
import tfg.backend_tfg.services.CursoService;
import tfg.backend_tfg.services.EquipoService;
import tfg.backend_tfg.services.UsuarioService;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EstudianteCursoRepository estudianteCursoRepository;

    @Autowired
    private ProfesorCursoRepository profesorCursoRepository;

    @Autowired
    private EstudianteEquipoRepository estudianteEquipoRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private CursoService cursoService;

    @PostMapping("/uploadEstudiantes")
    public ResponseEntity<?> uploadEstudiantes(@RequestParam("file") MultipartFile file) {
        try {
            // Verificar si el archivo no está vacío y es un Excel válido
            if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Por favor sube un archivo Excel válido.");
            }

            // Obtener la información de autenticación desde el SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
            }

            // Leer el archivo Excel
            List<Map<String, String>> estudiantes = new ArrayList<>();
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0); // Lee la primera hoja del Excel

            // Iterar sobre las filas del Excel (omitir la primera fila si tiene encabezados)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String cognomsNom = row.getCell(0).getStringCellValue(); // Columna 'Cognoms, Nom'
                    String correo = row.getCell(1).getStringCellValue(); // Columna 'Adreça electronica'

                    // Dividir la cadena "Cognoms, Nom" en apellidos y nombre
                    String[] partes = cognomsNom.split(",");
                    if (partes.length < 2) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato incorrecto en la fila " + (i + 1) + ": no se pudo dividir en apellidos y nombre.");
                    }

                    String apellidos = partes[0].trim(); // Puede tener uno o más apellidos
                    String nombre = partes[1].trim();

                    // Reorganizar el nombre completo como "Nombre Apellidos"
                    String nombreCompleto = nombre + " " + apellidos;

                    // Crear un mapa para representar un estudiante con nombre completo y correo
                    Map<String, String> estudianteData = new HashMap<>();
                    estudianteData.put("nombre", nombreCompleto);
                    estudianteData.put("correo", correo);

                    estudiantes.add(estudianteData);
                }
            }

            workbook.close();

            // Devolver la lista de estudiantes leídos
            return ResponseEntity.ok(estudiantes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el archivo: " + e.getMessage());
        }
    }


    @PreAuthorize("hasAuthority('PROFESOR')")
    @PostMapping("/crear")
    public ResponseEntity<?> crearCurso(@RequestBody CursoRequest cursoRequest) {
        try {
            // Obtener la información de autenticación desde el SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
            }

            // Verificar si ya existe un curso con el mismo nombre, año y cuatrimestre activo
            Optional<Curso> cursoExistente = cursoRepository.findByNombreAsignaturaAndAñoInicioAndCuatrimestreAndActivo(
                    cursoRequest.getNombreAsignatura(), cursoRequest.getAñoInicio(), cursoRequest.getCuatrimestre(), true);
            
            if (cursoExistente.isPresent()) {
                // Si ya existe un curso similar activo, devolver una respuesta especial
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Curso activo ya existente. ¿Desea desactivarlo?");
            }

            // Crear un nuevo curso
            Curso curso = Curso.builder()
                    .nombreAsignatura(cursoRequest.getNombreAsignatura())
                    .añoInicio(cursoRequest.getAñoInicio())
                    .cuatrimestre(cursoRequest.getCuatrimestre())
                    .activo(cursoRequest.isActivo())
                    .build();

            // Guardar el curso en la base de datos
            curso = cursoRepository.save(curso);

            // Añadir profesores al curso usando la función ya existente
            ResponseEntity<?> responseProfesores = añadirProfesores(curso, cursoRequest.getProfesores());
            if (!responseProfesores.getStatusCode().equals(HttpStatus.OK)) {
                return responseProfesores; // En caso de error, devolver la respuesta correspondiente
            }

            // Añadir estudiantes al curso usando la función ya existente
            ResponseEntity<?> responseEstudiantes = añadirEstudiantes(curso, cursoRequest.getEstudiantes());
            if (!responseEstudiantes.getStatusCode().equals(HttpStatus.OK)) {
                return responseEstudiantes; // En caso de error, devolver la respuesta correspondiente
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Curso creado exitosamente");

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

            Optional<Curso> cursoExistente;

            // Intentar buscar el curso por ID si se proporciona en la solicitud
            if (cursoRequest.getId() != null && cursoRequest.getId() != 0) {
                cursoExistente = cursoRepository.findById(cursoRequest.getId());
            }
             else {
                // Buscar el curso por nombreAsignatura, añoInicio y cuatrimestre si no se proporciona ID
                cursoExistente = cursoRepository.findByNombreAsignaturaAndAñoInicioAndCuatrimestreAndActivo(
                        cursoRequest.getNombreAsignatura(), cursoRequest.getAñoInicio(), cursoRequest.getCuatrimestre(), true);
            }

            // Verificar si el curso existe
            if (cursoExistente.isPresent()) {
                Curso curso = cursoExistente.get();

                // Cambiar el estado de activo a su contrario
                curso.setActivo(!curso.isActivo());
                cursoRepository.save(curso);
                    
                String estado = curso.isActivo() ? "activado" : "desactivado";
                return ResponseEntity.ok("Curso " + estado + " exitosamente");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Curso no encontrado");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cambiar el estado del curso: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('PROFESOR')")
    @PostMapping("/verificarCursoExistente")
    public ResponseEntity<?> verificarCursoExistente(@RequestBody CursoRequest cursoRequest) {
        try {
            // Obtener la información de autenticación desde el SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
            }

            Optional<Curso> cursoExistente = cursoRepository.findByNombreAsignaturaAndAñoInicioAndCuatrimestreAndActivo(
                    cursoRequest.getNombreAsignatura(), cursoRequest.getAñoInicio(), cursoRequest.getCuatrimestre(), true);

            if (cursoExistente.isPresent()) {
                // Si ya existe un curso similar activo, devolver una respuesta especial
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Curso activo ya existente. ¿Desea desactivarlo?");
            } else {
                return ResponseEntity.ok("No hay conflicto");
            }

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
            Profesor profesor = (Profesor) usuarioRepository.findByCorreo(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profesor no encontrado"));

            // Obtener los cursos asignados al profesor autenticado desde la relación ProfesorCurso
            List<ProfesorCurso> profesorCursos = profesorCursoRepository.findByProfesorId(profesor.getId());

            // Convertir los cursos en un DTO que también incluya el número de estudiantes en cada curso
            List<CursoSummaryDTO> cursoDTOs = profesorCursos.stream()
                    .map(profesorCurso -> {
                        Curso curso = profesorCurso.getCurso();
                        int numeroEstudiantes = estudianteCursoRepository.countByCursoId(curso.getId());
                        return new CursoSummaryDTO(
                                curso.getId(),
                                curso.getNombreAsignatura(),
                                curso.getAñoInicio(),
                                curso.getCuatrimestre(),
                                curso.isActivo(),
                                numeroEstudiantes
                        );
                    })
                    .toList();

            return ResponseEntity.ok(cursoDTOs);
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
            Profesor profesor = (Profesor) usuarioRepository.findByCorreo(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profesor no encontrado"));

            // Verificar si el profesor está asignado al curso
            Optional<ProfesorCurso> profesorCursoOpt = profesorCursoRepository.findByProfesorIdAndCursoId(profesor.getId(), id);
            if (profesorCursoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tiene acceso a este curso.");
            }

            // Obtener el curso con el id proporcionado
            Curso curso = cursoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

            // Obtener los estudiantes asociados al curso
            List<EstudianteCurso> estudiantesCurso = estudianteCursoRepository.findByCursoId(curso.getId());

            // Obtener IDs de estudiantes que pertenecen a algún equipo
            List<Integer> estudiantesConEquipoIds = estudianteEquipoRepository.findByCursoId(curso.getId()).stream()
                    .map(estudianteEquipo -> estudianteEquipo.getEstudiante().getId())
                    .toList();

            // Filtrar los estudiantes que no pertenecen a ningún equipo
            List<Estudiante> estudiantesSinGrupo = estudiantesCurso.stream()
                    .map(EstudianteCurso::getEstudiante)
                    .filter(estudiante -> !estudiantesConEquipoIds.contains(estudiante.getId()))
                    .toList();

            // Crear listas de nombres y correos de estudiantes sin grupo
            List<String> nombresEstudiantesSinGrupo = estudiantesSinGrupo.stream()
                    .map(Estudiante::getNombre)
                    .toList();
            List<String> correosEstudiantesSinGrupo = estudiantesSinGrupo.stream()
                    .map(Estudiante::getCorreo)
                    .toList();

            // Obtener equipos y sus miembros
            List<Equipo> equipos = equipoRepository.findByCursoId(curso.getId());
            List<EquipoDTO> equiposConMiembros = equipos.stream()
                    .map(equipo -> new EquipoDTO(
                            equipo.getNombre(),
                            equipo.getId(),
                            equipo.getEvaluador().getId(),
                            estudianteEquipoRepository.findByEquipoId(equipo.getId()).stream()
                                    .map(estudianteEquipo -> estudianteEquipo.getEstudiante().getNombre())
                                    .toList()
                    ))
                    .toList();

            // Obtener los nombres de los profesores asociados al curso
            List<ProfesorCurso> profesoresCurso = profesorCursoRepository.findByCursoId(curso.getId());
            List<String> nombresProfesores = profesoresCurso.stream()
                    .map(profesorCurso -> profesorCurso.getProfesor().getNombre())
                    .toList();

            // Crear el DTO con la información detallada
            CursoDetalleDTO cursoDetalleDTO = new CursoDetalleDTO(
                    curso.getId(),
                    curso.getNombreAsignatura(),
                    curso.getAñoInicio(),
                    curso.getCuatrimestre(),
                    curso.isActivo(),
                    nombresEstudiantesSinGrupo,
                    correosEstudiantesSinGrupo,
                    nombresProfesores,
                    equiposConMiembros
            );

            return ResponseEntity.ok(cursoDetalleDTO);

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
            ResponseEntity<?> response = modificarDatosCurso(cursoExistente, cursoRequest);
            if (response.getStatusCode() != HttpStatus.OK) {
                return response;
            }
    
            // Añadir estudiantes al curso
            response = añadirEstudiantes(cursoExistente, cursoRequest.getEstudiantesAñadir());
            if (response.getStatusCode() != HttpStatus.OK) {
                return response;
            }

            // Borrar estudiantes del curso
            borrarEstudiantes(cursoExistente, cursoRequest.getEstudiantesBorrar());
            
            // Añadir profesores al curso
            response = añadirProfesores(cursoExistente, cursoRequest.getProfesoresAñadir());
            if (response.getStatusCode() != HttpStatus.OK) {
                return response;
            }

            // Borrar profesores del curso
            borrarProfesores(cursoExistente, cursoRequest.getProfesoresBorrar());


            // Guardar los cambios finales del curso
            cursoRepository.save(cursoExistente);
    
            return ResponseEntity.ok("Curso modificado exitosamente.");
    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al modificar el curso: " + e.getMessage());
        }
    }
    
    // Función para modificar los datos básicos del curso
    private ResponseEntity<?> modificarDatosCurso(Curso cursoExistente, CursoRequest cursoRequest) {
        if (!cursoExistente.getNombreAsignatura().equals(cursoRequest.getNombreAsignatura()) ||
            cursoExistente.getAñoInicio() != cursoRequest.getAñoInicio() ||
            cursoExistente.getCuatrimestre() != cursoRequest.getCuatrimestre()) {
    
            Optional<Curso> cursoConflicto = cursoRepository.findByNombreAsignaturaAndAñoInicioAndCuatrimestreAndActivo(
                cursoRequest.getNombreAsignatura(), cursoRequest.getAñoInicio(), cursoRequest.getCuatrimestre(), true
            );
    
            if (cursoConflicto.isPresent() && cursoConflicto.get().getId() != (cursoExistente.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe un curso activo con el mismo nombre, año y cuatrimestre.");
            }
        }
    
        // Actualizar los datos básicos
        cursoExistente.setNombreAsignatura(cursoRequest.getNombreAsignatura());
        cursoExistente.setAñoInicio(cursoRequest.getAñoInicio());
        cursoExistente.setCuatrimestre(cursoRequest.getCuatrimestre());
    
        return ResponseEntity.ok().build();
    }
    
    
    // Función para añadir estudiantes al curso
    private ResponseEntity<?> añadirEstudiantes(Curso cursoExistente, List<EstudianteRequest> estudiantesAñadir) {
        for (EstudianteRequest estudianteRequest : estudiantesAñadir) {
            Usuario usuarioExistente = usuarioRepository.findByCorreo(estudianteRequest.getCorreo()).orElse(null);
            if (usuarioExistente == null) {
                // Llamar al servicio para crear el usuario
                UsuarioRequest usuarioRequest = new UsuarioRequest();
                usuarioRequest.setCorreo(estudianteRequest.getCorreo());
                usuarioRequest.setNombre(estudianteRequest.getNombre());
                usuarioRequest.setRol(Rol.Estudiante);

                ResponseEntity<?> response = usuarioService.crearUsuario(usuarioRequest);

                if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error al crear el estudiante: " + estudianteRequest.getCorreo());
                }

                usuarioExistente = usuarioRepository.findByCorreo(estudianteRequest.getCorreo()).orElse(null);
            }
            // Asociar al estudiante con el curso
            EstudianteCurso estudianteCurso = new EstudianteCurso((Estudiante) usuarioExistente, cursoExistente);
            estudianteCursoRepository.save(estudianteCurso);
        }
        return ResponseEntity.ok().build();
    }

    
    
    // Función para borrar estudiantes del curso
    private void borrarEstudiantes(Curso cursoExistente, List<EstudianteRequest> estudiantesBorrar) {
        for (EstudianteRequest estudianteRequest : estudiantesBorrar) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(estudianteRequest.getCorreo());

            if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) usuarioOpt.get();

                // Eliminar la relación entre el estudiante y el curso
                Optional<EstudianteCurso> estudianteCursoOpt = estudianteCursoRepository.findByEstudianteAndCurso(estudiante, cursoExistente);
                if (estudianteCursoOpt.isPresent()) {
                    estudianteCursoRepository.delete(estudianteCursoOpt.get());
                }

                // Verificar si el estudiante no está en ningún otro curso
                List<EstudianteCurso> cursosDelEstudiante = estudianteCursoRepository.findByEstudianteId(estudiante.getId());
                if (cursosDelEstudiante.isEmpty()) {
                    usuarioRepository.delete(estudiante);
                }
            }
        }
    }



    // Función para añadir profesores al curso
    private ResponseEntity<?> añadirProfesores(Curso cursoExistente, List<ProfesorRequest> profesoresAñadir) {
        for (ProfesorRequest profesorRequest : profesoresAñadir) {
            Usuario usuarioExistente = usuarioRepository.findByCorreo(profesorRequest.getCorreo()).orElse(null);
            
            if (usuarioExistente == null) {
                // Crear un nuevo profesor si no existe
                UsuarioRequest usuarioRequest = new UsuarioRequest();
                usuarioRequest.setCorreo(profesorRequest.getCorreo());
                usuarioRequest.setNombre(profesorRequest.getNombre());
                usuarioRequest.setRol(Rol.Profesor);

                ResponseEntity<?> response = usuarioService.crearUsuario(usuarioRequest);
                if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error al crear el profesor: " + profesorRequest.getCorreo());
                }

                usuarioExistente = usuarioRepository.findByCorreo(profesorRequest.getCorreo()).orElse(null);
            }

            // Crear la relación ProfesorCurso si no existe
            Profesor profesor = (Profesor) usuarioExistente;
            Optional<ProfesorCurso> profesorCursoOpt = profesorCursoRepository.findByProfesorIdAndCursoId(profesor.getId(), cursoExistente.getId());
            if (profesorCursoOpt.isEmpty()) {
                ProfesorCurso profesorCurso = new ProfesorCurso(profesor, cursoExistente);
                profesorCursoRepository.save(profesorCurso);
            }
        }
        return ResponseEntity.ok().build();
    }


    // Función para borrar profesores del curso
    private void borrarProfesores(Curso cursoExistente, List<ProfesorRequest> profesoresBorrar) {
        List<Integer> idsProfesoresBorrados = new ArrayList<>();

        for (ProfesorRequest profesorRequest : profesoresBorrar) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(profesorRequest.getCorreo());
            if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Profesor) {
                Profesor profesor = (Profesor) usuarioOpt.get();

                // Buscar la relación ProfesorCurso y eliminarla si existe
                Optional<ProfesorCurso> profesorCursoOpt = profesorCursoRepository.findByProfesorIdAndCursoId(profesor.getId(), cursoExistente.getId());
                if (profesorCursoOpt.isPresent()) {
                    profesorCursoRepository.delete(profesorCursoOpt.get());
                    
                    // Agregar el ID del profesor a la lista de IDs a borrar del curso
                    idsProfesoresBorrados.add(profesor.getId());
                }
            } else {
                System.out.println("El profesor con correo " + profesorRequest.getCorreo() + " no fue encontrado o no es un profesor.");
            }
        }

        // Actualizar la lista de profesores del curso eliminando todos los profesores borrados
        cursoExistente.getProfesores().removeIf(profesor -> idsProfesoresBorrados.contains(profesor.getId()));
    }



    @PreAuthorize("hasAuthority('ESTUDIANTE') or hasAuthority('PROFESOR')") //PERFE
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

    @PreAuthorize("hasAuthority('ESTUDIANTE')") //PERFE
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