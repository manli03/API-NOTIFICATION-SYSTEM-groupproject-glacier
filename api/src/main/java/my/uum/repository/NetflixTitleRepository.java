package my.uum.repository;

import my.uum.entity.NetflixTitle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NetflixTitleRepository extends JpaRepository<NetflixTitle, String> {
}
