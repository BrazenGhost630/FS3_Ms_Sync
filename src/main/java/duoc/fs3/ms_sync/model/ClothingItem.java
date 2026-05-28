package duoc.fs3.ms_sync.model;

import org.springframework.data.annotation.Id;
import java.time.Instant;

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
    private Instant updatedAt;

    // Constructores, Getters y Setters
    public ClothingItem() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
