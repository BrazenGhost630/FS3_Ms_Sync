package duoc.fs3.ms_sync.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Document(collection = "wardrobes")
public class Wardrobe {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String userId;
    private Instant lastSync;
    private List<ClothingItem> items;

    public Wardrobe(String userId, Instant lastSync, List<ClothingItem> items) {
        this.userId = userId;
        this.lastSync = lastSync;
        this.items = items;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Instant getLastSync() { return lastSync; }
    public void setLastSync(Instant lastSync) { this.lastSync = lastSync; }
    public List<ClothingItem> getItems() { return items; }
    public void setItems(List<ClothingItem> items) { this.items = items; }
}
