package duoc.fs3.ms_sync.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${app.storage.location}")
    private String storageLocation;

    private Path rootLocation;

    /**
     * Inicializa el directorio de almacenamiento
     */
    public void init() {
        try {
            this.rootLocation = Paths.get(storageLocation);
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento", e);
        }
    }

    /**
     * Guarda una imagen y retorna la URL de acceso
     * @param file Archivo de imagen
     * @return URL de la imagen guardada
     */
    public String storeImage(MultipartFile file) {
        if (rootLocation == null) {
            init();
        }

        try {
            // Validar que sea una imagen
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Solo se permiten archivos de imagen");
            }

            // Generar nombre único
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID().toString() + "." + extension;

            // Guardar archivo
            Path targetLocation = rootLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Retornar URL relativa para acceso
            return "/api/v1/sync/images/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen", e);
        }
    }

    /**
     * Elimina una imagen por su nombre de archivo
     * @param fileName Nombre del archivo a eliminar
     */
    public void deleteImage(String fileName) {
        try {
            if (rootLocation == null) {
                init();
            }
            Path filePath = rootLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar la imagen", e);
        }
    }

    /**
     * Obtiene la ruta completa de una imagen
     * @param fileName Nombre del archivo
     * @return Ruta completa del archivo
     */
    public Path loadImage(String fileName) {
        if (rootLocation == null) {
            init();
        }
        return rootLocation.resolve(fileName).normalize();
    }

    /**
     * Extrae el nombre de archivo de una URL
     * @param imageUrl URL de la imagen
     * @return Nombre del archivo
     */
    public String extractFileName(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        String[] parts = imageUrl.split("/");
        return parts[parts.length - 1];
    }
}
