package com.example.finished_api2.resources;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class RestApiVerticle extends AbstractVerticle {


    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        log.info("The {} verticle is starting...", RestApiVerticle.class.getSimpleName());
        Router router = configRestApis();
        configHttpServer(startPromise, router, config());
    }

    private Router configRestApis() {

        Router router = Router.router(vertx);
        router.route()
            .handler(BodyHandler.create()) // handles the request body
            .failureHandler(this::handleFailure); // general endpoint failure.

        ProductRestAPI.attach(router);

        return router;
    }

    private void handleFailure(RoutingContext context) {

        if (context.response().ended()) {
            return; // Ignore completed response
        }
        log.error("Route Error:", context.failure());
        context.response()
            .setStatusCode(500)
            .end(
                new JsonObject()
                    .put("message", "There was an error")
                    .toBuffer());
    }

    private static Integer getPort(Promise<Void> startPromise, JsonObject config) {

        Integer port = config.getInteger("http.port");
        if (Objects.isNull(port)) {
            String errorMessage = "The 'http.port' property was not provided.";
            log.error(errorMessage);
            startPromise.fail(errorMessage);
            System.exit(-1);
        }
        return port;
    }

    private void configHttpServer(Promise<Void> startPromise, Router router, JsonObject options) {

        Integer port = getPort(startPromise, options); // if there is no port in the properties the start-up will fail.

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(port, http -> {

                if (http.succeeded()) {
                    startPromise.complete();
                    log.info("HTTP server started on port: {}", port);
                }
                else {
                    startPromise.fail(http.cause());
                }
            });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {

        log.info("The {} verticle was stopped.", RestApiVerticle.class.getSimpleName());
        stopPromise.complete();
    }

}
