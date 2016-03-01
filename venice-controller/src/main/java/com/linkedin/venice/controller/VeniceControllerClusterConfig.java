package com.linkedin.venice.controller;

import com.linkedin.venice.exceptions.VeniceException;
import com.linkedin.venice.meta.OfflinePushStrategy;
import com.linkedin.venice.meta.PersistenceType;
import com.linkedin.venice.meta.ReadStrategy;
import com.linkedin.venice.meta.RoutingStrategy;
import com.linkedin.venice.utils.Props;
import com.linkedin.venice.utils.Utils;
import java.util.List;
import org.apache.log4j.Logger;


/**
 * Configuration for one cluster in venice controller.
 */
public class VeniceControllerClusterConfig {
  private static final Logger logger = Logger.getLogger(VeniceControllerClusterConfig.class);

  public static final String CLUSTER_NAME = "cluster.name";
  public static final String ZK_ADDRESS = "zk.address";
  public static final String CONTROLLER_NAME = "controller.name";
  public static final String KAFKA_REPLICA_FACTOR = "kafka.replica.factor";
  public static final String KAFKA_ZK_ADDRESS = "kafka.zk.address";
  public static final String PERSISTENCE_TYPE = "default.persistence.type";
  public static final String READ_STRATEGY = "default.read.strategy";
  public static final String OFFLINE_PUSH_STRATEGY = "default.offline.push.strategy";
  public static final String ROUTING_STRATEGY = "default.routing.strategy";
  public static final String REPLICA_FACTOR = "default.replica.factor";
  public static final String NUMBER_OF_PARTITION = "default.partition.count";
  public static final String INITIALIZING_STORES = "default.initializing.stores";

  private String clusterName;
  private String zkAddress;
  private String controllerName;
  private PersistenceType persistenceType;
  private ReadStrategy readStrategy;
  private OfflinePushStrategy offlinePushStrategy;
  private RoutingStrategy routingStrategy;
  private int replicaFactor;
  private int numberOfPartition;
  /**
   * Number of replication for each kafka topic. It should be different from venice data replica factor.
   */
  private int kafkaReplicaFactor;
  /**
   * Address of zookeeper that kafka used. It may be different from what Helix used.
   */
  private String kafkaZkAddress;
  /**
   * Default stores which should be created when controller start-up. Right now only for testing purpose.
   */
  private List<String> stores;

  public VeniceControllerClusterConfig(String filePath) {
    logger.info("Loading configuration from config file:" + filePath);
    try {
      Props props = Utils.parseProperties(filePath);
      checkProperties(props);
      logger.info("Loaded configuration");
    } catch (Exception e) {
      String errorMessage = "Can not load properties from config file:" + filePath;
      logger.error(errorMessage);
      throw new VeniceException(errorMessage, e);
    }
  }

  private void checkProperties(Props props) {
    clusterName = props.getString(CLUSTER_NAME);
    zkAddress = props.getString(ZK_ADDRESS);
    controllerName = props.getString(CONTROLLER_NAME);
    kafkaZkAddress = props.getString(KAFKA_ZK_ADDRESS);
    kafkaReplicaFactor = props.getInt(KAFKA_REPLICA_FACTOR);
    replicaFactor = props.getInt(REPLICA_FACTOR);
    numberOfPartition = props.getInt(NUMBER_OF_PARTITION);

    if (props.containsKey(PERSISTENCE_TYPE)) {
      persistenceType = PersistenceType.valueOf(props.getString(PERSISTENCE_TYPE));
    } else {
      persistenceType = PersistenceType.IN_MEMORY;
    }
    if (props.containsKey(READ_STRATEGY)) {
      readStrategy = ReadStrategy.valueOf(props.getString(READ_STRATEGY));
    } else {
      readStrategy = ReadStrategy.ANY_OF_ONLINE;
    }
    if (props.containsKey(OFFLINE_PUSH_STRATEGY)) {
      offlinePushStrategy = OfflinePushStrategy.valueOf(props.getString(OFFLINE_PUSH_STRATEGY));
    } else {
      offlinePushStrategy = OfflinePushStrategy.WAIT_ALL_REPLICAS;
    }
    if (props.containsKey(ROUTING_STRATEGY)) {
      routingStrategy = RoutingStrategy.valueOf(props.getString(ROUTING_STRATEGY));
    } else {
      routingStrategy = RoutingStrategy.CONSISTENT_HASH;
    }
    stores = props.getList(INITIALIZING_STORES);
  }

  public String getClusterName() {
    return clusterName;
  }

  public String getZkAddress() {
    return zkAddress;
  }

  public String getControllerName() {
    return controllerName;
  }

  public String getKafkaZkAddress() {
    return kafkaZkAddress;
  }

  public PersistenceType getPersistenceType() {
    return persistenceType;
  }

  public ReadStrategy getReadStrategy() {
    return readStrategy;
  }

  public OfflinePushStrategy getOfflinePushStrategy() {
    return offlinePushStrategy;
  }

  public RoutingStrategy getRoutingStrategy() {
    return routingStrategy;
  }

  public int getReplicaFactor() {
    return replicaFactor;
  }

  public int getNumberOfPartition() {
    return numberOfPartition;
  }

  public int getKafkaReplicaFactor() {
    return kafkaReplicaFactor;
  }

  public List<String> getStores() {
    return stores;
  }
}
