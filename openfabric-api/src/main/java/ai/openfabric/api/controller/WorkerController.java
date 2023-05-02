package ai.openfabric.api.controller;

import ai.openfabric.api.config.DockerConfig;
import ai.openfabric.api.model.Worker;
import ai.openfabric.api.model.WorkerBuilder;
import ai.openfabric.api.repository.WorkerRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${node.api.path}/worker")
public class WorkerController {

    private final DockerConfig dockerConfig;
    private final WorkerRepository workerRepository;

    public WorkerController(DockerConfig dockerConfig, WorkerRepository workerRepository) {
        this.dockerConfig = dockerConfig;
        this.workerRepository = workerRepository;
    }

    @PostMapping(path = "/hello")
    public @ResponseBody String hello(@RequestBody String name) {
        return "Hello!" + name;
    }

    @PostMapping(path = "/start/{name}")
    public void startWorker(@PathVariable String name) {
        DockerClient dockerClient = dockerConfig.buildClient();
        dockerClient.startContainerCmd(name).exec();

        InspectContainerResponse containerResponse = dockerClient.inspectContainerCmd(name).exec();
        updateWorker(containerResponse);
    }

    @PostMapping(path = "/stop/{name}")
    public void stopWorker(@PathVariable String name) {
        DockerClient dockerClient = dockerConfig.buildClient();
        dockerClient.stopContainerCmd(name).exec();

        InspectContainerResponse containerResponse = dockerClient.inspectContainerCmd(name).exec();
        updateWorker(containerResponse);
    }

    private void updateWorker(InspectContainerResponse containerResponse) {
        Optional<Worker> optionalWorker = workerRepository.findByName(containerResponse.getName());
        Worker worker;

        if (optionalWorker.isPresent()) {
            worker = optionalWorker.get();
            worker.setStatus(containerResponse.getState().getStatus());
        } else {
            worker = new WorkerBuilder()
                    .withName(containerResponse.getName())
                    .withStatus(containerResponse.getState().getStatus())
                    .withImage(containerResponse.getConfig().getImage())
                    .withPorts(containerResponse.getNetworkSettings().getPorts())
                    .build();
        }

        workerRepository.save(worker);
    }

    @GetMapping(path = "/workers")
    public @ResponseBody List<Worker> listWorkers(@RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "5") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        Page<Worker> workerPage = workerRepository.findAll(pageable);

        return workerPage.getContent();
    }

    @GetMapping(path = "/info/{name}")
    public @ResponseBody Worker getWorkerInfo(@PathVariable String name) {
        return workerRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Worker not found with Name: " + name));
    }

}
