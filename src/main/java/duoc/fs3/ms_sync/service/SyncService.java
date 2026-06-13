package duoc.fs3.ms_sync.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import duoc.fs3.ms_sync.model.ClothingItem;
import duoc.fs3.ms_sync.model.Wardrobe;
import duoc.fs3.ms_sync.repository.WardrobeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncService {

    private final WardrobeRepository wardrobeRepository;
    private final Optional<ImageStorageService> imageStorageService;

    /**
     * Sincronización bidireccional basada en Timestamp (updatedAt)
     */
    public Wardrobe syncWithCloud(String userId, List<ClothingItem> localItems) {
        // 1. Obtener el ropero de la nube o crear uno nuevo si no existe
        Wardrobe cloudWardrobe = wardrobeRepository.findByUserId(userId).orElse(null);
        if (cloudWardrobe == null) {
            cloudWardrobe = new Wardrobe(userId, Instant.now(), new ArrayList<>());
            // Guardar el ropero padre primero para evitar "Detached entity passed to persist" al persistir cascadas
            cloudWardrobe = wardrobeRepository.save(cloudWardrobe);
        }

        // 2. Mapear los items de la nube por ID para una búsqueda rápida
        Map<Long, ClothingItem> mergedItems = cloudWardrobe.getItems().stream()
                .collect(Collectors.toMap(ClothingItem::getId, item -> item));

        // 3. Resolución de conflictos: Comparar timestamps
        for (ClothingItem localItem : localItems) {
            ClothingItem cloudItem = mergedItems.get(localItem.getId());

            if (cloudItem == null) {
                // Si es un item nuevo, limpiamos el ID local para que MySQL asigne el suyo y lo trate como entidad nueva
                localItem.setId(null);
                localItem.setSyncStatus("synced");
                localItem.setWardrobe(cloudWardrobe);
                cloudWardrobe.getItems().add(localItem);
            } else if (localItem.getUpdatedAt().isAfter(cloudItem.getUpdatedAt())) {
                // Si el item existe, actualizamos los valores de la entidad conectada (attached)
                cloudItem.setName(localItem.getName());
                cloudItem.setType(localItem.getType());
                cloudItem.setSeason(localItem.getSeason());
                cloudItem.setStyle(localItem.getStyle());
                cloudItem.setImageUri(localItem.getImageUri());
                cloudItem.setPrimaryColor(localItem.getPrimaryColor());
                cloudItem.setSecondaryColor(localItem.getSecondaryColor());
                cloudItem.setSyncStatus("synced");
                cloudItem.setUpdatedAt(localItem.getUpdatedAt());
            }
        }

        // 4. Eliminar prendas de la nube que ya no existen localmente
        List<Long> localItemIds = localItems.stream()
                .map(ClothingItem::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        List<ClothingItem> itemsToRemove = cloudWardrobe.getItems().stream()
                .filter(item -> !localItemIds.contains(item.getId()))
                .collect(Collectors.toList());

        for (ClothingItem itemToRemove : itemsToRemove) {
            // Eliminar la imagen asociada si existe
            if (itemToRemove.getImageUri() != null && !itemToRemove.getImageUri().isEmpty()) {
                if (imageStorageService.isPresent()) {
                    String fileName = imageStorageService.get().extractFileName(itemToRemove.getImageUri());
                    imageStorageService.get().deleteImage(fileName);
                }
            }
            // Remover la prenda (JPA orphanRemoval se encarga de eliminar de BD)
            cloudWardrobe.getItems().remove(itemToRemove);
        }

        cloudWardrobe.setLastSync(Instant.now());

        // 5. Guardar en la base de datos (MySQL) y retornar la versión final
        return wardrobeRepository.save(cloudWardrobe);
    }

    /**
     * Sube una imagen y retorna la URL
     */
    public String uploadImage(MultipartFile file) {
        if (imageStorageService.isEmpty()) {
            throw new RuntimeException("El servicio de almacenamiento de imágenes no está disponible. Configure aws.s3.enabled=true para usar S3.");
        }
        return imageStorageService.get().storeImage(file);
    }

    /**
     * Elimina una prenda del ropero del usuario
     */
    public void deleteClothingItem(String userId, Long itemId) {
        Wardrobe wardrobe = wardrobeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ropero no encontrado para el usuario: " + userId));

        // Buscar la prenda a eliminar dentro de la lista de items del ropero
        ClothingItem itemToRemove = wardrobe.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Prenda no encontrada con ID: " + itemId));

        // Eliminar la imagen asociada del almacenamiento si existe
        if (itemToRemove.getImageUri() != null && !itemToRemove.getImageUri().isEmpty()) {
            if (imageStorageService.isPresent()) {
                String fileName = imageStorageService.get().extractFileName(itemToRemove.getImageUri());
                imageStorageService.get().deleteImage(fileName);
            }
        }

        // Remover la prenda de la lista. Gracias a orphanRemoval=true, JPA la borrará de la BD.
        wardrobe.getItems().remove(itemToRemove);
        wardrobe.setLastSync(Instant.now());
        wardrobeRepository.save(wardrobe); // Guardar el ropero para actualizar lastSync y confirmar la eliminación
    }

    /**
     * Solo descarga los datos de la nube
     */
    public Wardrobe getCloudWardrobe(String userId) {
        // Busca el ropero, y si no existe, crea uno nuevo, lo guarda en la BD y lo retorna.
        return wardrobeRepository.findByUserId(userId).orElseGet(() -> {
            Wardrobe newWardrobe = new Wardrobe(userId, Instant.now(), new ArrayList<>());
            return wardrobeRepository.save(newWardrobe);
        });
    }
}
