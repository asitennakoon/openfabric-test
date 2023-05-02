package ai.openfabric.api.service;

import ai.openfabric.api.config.DockerConfig;
import ai.openfabric.api.model.Worker;
import ai.openfabric.api.model.WorkerBuilder;
import ai.openfabric.api.repository.WorkerRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.InvocationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class WorkerService {

    private final DockerConfig dockerConfig;
    private final WorkerRepository workerRepository;

    public WorkerService(DockerConfig dockerConfig, WorkerRepository workerRepository) {
        this.dockerConfig = dockerConfig;
        this.workerRepository = workerRepository;
    }

    public void startWorker(String name) {
        DockerClient dockerClient = dockerConfig.buildClient();
        dockerClient.startContainerCmd(name).exec();

        InspectContainerResponse containerResponse = dockerClient.inspectContainerCmd(name).exec();
        updateWorker(containerResponse);
    }

    public void stopWorker(String name) {
        DockerClient dockerClient = dockerConfig.buildClient();
        dockerClient.stopContainerCmd(name).exec();

        InspectContainerResponse containerResponse = dockerClient.inspectContainerCmd(name).exec();
        updateWorker(containerResponse);
    }

    private void updateWorker(InspectContainerResponse containerResponse) {
        Optional<Worker> optionalWorker = workerRepository.findByName(containerResponse.getName().replace("/", ""));
        Worker worker;

        if (optionalWorker.isPresent()) {
            worker = optionalWorker.get();
            worker.setStatus(containerResponse.getState().getStatus());
        } else {
            worker = new WorkerBuilder()
                    .withName(containerResponse.getName().replace("/", ""))
                    .withStatus(containerResponse.getState().getStatus())
                    .withImage(containerResponse.getConfig().getImage())
                    .withPorts(containerResponse.getNetworkSettings().getPorts())
                    .build();
        }

        workerRepository.save(worker);
    }

    public List<Worker> listWorkers(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        Page<Worker> workerPage = workerRepository.findAll(pageable);

        return workerPage.getContent();
    }

    public Worker getWorkerInfo(String name) {
        return workerRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Worker not found with Name: " + name));
    }

    public Statistics getWorkerStats(String name) {
        InvocationBuilder.AsyncResultCallback<Statistics> callback = new InvocationBuilder.AsyncResultCallback<>();

        DockerClient dockerClient = dockerConfig.buildClient();
        dockerClient.statsCmd(name).exec(callback);
        Statistics stats = callback.awaitResult();
        try {
            callback.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stats;
    }

}