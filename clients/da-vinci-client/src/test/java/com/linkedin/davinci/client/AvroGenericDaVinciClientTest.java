package com.linkedin.davinci.client;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.linkedin.venice.meta.ReadOnlySchemaRepository;
import com.linkedin.venice.schema.SchemaEntry;
import org.apache.avro.Schema;
import org.testng.Assert;
import org.testng.annotations.Test;


public class AvroGenericDaVinciClientTest {
  @Test
  public void testGetValueSchemaIdForComputeRequest() {
    String storeName = "test_store";
    ReadOnlySchemaRepository repo = mock(ReadOnlySchemaRepository.class);

    Schema computeValueSchema = mock(Schema.class);
    String computeValueSchemaString = "compute_value_schema";
    doReturn(computeValueSchemaString).when(computeValueSchema).toString();
    int valueSchemaId = 1;
    doReturn(new SchemaEntry(valueSchemaId, computeValueSchema)).when(repo).getSupersetOrLatestValueSchema(storeName);
    Assert.assertEquals(
        AvroGenericDaVinciClient.getValueSchemaIdForComputeRequest(storeName, computeValueSchema, repo),
        1);

    // mismatch scenario
    Schema latestValueSchema = mock(Schema.class);
    int latestValueSchemaId = 2;
    doReturn(new SchemaEntry(latestValueSchemaId, latestValueSchema)).when(repo)
        .getSupersetOrLatestValueSchema(storeName);
    doReturn(valueSchemaId).when(repo).getValueSchemaId(storeName, computeValueSchemaString);
    Assert.assertEquals(
        AvroGenericDaVinciClient.getValueSchemaIdForComputeRequest(storeName, computeValueSchema, repo),
        1);
    verify(repo).getValueSchemaId(storeName, computeValueSchemaString);
  }
}
