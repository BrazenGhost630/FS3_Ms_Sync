package duoc.fs3.ms_sync.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class ImageStorageService {

    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.presignedUrlExpirationMinutes}")
    private int presignedUrlExpirationMinutes;

    public ImageStorageService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Guarda una imagen en S3 y retorna la URL presigned para acceso
     * @param file Archivo de imagen
     * @return URL presigned de la imagen guardada
     */
    public String storeImage(MultipartFile file) {
        try {
            // Validar que sea una imagen
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Solo se permiten archivos de imagen");
            }

            // Generar nombre único
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID().toString() + "." + extension;

            // Configurar metadata del objeto
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(file.getSize());

            // Subir archivo a S3
            PutObjectRequest putRequest = new PutObjectRequest(
                bucketName,
                uniqueFileName,
                file.getInputStream(),
                metadata
            );
            s3Client.putObject(putRequest);

            // Generar y retornar URL presigned
            return generatePresignedUrl(uniqueFileName);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen en S3", e);
        }
    }

    /**
     * Elimina una imagen de S3 por su nombre de archivo
     * @param fileName Nombre del archivo a eliminar
     */
    public void deleteImage(String fileName) {
        try {
            DeleteObjectRequest deleteRequest = new DeleteObjectRequest(bucketName, fileName);
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar la imagen de S3", e);
        }
    }

    /**
     * Genera una URL presigned para acceder a una imagen en S3
     * @param fileName Nombre del archivo
     * @return URL presigned para acceso temporal
     */
    public String loadImage(String fileName) {
        return generatePresignedUrl(fileName);
    }

    /**
     * Genera una URL presigned para un archivo en S3
     * @param fileName Nombre del archivo
     * @return URL presigned
     */
    private String generatePresignedUrl(String fileName) {
        Date expiration = new Date(System.currentTimeMillis() + 
            presignedUrlExpirationMinutes * 60 * 1000L);
        
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(
            bucketName,
            fileName
        );
        urlRequest.setExpiration(expiration);
        
        URL url = s3Client.generatePresignedUrl(urlRequest);
        return url.toString();
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
