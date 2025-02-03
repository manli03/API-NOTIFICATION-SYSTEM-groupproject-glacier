package my.uum;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.StructType;

import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

public class KafkaToCassandra {

    private static final Logger logger = Logger.getLogger(KafkaToCassandra.class);

    public static void main(String[] args) throws StreamingQueryException, TimeoutException {
        // Configure logging to minimize console output for better clarity
        Logger.getLogger("org.apache.spark").setLevel(Level.WARN);
        Logger.getLogger("org.apache.kafka").setLevel(Level.WARN);

        // Create Spark session with appropriate configurations for performance optimization
        SparkSession spark = SparkSession.builder()
                .appName("Kafka to Cassandra Stream")
                .master("local[*]")
                .config("spark.cassandra.connection.host", "127.0.0.1")
                .config("spark.cassandra.connection.port", "9042")
                .config("spark.sql.shuffle.partitions", "500") // Higher parallelism
                .config("spark.default.parallelism", "500")
                .config("spark.executor.memory", "16g")
                .config("spark.executor.cores", "8")
                .config("spark.streaming.backpressure.enabled", "true")
                .config("spark.sql.streaming.forceDeleteTempCheckpointLocation", "true") // Delete temp checkpoints
                .getOrCreate();

        // Define the schema of the data that is expected from Kafka
        StructType schema = new StructType()
                .add("show_id", "string")
                .add("type", "string")
                .add("title", "string")
                .add("director", "string")
                .add("cast", "string")
                .add("country", "string")
                .add("date_added", "string")
                .add("release_year", "integer")
                .add("rating", "string")
                .add("duration", "string")
                .add("listed_in", "string")
                .add("description", "string");

        // Read streaming data from Kafka
        Dataset<Row> kafkaStream = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "127.0.0.1:9092")
                .option("subscribe", "test1")
                .option("startingOffsets", "latest")
                .option("failOnDataLoss", "false")
                .load()
                .selectExpr("CAST(value AS STRING) as json")
                .select(from_json(col("json"), schema).as("data"))
                .select("data.*")
                .filter("show_id IS NOT NULL");

        // Process the data stream and write to Cassandra
        StreamingQuery query = kafkaStream.writeStream()
                .outputMode("append")
                .trigger(Trigger.ProcessingTime("20 seconds"))
                .foreachBatch((batchDF, batchId) -> {
                    if (!batchDF.isEmpty()) {
                        try {
                            // Log the number of records in the batch
                            logger.info("Processing batch " + batchId + " with " + batchDF.count() + " records");

                            // Load existing data from Cassandra
                            Dataset<Row> existingData = spark
                                    .read()
                                    .format("org.apache.spark.sql.cassandra")
                                    .option("keyspace", "sparkdata")
                                    .option("table", "cust_data")
                                    .load()
                                    .filter("show_id IS NOT NULL");

                            if (!existingData.isEmpty()) {
                                // Alias dataframes to avoid ambiguous column references
                                Dataset<Row> batchAlias = batchDF.as("batchDF");
                                Dataset<Row> existingAlias = existingData.as("existingData");

                                // Find new data
                                Dataset<Row> newData = batchAlias.join(existingAlias, batchAlias.col("show_id").equalTo(existingAlias.col("show_id")), "left_anti")
                                        .withColumn("logg", lit("new data"));

                                // Find updated data
                                Dataset<Row> rowsToUpdate = batchAlias.join(existingAlias, "show_id")
                                        .filter(
                                                batchAlias.col("type").notEqual(existingAlias.col("type"))
                                                        .or(batchAlias.col("title").notEqual(existingAlias.col("title")))
                                                        .or(batchAlias.col("director").notEqual(existingAlias.col("director")))
                                                        .or(batchAlias.col("cast").notEqual(existingAlias.col("cast")))
                                                        .or(batchAlias.col("country").notEqual(existingAlias.col("country")))
                                                        .or(batchAlias.col("date_added").notEqual(existingAlias.col("date_added")))
                                                        .or(batchAlias.col("release_year").notEqual(existingAlias.col("release_year")))
                                                        .or(batchAlias.col("rating").notEqual(existingAlias.col("rating")))
                                                        .or(batchAlias.col("duration").notEqual(existingAlias.col("duration")))
                                                        .or(batchAlias.col("listed_in").notEqual(existingAlias.col("listed_in")))
                                                        .or(batchAlias.col("description").notEqual(existingAlias.col("description"))))
                                        .select("batchDF.*")
                                        .withColumn("logg", lit("updated"));

                                // Find deleted data by checking for rows in existingAlias that are not in batchAlias
                                Dataset<Row> deletedData = existingAlias.join(batchAlias, existingAlias.col("show_id").equalTo(batchAlias.col("show_id")), "left_anti")
                                        .filter("show_id IS NOT NULL")
                                        .withColumn("logg", lit("deleted"));

                                // Write new data to Cassandra
                                if (!newData.isEmpty()) {
                                    newData.write()
                                            .format("org.apache.spark.sql.cassandra")
                                            .option("keyspace", "sparkdata")
                                            .option("table", "cust_data")
                                            .mode("append")
                                            .save();
                                }

                                // Write deleted data to Cassandra
                                if (!deletedData.isEmpty()) {
                                    deletedData.write()
                                            .format("org.apache.spark.sql.cassandra")
                                            .option("keyspace", "sparkdata")
                                            .option("table", "cust_data")
                                            .mode("append")
                                            .save();
                                }

                                // Write updated data to Cassandra
                                if (!rowsToUpdate.isEmpty()) {
                                    rowsToUpdate.write()
                                            .format("org.apache.spark.sql.cassandra")
                                            .option("keyspace", "sparkdata")
                                            .option("table", "cust_data")
                                            .mode("append")
                                            .save();
                                }
                            } else {
                                // If existing data is empty, treat all batch data as no changes
                                batchDF.withColumn("logg", lit("no changes"))
                                        .write()
                                        .format("org.apache.spark.sql.cassandra")
                                        .option("keyspace", "sparkdata")
                                        .option("table", "cust_data")
                                        .mode("append")
                                        .save();
                            }

                            batchDF.unpersist(); // Unpersist data from cache after use
                        } catch (Exception e) {
                            logger.error("Error processing batch " + batchId + ": " + e.getMessage(), e);
                        }
                    } else {
                        logger.info("No records to process in batch " + batchId);
                    }
                })
                .start();

        query.awaitTermination();
    }
}
