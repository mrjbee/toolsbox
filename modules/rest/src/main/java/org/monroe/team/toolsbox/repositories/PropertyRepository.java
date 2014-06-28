package org.monroe.team.toolsbox.repositories;

import org.monroe.team.toolsbox.entities.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
}
