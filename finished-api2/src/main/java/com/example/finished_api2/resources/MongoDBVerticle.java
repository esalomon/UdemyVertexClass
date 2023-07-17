package com.example.finished_api2.resources;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.example.finished_api2.constants.ConstantsApp.*;

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

        log.info("The {} verticle is registering the event bus consumer: {}...", MongoDBVerticle.class.getSimpleName(), PRODUCT_MESSAGE);
        vertx.eventBus().consumer(PRODUCT_MESSAGE, message -> {

            log.info("Received message: {}", Json.encodePrettily(message.body()));
            JsonObject inputJson = new JsonObject(message.body().toString());

            if (inputJson.getString(COMMAND).equals(GET_PRODUCT_LIST)) {
                getProductList(message);
            }
            else if (inputJson.getString(COMMAND).equals(GET_PRODUCT_BY_ID)) {
                getProductById(message);
            }
            else {
                String errorMessage = String.format(
                    "The received command is not valid: '%s', message: %s",
                    inputJson.getString(COMMAND),
                    message.body()
                );
                log.error(errorMessage);
                message.fail(500, errorMessage);
            }
        });
    }

    private void getProductList(Message<Object> message) {

        mongoClient.find(PRODUCTS_COLLECTION, new JsonObject(), handler -> {

            if (handler.failed()) {
                handleError(message, handler, GET_PRODUCT_LIST);
                return;
            }
            List<JsonObject> result = handler.result();
            log.info("{} handler got data from mongoDB size: {}", GET_PRODUCT_LIST, result.size());
            var response = new JsonObject()
                .put(PRODUCTS_COLLECTION, result);
            message.reply(response.toString());
        });
    }

    private void getProductById(Message<Object> message) {

        var body = new JsonObject(message.body().toString());
        JsonObject query = new JsonObject().put("_id", body.getValue(PRODUCT_ID));

        mongoClient.find(PRODUCTS_COLLECTION, query, handler -> {

            if (handler.failed()) {
                handleError(message, handler, GET_PRODUCT_BY_ID);
                return;
            }
            List<JsonObject> result = handler.result();
            log.info("{} handler got data from mongoDB size: {}", GET_PRODUCT_BY_ID, result.size());
            if (result.isEmpty()) {
                message.reply(NOT_FOUND);
            }
            else {
                message.reply(result.get(0));
            }
        });
    }

    private static void handleError(Message<Object> message, AsyncResult<List<JsonObject>> handler, String message2) {

        String errorMessage = String.format(
            "The %s handler failed with error: %s",
            message2,
            handler.cause().getMessage());
        log.error(errorMessage);
        message.fail(500, errorMessage);
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {

        log.info("The {} verticle was stopped.", MongoDBVerticle.class.getSimpleName());
        stopPromise.complete();
    }
}
