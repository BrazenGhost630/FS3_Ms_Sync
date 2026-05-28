package duoc.fs3.ms_sync.service;

import duoc.fs3.ms_sync.model.ClothingItem;
import duoc.fs3.ms_sync.model.Wardrobe;
import duoc.fs3.ms_sync.repository.WardrobeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SyncService {

    private final WardrobeRepository wardrobeRepository;

    public SyncService(WardrobeRepository wardrobeRepository) {
        this.wardrobeRepository = wardrobeRepository;
    }

    /**
     * Sincronización bidireccional basada en Timestamp (updatedAt)
     */
    public Wardrobe syncWithCloud(String userId, List<ClothingItem> localItems) {
        // 1. Obtener el ropero de la nube o crear uno nuevo si no existe
        Wardrobe cloudWardrobe = wardrobeRepository.findByUserId(userId)
                .orElse(new Wardrobe(userId, Instant.now(), new ArrayList<>()));

        // 2. Mapear los items de la nube por ID para una búsqueda rápida
        Map<String, ClothingItem> mergedItems = cloudWardrobe.getItems().stream()
                .collect(Collectors.toMap(ClothingItem::getId, item -> item));

        // 3. Resolución de conflictos: Comparar timestamps
        for (ClothingItem localItem : localItems) {
            ClothingItem cloudItem = mergedItems.get(localItem.getId());
            
            // Si el item no existe en la nube, o si el local es más reciente, gana el local
            if (cloudItem == null || localItem.getUpdatedAt().isAfter(cloudItem.getUpdatedAt())) {
                mergedItems.put(localItem.getId(), localItem);
            }
        }

        // 4. Actualizar el ropero con la lista fusionada y el nuevo timestamp de sincronización
        cloudWardrobe.setItems(new ArrayList<>(mergedItems.values()));
        cloudWardrobe.setLastSync(Instant.now());

        // 5. Guardar en la base de datos (MongoDB) y retornar la versión final
        return wardrobeRepository.save(cloudWardrobe);
    }
    
    /**
     * Solo descarga los datos de la nube
     */
    public Wardrobe getCloudWardrobe(String userId) {
        return wardrobeRepository.findByUserId(userId)
                .orElse(new Wardrobe(userId, Instant.now(), new ArrayList<>()));
    }
}
