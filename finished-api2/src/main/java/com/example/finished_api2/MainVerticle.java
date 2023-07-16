package com.example.finished_api2;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

    public static void main(String... args) {

        Vertx vertx = Vertx.vertx();

        ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
        configRetriever.getConfig(config -> { // Use config/config.json from resources/classpath

            if (config.succeeded()) {
                JsonObject configJson = config.result();
                log.info("Retrieved properties from 'config/config.json' \n{}", configJson.encodePrettily());
                DeploymentOptions options = new DeploymentOptions().setConfig(configJson);
                vertx.deployVerticle(new MainVerticle(), options);
            }
        });
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        vertx.createHttpServer().requestHandler(req -> {
            req.response()
                .putHeader("content-type", "text/plain")
                .end("Hello from Vert.x!");
        }).listen(8888, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                log.info("HTTP server started on port 8888");
            } else {
                startPromise.fail(http.cause());
            }
        });
    }
}
