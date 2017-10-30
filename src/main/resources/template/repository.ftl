package ${package};

import io.gumga.domain.repository.GumgaCrudRepository;
import ${packageEntity};
import org.springframework.stereotype.Repository;

@Repository
public interface ${repositoryName}Repository extends GumgaCrudRepository<${repositoryName}, String> {}