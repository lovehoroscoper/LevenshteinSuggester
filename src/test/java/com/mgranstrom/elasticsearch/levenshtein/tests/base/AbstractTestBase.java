package com.mgranstrom.elasticsearch.levenshtein.tests.base;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public abstract class AbstractTestBase {

    /**
     * Define a unique index name
     * @return The unique index name (could be this.getClass().getSimpleName())
     */
    protected String indexName() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    /**
     * Define the waiting time in seconds before launching a test
     * @return Waiting time (in seconds)
     */
    abstract public long waitingTime() throws Exception;

    protected HttpClient getHttpClient(){
        return new HttpClient("http://localhost:9200");
    }

    protected static Node node;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        if (node == null) {
            // First we delete old datas...
            File dataDir = new File("./target/es/data");
            if(dataDir.exists()) {
                FileSystemUtils.deleteRecursively(dataDir, true);
            }
            // Then we start our node for tests
            node = NodeBuilder
                    .nodeBuilder()
                    .settings(
                            ImmutableSettings.settingsBuilder()
                                    .put("gateway.type", "local")
                                    .put("path.data", "./target/es/data")
                                    .put("path.logs", "./target/es/logs")
                                    .put("path.work", "./target/es/work")
                    ).node();

            // We wait now for the yellow (or green) status
            node.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
        }
    }

    @Before
    public void setUp() throws Exception {
        // We delete the index before we start any test
        try {
            node.client().admin().indices().delete(new DeleteIndexRequest(indexName())).actionGet();
            // We wait for one second to let ES delete the index
            Thread.sleep(1000);
        } catch (IndexMissingException e) {
            // Index does not exist... Fine
        }

        // Creating the index with mappings
        node.client().admin().indices().create(new CreateIndexRequest(indexName())).actionGet();
        Thread.sleep(1000);

    }
}