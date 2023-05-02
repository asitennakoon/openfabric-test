package ai.openfabric.api.controller;

import ai.openfabric.api.config.DockerConfig;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${node.api.path}/worker")
public class WorkerController {

    private final DockerConfig dockerConfig;

    public WorkerController(DockerConfig dockerConfig) {
        this.dockerConfig = dockerConfig;
    }

    @PostMapping(path = "/hello")
    public @ResponseBody String hello(@RequestBody String name) {
        return "Hello!" + name;
    }

    @PostMapping(path = "/start/{id}")
    public void startWorker(@PathVariable String id) {
        DockerClient dockerClient = dockerConfig.buildClient();
        dockerClient.startContainerCmd(id).exec();
    }

    @PostMapping(path = "/stop/{id}")
    public void stopWorker(@PathVariable String id) {
        DockerClient dockerClient = dockerConfig.buildClient();
        dockerClient.stopContainerCmd(id).exec();
    }

    @GetMapping(path = "/workers/{limit}")
    public @ResponseBody List<Container> listWorkers(@PathVariable int limit) {
        DockerClient dockerClient = dockerConfig.buildClient();
        return dockerClient.listContainersCmd().withLimit(limit).exec();
    }

}
