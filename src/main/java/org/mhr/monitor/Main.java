package org.mhr.monitor;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.mhr.monitor.mock.data.mongo.EmbeddedMongoContainer.HOST;
import static org.mhr.monitor.mock.data.mongo.EmbeddedMongoContainer.PORT;

@Configuration
@EnableScheduling
@EnableAutoConfiguration(exclude = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class,
    MongoRepositoriesAutoConfiguration.class,
    EmbeddedMongoAutoConfiguration.class
})
@ComponentScan("org.mhr.monitor")
@PropertySource("classpath:application.properties")
public class Main {

    @Bean
    @DependsOn("mongo")
    public MongoClient mongoClient() {
        final ConnectionString connection = new ConnectionString("mongodb://" + HOST + ":" + PORT);
        return MongoClients.create(connection);
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Main.class);
        app.run(args);
    }
}
