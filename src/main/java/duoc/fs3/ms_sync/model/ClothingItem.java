package duoc.fs3.ms_sync.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "clothing_items")
public class ClothingItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private String season;
    private String style;
    private String imageUri;
    private String primaryColor;
    private String secondaryColor;
    private String syncStatus; // 'pending', 'synced', 'error'
    private Instant updatedAt;

    @ManyToOne
    @JoinColumn(name = "wardrobe_id")
    @JsonIgnore
    private Wardrobe wardrobe;
}
