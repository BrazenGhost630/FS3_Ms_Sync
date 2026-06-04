package duoc.fs3.ms_sync.service;

import duoc.fs3.ms_sync.model.ClothingItem;
import duoc.fs3.ms_sync.model.Wardrobe;
import duoc.fs3.ms_sync.repository.WardrobeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyncService {

    private final WardrobeRepository wardrobeRepository;
    private final ImageStorageService imageStorageService;

    /**
     * Sincronización bidireccional basada en Timestamp (updatedAt)
     */
    public Wardrobe syncWithCloud(String userId, List<ClothingItem> localItems) {
        // 1. Obtener el ropero de la nube o crear uno nuevo si no existe
        Wardrobe cloudWardrobe = wardrobeRepository.findByUserId(userId)
                .orElse(new Wardrobe(userId, Instant.now(), new ArrayList<>()));

        // 2. Mapear los items de la nube por ID para una búsqueda rápida
        Map<Long, ClothingItem> mergedItems = cloudWardrobe.getItems().stream()
                .collect(Collectors.toMap(ClothingItem::getId, item -> item));

        // 3. Resolución de conflictos: Comparar timestamps
        for (ClothingItem localItem : localItems) {
            ClothingItem cloudItem = mergedItems.get(localItem.getId());

            // Si el item no existe en la nube, o si el local es más reciente, gana el local
            if (cloudItem == null || localItem.getUpdatedAt().isAfter(cloudItem.getUpdatedAt())) {
                // Actualizar syncStatus a synced y establecer relación
                localItem.setSyncStatus("synced");
                localItem.setWardrobe(cloudWardrobe);
                mergedItems.put(localItem.getId(), localItem);
            }
        }

        // 4. Actualizar el ropero con la lista fusionada y el nuevo timestamp de sincronización
        cloudWardrobe.setItems(new ArrayList<>(mergedItems.values()));
        cloudWardrobe.setLastSync(Instant.now());

        // 5. Guardar en la base de datos (MySQL) y retornar la versión final
        return wardrobeRepository.save(cloudWardrobe);
    }

    /**
     * Sube una imagen y retorna la URL
     */
    public String uploadImage(MultipartFile file) {
        return imageStorageService.storeImage(file);
    }

    /**
     * Elimina una prenda del ropero del usuario
     */
    public void deleteClothingItem(String userId, Long itemId) {
        Wardrobe wardrobe = wardrobeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ropero no encontrado para el usuario"));

        // Eliminar la prenda de la lista
        List<ClothingItem> updatedItems = wardrobe.getItems().stream()
                .filter(item -> !item.getId().equals(itemId))
                .collect(Collectors.toList());

        wardrobe.setItems(updatedItems);
        wardrobe.setLastSync(Instant.now());
        wardrobeRepository.save(wardrobe);
    }

    /**
     * Solo descarga los datos de la nube
     */
    public Wardrobe getCloudWardrobe(String userId) {
        return wardrobeRepository.findByUserId(userId)
                .orElse(new Wardrobe(userId, Instant.now(), new ArrayList<>()));
    }
}
