package com.restaurante.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class RespaldoService {

    private static final String UPLOADS_DIR = "uploads";

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDirPath;

    @Value("${app.backup.dir}")
    private String backupsDir;

    private String getDbFilePath() {
        return datasourceUrl.replace("jdbc:sqlite:", "");
    }

    private String getDbFileName() {
        return Paths.get(getDbFilePath()).getFileName().toString();
    }

    public Path exportar(boolean incluirImagenes) throws IOException {
        Path exportDir = Paths.get(backupsDir);
        Files.createDirectories(exportDir);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm"));
        Path zipPath = exportDir.resolve("respaldo_" + timestamp + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            Path dbPath = Paths.get(getDbFilePath());
            if (Files.exists(dbPath)) {
                ZipEntry entry = new ZipEntry(getDbFileName());
                zos.putNextEntry(entry);
                Files.copy(dbPath, zos);
                zos.closeEntry();
            }

            if (incluirImagenes) {
                Path uploadsPath = Paths.get(uploadDirPath);
                if (Files.exists(uploadsPath) && Files.isDirectory(uploadsPath)) {
                    Files.walkFileTree(uploadsPath, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Path relativePath = uploadsPath.relativize(file);
                            ZipEntry entry = new ZipEntry(UPLOADS_DIR + "/" + relativePath.toString().replace("\\", "/"));
                            zos.putNextEntry(entry);
                            Files.copy(file, zos);
                            zos.closeEntry();
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
        }

        return zipPath;
    }

    public void importar(Path zipPath) throws IOException {
        Path tempDir = Files.createTempDirectory("respaldo_import_");

        try {
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path outPath = tempDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(outPath);
                    } else {
                        Files.createDirectories(outPath.getParent());
                        Files.copy(zis, outPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zis.closeEntry();
                }
            }

            Path importedDb = null;
            Path importedUploads = tempDir.resolve(UPLOADS_DIR);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDir)) {
                for (Path p : stream) {
                    String name = p.getFileName().toString();
                    if (name.endsWith(".db") || name.equalsIgnoreCase(getDbFileName())) {
                        importedDb = p;
                        break;
                    }
                }
            }

            if (importedDb == null) {
                throw new RuntimeException("El archivo ZIP no contiene un archivo de base de datos (.db)");
            }

            if (!validarSqlite(importedDb)) {
                throw new RuntimeException("El archivo proporcionado no es una base de datos SQLite válida");
            }

            respaldarActual();

            Files.copy(importedDb, Paths.get(getDbFilePath()), StandardCopyOption.REPLACE_EXISTING);

            if (Files.exists(importedUploads) && Files.isDirectory(importedUploads)) {
                Path uploadsDest = Paths.get(uploadDirPath);
                Files.createDirectories(uploadsDest);

                Files.walkFileTree(importedUploads, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path relativePath = importedUploads.relativize(file);
                        Path destFile = uploadsDest.resolve(relativePath);

                        if (!Files.exists(destFile)) {
                            Files.createDirectories(destFile.getParent());
                            Files.copy(file, destFile);
                        }

                        return FileVisitResult.CONTINUE;
                    }
                });
            }

        } finally {
            try {
                deleteRecursively(tempDir);
            } catch (IOException ignored) {}
        }
    }

    private boolean validarSqlite(Path dbPath) {
        try {
            byte[] header = new byte[16];
            try (InputStream is = new FileInputStream(dbPath.toFile())) {
                int read = is.read(header);
                if (read < 16) return false;
            }
            String magic = new String(header, 0, 16, java.nio.charset.StandardCharsets.US_ASCII);
            return magic.startsWith("SQLite format 3");
        } catch (IOException e) {
            return false;
        }
    }

    private void respaldarActual() throws IOException {
        Path dbPath = Paths.get(getDbFilePath());
        if (!Files.exists(dbPath)) return;

        Path backupDir = Paths.get(backupsDir);
        Files.createDirectories(backupDir);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
        Path backupPath = backupDir.resolve("backup_antes_importacion_" + timestamp + ".db");

        Files.copy(dbPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            File[] files = path.toFile().listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteRecursively(f.toPath());
                }
            }
        }
        Files.deleteIfExists(path);
    }
}
