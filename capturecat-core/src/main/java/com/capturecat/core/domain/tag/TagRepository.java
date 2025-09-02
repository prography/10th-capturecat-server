package com.capturecat.core.domain.tag;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long>, TagCustomRepository {

	List<Tag> findByNameIn(List<String> names);

	Optional<Tag> findByName(String name);
}
