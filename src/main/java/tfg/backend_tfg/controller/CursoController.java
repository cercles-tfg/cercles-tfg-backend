package tfg.backend_tfg.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import tfg.backend_tfg.model.Estudiante;
import tfg.backend_tfg.model.Rol;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.repository.UsuarioRepository;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private UsuarioRepository usuarioRepository;
    
        @PostMapping("/uploadEstudiantes")
        public ResponseEntity<?> uploadEstudiantes(@RequestParam("file") MultipartFile file) {
            try {
                // Verificar si el archivo no está vacío y es un Excel válido
                if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Por favor sube un archivo Excel válido.");
                }
    
                // Leer el archivo Excel
                List<Estudiante> estudiantes = new ArrayList<>();
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
                        String apellidos = partes[0].trim();
                        String nombre = partes.length > 1 ? partes[1].trim() : "";
    
                        // Crear un nuevo estudiante con los datos leídos
                        Usuario usuario = Estudiante.builder()
                        .nombre(nombre + " " + apellidos)
                        .correo(correo)
                        .rol(Rol.Estudiante)
                        .build();
    
                        usuarioRepository.save(usuario);
                }
            }

            workbook.close();
            // Aquí podrías guardar los estudiantes en la base de datos, o asociarlos con el curso

            return ResponseEntity.ok("Archivo procesado correctamente. Estudiantes leídos: " + estudiantes.size());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el archivo: " + e.getMessage());
        }
    }
}
