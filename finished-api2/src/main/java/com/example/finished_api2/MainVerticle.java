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
        vertx.deployVerticle(new MainVerticle());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
        configRetriever.getConfig(config -> { // Use config/config.json from resources/classpath

            if (config.succeeded()) {
                JsonObject configJson = config.result();
                log.info("Retrieved properties from 'config/config.json' \n{}", configJson.encodePrettily());
                DeploymentOptions options = new DeploymentOptions().setConfig(configJson);
                configHttpServer(startPromise, options);
            }
        });
    }

    private void configHttpServer(Promise<Void> startPromise, DeploymentOptions options) {

        Integer port = options.getConfig().getInteger("http.port");
        vertx.createHttpServer().requestHandler(req -> {
            req.response()
                .putHeader("content-type", "text/plain")
                .end("Hello from Vert.x!");
        }).listen(port, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                log.info("HTTP server started on port: {}", port);
            } else {
                startPromise.fail(http.cause());
            }
        });
    }
}
