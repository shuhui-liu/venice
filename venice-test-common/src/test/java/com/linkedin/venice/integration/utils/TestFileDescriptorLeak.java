package com.linkedin.venice.integration.utils;

import com.sun.management.UnixOperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestFileDescriptorLeak {
  private static final Logger LOGGER = Logger.getLogger(TestFileDescriptorLeak.class);

  private static final boolean FORCE_GC = false;

  @Test(invocationCount = 25000, groups = {"flaky"})
  public void testZookeeperLeak() {
    try (ZkServerWrapper zkServerWrapper = ServiceFactory.getZkServer()) {
      LOGGER.info("Created ZkServerWrapper: " + zkServerWrapper.getAddress());
    }
  }

  @Test(invocationCount = 20, groups = {"flaky"})
  public void testKafkaBrokerLeak() {
    try (KafkaBrokerWrapper kafkaBrokerWrapper = ServiceFactory.getKafkaBroker()) {
      LOGGER.info("Created KafkaBrokerWrapper: " + kafkaBrokerWrapper.getAddress());
    }
  }

  @Test(invocationCount = 20, groups = {"flaky"})
  public void testVeniceClusterLeak() {
    try (VeniceClusterWrapper veniceClusterWrapper = ServiceFactory.getVeniceCluster()) {
      LOGGER.info("Created VeniceClusterWrapper: " + veniceClusterWrapper.getClusterName());
    }
  }

  @Test(invocationCount = 20, groups = {"flaky"})
  public void testBrooklinLeak() {
    try (KafkaBrokerWrapper kafkaBrokerWrapper = ServiceFactory.getKafkaBroker();
        BrooklinWrapper brooklinWrapper = ServiceFactory.getBrooklinWrapper(kafkaBrokerWrapper)) {
      LOGGER.info("Created BrooklinWrapper: " + brooklinWrapper.getAddress());
    }
  }

  private VeniceClusterWrapper veniceClusterWrapper;

  @BeforeClass
  public void setUpReusableCluster() {
    this.veniceClusterWrapper = ServiceFactory.getVeniceCluster(1, 2, 1);
  }

  @AfterClass
  public void tearDownReusableCluster() {
    IOUtils.closeQuietly(this.veniceClusterWrapper);
  }

  @Test(invocationCount = 20, groups = {"flaky"})
  public void testVeniceServerLeak() {
    testVeniceInstanceLeak(() -> this.veniceClusterWrapper.addVeniceServer(new Properties(), new Properties()), port -> this.veniceClusterWrapper.removeVeniceServer(port), 3);
  }

  @Test(invocationCount = 20, groups = {"flaky"})
  public void testVeniceRouterLeak() {
    testVeniceInstanceLeak(() -> this.veniceClusterWrapper.addVeniceRouter(new Properties()), port -> this.veniceClusterWrapper.removeVeniceRouter(port), 6);
  }

  private <T extends ProcessWrapper> void testVeniceInstanceLeak(Supplier<T> addFunction, Consumer<Integer> removeFunction, int toleratedFDsLeakPerInstance) {
    UnixOperatingSystemMXBean osMXBean = ((UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean());
    LOGGER.info("Max FD Count: " + osMXBean.getMaxFileDescriptorCount());
    long startFDsCount = osMXBean.getOpenFileDescriptorCount();
    LOGGER.info("Start FD Count: " + startFDsCount);
    List<T> instances = new ArrayList<>();
    int numberOfInstances = 10;
    for (int i = 0; i < numberOfInstances; i++) {
      instances.add(addFunction.get());
    }
    instances.stream().forEach(instance -> removeFunction.accept(instance.getPort()));
    long endFDsCount = osMXBean.getOpenFileDescriptorCount();
    LOGGER.info("End FD Count (without explicit GC): " + endFDsCount);
    if (FORCE_GC) {
      System.gc();
      endFDsCount = osMXBean.getOpenFileDescriptorCount();
      LOGGER.info("End FD Count (after explicit GC): " + endFDsCount);
    }
    int toleratedLeaks = numberOfInstances * toleratedFDsLeakPerInstance;
    Assert.assertTrue((endFDsCount - toleratedLeaks) <= startFDsCount,
        "The end FD count (minus tolerated leaks of " + toleratedLeaks + ") should be equal or less than at the beginning! "
            + "Start FD count: " + startFDsCount + ", end FD count: " + endFDsCount + ".");
  }

}