package duoc.fs3.ms_sync.repository;

import duoc.fs3.ms_sync.model.Wardrobe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WardrobeRepository extends MongoRepository<Wardrobe, String> {
    Optional<Wardrobe> findByUserId(String userId);
}
