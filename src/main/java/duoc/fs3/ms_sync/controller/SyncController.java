package duoc.fs3.ms_sync.controller;

import duoc.fs3.ms_sync.model.ClothingItem;
import duoc.fs3.ms_sync.model.Wardrobe;
import duoc.fs3.ms_sync.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

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
     * Endpoint para que el dispositivo descargue la última versión de la nube
     */
    @GetMapping("/download")
    public ResponseEntity<Wardrobe> downloadCloudData(Principal principal) {
        
        String userId = principal.getName();
        
        Wardrobe cloudWardrobe = syncService.getCloudWardrobe(userId);
        return ResponseEntity.ok(cloudWardrobe);
    }
}
