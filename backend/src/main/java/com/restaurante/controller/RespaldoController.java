package com.restaurante.controller;

import com.restaurante.service.RespaldoService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@RestController
@RequestMapping("/api/respaldo")
public class RespaldoController {

    private final RespaldoService respaldoService;
    private final ConfigurableApplicationContext applicationContext;
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;

    public RespaldoController(RespaldoService respaldoService,
                              ConfigurableApplicationContext applicationContext,
                              DataSource dataSource,
                              EntityManagerFactory entityManagerFactory) {
        this.respaldoService = respaldoService;
        this.applicationContext = applicationContext;
        this.dataSource = dataSource;
        this.entityManagerFactory = entityManagerFactory;
    }

    @GetMapping("/exportar")
    public ResponseEntity<Resource> exportar(@RequestParam(defaultValue = "false") boolean incluirImagenes) throws IOException {
        Path zipPath = respaldoService.exportar(incluirImagenes);
        Resource resource = new FileSystemResource(zipPath);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + zipPath.getFileName().toString() + "\"")
                .body(resource);
    }

    @PostMapping("/importar")
    public ResponseEntity<Map<String, String>> importar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Archivo vacío"));
        }

        try {
            Path tempZip = Files.createTempFile("respaldo_import_", ".zip");
            Files.copy(file.getInputStream(), tempZip, StandardCopyOption.REPLACE_EXISTING);

            entityManagerFactory.close();

            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                ((com.zaxxer.hikari.HikariDataSource) dataSource).close();
            }

            respaldoService.importar(tempZip);

            Files.deleteIfExists(tempZip);

            new Thread(this::reiniciarAplicacion).start();

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Datos importados exitosamente. La aplicación se está reiniciando..."
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al importar: " + e.getMessage()));
        }
    }

    private void reiniciarAplicacion() {
        try {
            Path jarPath = Paths.get("target", "restaurante-backend-1.0.0.jar").toAbsolutePath();

            if (!Files.exists(jarPath)) {
                applicationContext.close();
                return;
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "java", "-jar", jarPath.toString(),
                    "--server.port=8080"
            );
            pb.directory(new File(System.getProperty("user.dir")));
            pb.inheritIO();

            Process nuevoProceso = pb.start();

            Thread.sleep(3000);

            if (nuevoProceso.isAlive()) {
                applicationContext.close();
            }
        } catch (Exception e) {
            applicationContext.close();
        }
    }
}
