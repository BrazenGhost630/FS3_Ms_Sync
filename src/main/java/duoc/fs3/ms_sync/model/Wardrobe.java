package duoc.fs3.ms_sync.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "wardrobes")
public class Wardrobe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String userId;
    private Instant lastSync;

    @OneToMany(mappedBy = "wardrobe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClothingItem> items = new ArrayList<>();

    public Wardrobe() {}

    public Wardrobe(String userId, Instant lastSync, List<ClothingItem> items) {
        this.userId = userId;
        this.lastSync = lastSync;
        this.items = items;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Instant getLastSync() { return lastSync; }
    public void setLastSync(Instant lastSync) { this.lastSync = lastSync; }
    public List<ClothingItem> getItems() { return items; }
    public void setItems(List<ClothingItem> items) { this.items = items; }

    public void addItem(ClothingItem item) {
        items.add(item);
        item.setWardrobe(this);
    }

    public void removeItem(ClothingItem item) {
        items.remove(item);
        item.setWardrobe(null);
    }
}
