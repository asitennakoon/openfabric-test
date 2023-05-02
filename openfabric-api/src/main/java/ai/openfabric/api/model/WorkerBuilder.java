package ai.openfabric.api.model;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;

import java.util.ArrayList;
import java.util.List;

public class WorkerBuilder {

    private final Worker worker;

    public WorkerBuilder() {
        worker = new Worker();
    }

    public WorkerBuilder withName(String name) {
        worker.setName(name);
        return this;
    }

    public WorkerBuilder withPorts(Ports ports) {
        List<Integer> portList = new ArrayList<>();
        for (ExposedPort exposedPort : ports.getBindings().keySet()) {
            portList.add(exposedPort.getPort());
        }

        worker.setPorts(portList);
        return this;
    }

    public WorkerBuilder withStatus(String status) {
        worker.setStatus(status);
        return this;
    }

    public WorkerBuilder withImage(String image) {
        worker.setImage(image);
        return this;
    }

    public Worker build() {
        return worker;
    }

}
