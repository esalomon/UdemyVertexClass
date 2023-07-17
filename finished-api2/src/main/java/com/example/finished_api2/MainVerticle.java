package com.example.finished_api2;

import com.example.finished_api2.resources.MongoDBVerticle;
import com.example.finished_api2.resources.RestApiVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

    public static void main(String... args) {

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
        configRetriever.getConfig(config -> { // retrieves config/config.json from resources/classpath

            if (config.succeeded()) {

                JsonObject configJson = config.result();
                log.info("Retrieved properties from 'config/config.json' \n{}", configJson.encodePrettily());
                DeploymentOptions options = new DeploymentOptions().setConfig(configJson);

                vertx.deployVerticle(RestApiVerticle.class.getName(), options)
                    .onFailure(startPromise::fail)
                    .onSuccess(id ->
                        log.info("Deployed {} with id {}", RestApiVerticle.class.getSimpleName(), id))
                    .compose(next ->
                        deployMongoDbVerticle(startPromise, options)
                    );

            }
        });
    }

    private Future<String> deployMongoDbVerticle(final Promise<Void> startPromise, DeploymentOptions options) {

        return vertx.deployVerticle(MongoDBVerticle.class.getName(), options)
            .onFailure(startPromise::fail)
            .onSuccess(id -> {
                log.info("Deployed {} with id {}", MongoDBVerticle.class.getSimpleName(), id);
                startPromise.complete();
            });
    }
}
