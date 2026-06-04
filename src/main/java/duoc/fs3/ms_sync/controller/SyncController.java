package duoc.fs3.ms_sync.controller;

import duoc.fs3.ms_sync.model.ClothingItem;
import duoc.fs3.ms_sync.model.Wardrobe;
import duoc.fs3.ms_sync.service.ImageStorageService;
import duoc.fs3.ms_sync.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;
    private final ImageStorageService imageStorageService;

    /**
     * Endpoint para sincronizar/exportar datos a la nube.
     * El Principal es inyectado automáticamente por Spring Security tras validar el JWT.
     */
    @PostMapping("/export")
    public ResponseEntity<Wardrobe> exportAndSync(
            @RequestBody List<ClothingItem> localItems,
            Principal principal) {
        
        // Extraemos el userId de forma segura desde el token JWT (Principal)
        String userId = principal.getName(); 
        
        Wardrobe syncedWardrobe = syncService.syncWithCloud(userId, localItems);
        return ResponseEntity.ok(syncedWardrobe);
    }

    /**
     * Endpoint para subir una imagen individual
     */
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("image") MultipartFile file,
            Principal principal) {
        
        String imageUrl = syncService.uploadImage(file);
        return ResponseEntity.ok(Map.of("url", imageUrl));
    }

    /**
     * Endpoint para eliminar una prenda del ropero
     */
    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<Map<String, String>> deleteItem(
            @PathVariable Long itemId,
            Principal principal) {

        String userId = principal.getName();
        syncService.deleteClothingItem(userId, itemId);
        return ResponseEntity.ok(Map.of("message", "Prenda eliminada exitosamente"));
    }

    /**
     * Endpoint para que el dispositivo descargue la última versión de la nube
     */
    @GetMapping("/download")
    public ResponseEntity<Wardrobe> downloadCloudData(Principal principal) {
        
        String userId = principal.getName();
        
        Wardrobe cloudWardrobe = syncService.getCloudWardrobe(userId);
        return ResponseEntity.ok(cloudWardrobe);
    }

    /**
     * Endpoint para servir una imagen guardada.
     */
    @GetMapping("/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = imageStorageService.loadImage(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
