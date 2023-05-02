package ai.openfabric.api.controller;

import ai.openfabric.api.model.Worker;
import ai.openfabric.api.service.WorkerService;
import com.github.dockerjava.api.model.Statistics;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${node.api.path}/worker")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping(path = "/hello")
    public @ResponseBody String hello(@RequestBody String name) {
        return "Hello!" + name;
    }

    @PostMapping(path = "/start/{name}")
    public void start(@PathVariable String name) {
        workerService.startWorker(name);
    }

    @PostMapping(path = "/stop/{name}")
    public void stop(@PathVariable String name) {
        workerService.stopWorker(name);
    }

    @GetMapping(path = "/workers")
    public @ResponseBody List<Worker> workers(@RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "5") int limit) {
        return workerService.listWorkers(offset, limit);
    }

    @GetMapping(path = "/info/{name}")
    public @ResponseBody Worker info(@PathVariable String name) {
        return workerService.getWorkerInfo(name);
    }

    @GetMapping(path = "/stats/{name}")
    public @ResponseBody Statistics stats(@PathVariable String name) {
        return workerService.getWorkerStats(name);
    }

}
