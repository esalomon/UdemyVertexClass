package com.example.finished_api2.resources;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import static com.example.finished_api2.constants.ConstantsApp.MONGO_DB_MESSAGE;

@Slf4j
public class MongoDBVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        log.info("The {} verticle is starting...", MongoDBVerticle.class.getSimpleName());
        registerConsumer(vertx);
        startPromise.complete();
    }

    public void registerConsumer(Vertx vertx) {

        log.info("The MongoDB Event Bus consumer is register.");
        vertx.eventBus().consumer(MONGO_DB_MESSAGE, message -> {

            log.info("Received message: {}", Json.encodePrettily(message.body()));
            JsonObject inputJson = new JsonObject(message.body().toString());

            if (inputJson.getString("command").equals("get-product-list")) {
                getProductList(message);
            }
            else {
                String errorMessage = "The received message was not processed.";
                log.error(errorMessage);
                message.fail(500, errorMessage);
            }
        });
    }

    private void getProductList(Message<Object> message) {

        var response = new JsonObject()
            .put("message", "get product list was executed in mongo class");
        message.reply(response);
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {

        log.info("The {} verticle was stopped.", MongoDBVerticle.class.getSimpleName());
        stopPromise.complete();
    }
}
