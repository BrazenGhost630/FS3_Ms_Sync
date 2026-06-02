package duoc.fs3.ms_sync.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clothing_items")
public class ClothingItem {
    @Id
    private String id;
    private String name;
    private String type;
    private String season;
    private String style;
    private String imageUri;
    private String primaryColor;
    private String secondaryColor;
    private String syncStatus; // 'pending', 'synced', 'error'
    private Instant updatedAt;

    public ClothingItem(String id, String name, String type, String season, String style, 
                       String imageUri, String primaryColor, String secondaryColor, 
                       String syncStatus, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.season = season;
        this.style = style;
        this.imageUri = imageUri;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.syncStatus = syncStatus;
        this.updatedAt = updatedAt;
    }
}
