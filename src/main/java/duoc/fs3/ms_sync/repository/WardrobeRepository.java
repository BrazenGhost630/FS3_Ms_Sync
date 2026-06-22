package duoc.fs3.ms_sync.repository;

import duoc.fs3.ms_sync.model.Wardrobe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WardrobeRepository extends JpaRepository<Wardrobe, Long> {
    Optional<Wardrobe> findByUserId(String userId);
}
