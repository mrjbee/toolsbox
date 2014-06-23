package org.monroe.team.toolsbox.repositories;

import org.monroe.team.toolsbox.entities.FileDescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDescriptorRepository extends JpaRepository<FileDescription,Integer> {
}
