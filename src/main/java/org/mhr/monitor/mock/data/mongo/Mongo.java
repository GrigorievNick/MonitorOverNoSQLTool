package org.mhr.monitor.mock.data.mongo;

import java.io.IOException;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class Mongo {

    final EmbeddedMongoContainer embeddedMongoContainer;

    public Mongo() throws IOException {
        embeddedMongoContainer = new EmbeddedMongoContainer();
        embeddedMongoContainer.start();
    }

    @PreDestroy
    public void stop() {
        embeddedMongoContainer.stop();
    }
}
