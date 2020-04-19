package com.redhat.coolstore;

import com.redhat.coolstore.model.Product;
import com.redhat.coolstore.model.ShoppingCartItem;
import com.redhat.coolstore.utils.Transformers;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemTrackerVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(ItemTrackerVerticle.class.getName());

    // use producer for interacting with Apache Kafka
    private KafkaConsumer<String, String> consumer;
    private String kafkaTopic;

    private Map<String, Integer> registry = new HashMap<>();
    private Map<String, Product> productDetails = new HashMap<>();

    @Override
    public void start() {
        logger.info("Starting " + this.getClass().getSimpleName());

        // read inputs from kafka

        kafkaTopic = config().getString("itemposter.kafkatopic");
        consumer = KafkaConsumer.create(vertx, kafkaConfig());

        consumer.subscribe(kafkaTopic);

        consumer.handler(record -> {
                    System.out.println(record.value());
                    ShoppingCartItem item = Transformers.jsonToShoppingCartItem(new JsonObject(record.value()));
                    trackItem(item);
                }
        );

        // serve results to the web

        Router router = Router.router(vertx);
        router.get("/services/populars").handler(this::getItems);
        vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port")+1);
    }

    private void trackItem(ShoppingCartItem item) {
        String id = item.getProduct().getItemId();

        if (registry.containsKey(id)) {
            registry.put(id, registry.get(id) + item.getQuantity());
        } else {
            registry.put(id, item.getQuantity());
            saveProduct(item.getProduct());
        }

        System.out.println(id + " -> " + registry.get(id));
    }

    private void saveProduct(Product product) {
        productDetails.put(product.getItemId(), product);
    }

    private Map<String, String> kafkaConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", config().getString("bootstrap.servers"));
        config.put("key.deserializer", config().getString("key.deserializer"));
        config.put("value.deserializer", config().getString("value.deserializer"));
        config.put("acks", config().getString("acks"));
        config.put("group.id", config().getString("group.id"));
        config.put("auto.offset.reset", config().getString("auto.offset.reset"));
        config.put("enable.auto.commit", config().getString("enable.auto.commit"));

        return config;
    }

    private void getItems(RoutingContext ctx) {
        JsonArray results = new JsonArray();

        registry.keySet().forEach(id -> {
            int popularity = registry.get(id);
            Product product = productDetails.get(id);

            JsonObject json = Transformers.productToJson(product);
            json.put("popularity", popularity);

            results.add(json);
        });

        sendJsonArrayResponse(ctx, results);
    }

    private void sendJsonArrayResponse(RoutingContext ctx, JsonArray arr) {
        ctx.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(arr.encodePrettily());
    }
}
