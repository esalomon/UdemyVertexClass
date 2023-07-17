package com.example.finished_api2.resources;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.example.finished_api2.constants.ConstantsApp.MONGO_DB_MESSAGE;

@Slf4j
public class MongoDBVerticle extends AbstractVerticle {

    private MongoClient mongoClient = null;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        log.info("The {} verticle is starting...", MongoDBVerticle.class.getSimpleName());
        setupMongoDbClient(vertx);
        registerConsumer(vertx);
        startPromise.complete();
    }

    private void setupMongoDbClient(Vertx vertx) {

        log.info("The {} verticle is configuring the mongoDb client...", MongoDBVerticle.class.getSimpleName());
        JsonObject dbConfig = new JsonObject();
        dbConfig.put("connection_string", "mongodb://" + config().getString("mongodb.host") + ":" + config().getInteger("mongodb.port") + "/" + config().getString("mongodb.databasename"));
        dbConfig.put("username", config().getString("mongodb.username")); //use docker credentials
        dbConfig.put("password", config().getString("mongodb.password"));
        dbConfig.put("authSource", config().getString("mongodb.authSource"));
        dbConfig.put("useObjectId", true);

        mongoClient = MongoClient.createShared(vertx, dbConfig);
    }

    public void registerConsumer(Vertx vertx) {

        log.info("The {} verticle is registering the event bus consumer: {}...", MongoDBVerticle.class.getSimpleName(), MONGO_DB_MESSAGE);
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

        mongoClient.find("products", new JsonObject(), handler -> {

            if (handler.failed()) {
                String errorMessage = String.format(
                    "The getProductList() handler failed with error: %s",
                    handler.cause().getMessage());
                log.error(errorMessage);
                message.fail(500, errorMessage);
                return;
            }
            List<JsonObject> result = handler.result();
            log.info("The getProductList() handler got data from mongoDB size: {}", result.size());
            var response = new JsonObject()
                .put("products", result);
            message.reply(response.toString());
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {

        log.info("The {} verticle was stopped.", MongoDBVerticle.class.getSimpleName());
        stopPromise.complete();
    }
}
