package cz.istep.javatest.repository;

import cz.istep.javatest.data.FrameworkVersion;
import org.springframework.data.repository.CrudRepository;

public interface FrameworkVersionRepository extends CrudRepository<FrameworkVersion, Long> {
}