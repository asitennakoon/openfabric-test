package ai.openfabric.api.repository;

import ai.openfabric.api.model.Worker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerRepository extends CrudRepository<Worker, String> {

    Optional<Worker> findByName(String name);
    Page<Worker> findAll(Pageable pageable);

}
