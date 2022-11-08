package org.duo.crud;

import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.Test;

import java.io.IOException;

public class crudOp {

    @Test
    public void createIndex() throws IOException {

        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("server01", 9200, "http"),
                        new HttpHost("server02", 9200, "http"),
                        new HttpHost("server03", 9200, "http")));

        // request对象
        CreateIndexRequest request = new CreateIndexRequest("twitter");

        // 组装数据
        // setting & mapping
        request.source("{\n" +
                "    \"settings\" : {\n" +
                "        \"number_of_shards\" : 1,\n" +
                "        \"number_of_replicas\" : 0\n" +
                "    },\n" +
                "    \"mappings\" : {\n" +
                "        \"properties\" : {\n" +
                "            \"message\" : { \"type\" : \"text\" }\n" +
                "        }\n" +
                "    }\n" +
                "}", XContentType.JSON);


        // 同步
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        // 是否所有节点都已确认
        createIndexResponse.isAcknowledged();
        // 在超时之前是否为索引中的每个碎片启动所需数量的碎片副本
        createIndexResponse.isShardsAcknowledged();
        client.close();
    }

    @Test
    public void getIndex() throws IOException {

        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("server01", 9200, "http"),
                        new HttpHost("server02", 9200, "http"),
                        new HttpHost("server03", 9200, "http")));

        GetIndexRequest request = new GetIndexRequest("product*");
        GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
        String[] indices = response.getIndices();
        for (String indexName : indices) {
            System.out.println("index name:" + indexName);
        }
        client.close();
    }


    @Test
    public void delIndex() throws IOException {

        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("server01", 9200, "http"),
                        new HttpHost("server02", 9200, "http"),
                        new HttpHost("server03", 9200, "http")));

        DeleteIndexRequest request = new DeleteIndexRequest("test_index");
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        if (response.isAcknowledged()) {
            System.out.println("删除index成功");
        } else {
            System.out.println("删除index失败");
        }
        client.close();
    }

    @Test
    public void insertDoc() throws IOException {

        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("server01", 9200, "http"),
                        new HttpHost("server02", 9200, "http"),
                        new HttpHost("server03", 9200, "http")));

        IndexRequest request = new IndexRequest("product");
        Product product = new Product();

        product.setId("18");
        product.setName("huawei");
        product.setType("mobile phone");
        product.setPrice(3999);

        Gson gson = new Gson();
        request.id(product.getId());
        request.source(gson.toJson(product), XContentType.JSON);

        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        System.out.println(response);
        client.close();
    }

    @Test
    public void bulkInsertDoc() throws IOException {

        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("server01", 9200, "http"),
                        new HttpHost("server02", 9200, "http"),
                        new HttpHost("server03", 9200, "http")));


        BulkRequest request = new BulkRequest("product");

        Gson gson = new Gson();
        Product product = new Product();
        product.setName("huawei");
        product.setType("mobile phone");
        for (int i = 0; i < 10; i++) {
            product.setName("bulk_name_" + 0);
            product.setPrice(4567 + i);
            request.add(new IndexRequest().id(Integer.toString(20 + i)).source(gson.toJson(product), XContentType.JSON));
        }
        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println("数量：" + response.getItems().length);
        client.close();
    }


    @Test
    public void getById() throws IOException {

        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("server01", 9200, "http"),
                        new HttpHost("server02", 9200, "http"),
                        new HttpHost("server03", 9200, "http")));

        GetRequest request = new GetRequest("product", "22");

        //组装数据
        String[] includes = {"name", "price"};
        String[] excludes = {"desc"};
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
        // 只查询特定字段，如果需要查询所有字段则不设置该项。
        request.fetchSourceContext(fetchSourceContext);

        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        System.out.println(response);
        client.close();
    }

    @Test
    public void delById() throws IOException {

        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("server01", 9200, "http"),
                        new HttpHost("server02", 9200, "http"),
                        new HttpHost("server03", 9200, "http")));

        DeleteRequest request = new DeleteRequest("product", "29");
        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(response);
        client.close();
    }

    @Test
    public void multiGetById() throws IOException {

        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("server01", 9200, "http"),
                        new HttpHost("server02", 9200, "http"),
                        new HttpHost("server03", 9200, "http")));

        MultiGetRequest request = new MultiGetRequest();
        request.add("product", "1");
        request.add("product", "2");
        request.add("product", "4");

        MultiGetResponse response = client.mget(request, RequestOptions.DEFAULT);
        for (MultiGetItemResponse itemResponse : response) {
            System.out.println(itemResponse.getResponse().getSourceAsString());
        }
        client.close();
    }

    @Test
    public void updateById() throws IOException {

        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("server01", 9200, "http"),
                        new HttpHost("server02", 9200, "http"),
                        new HttpHost("server03", 9200, "http")));

        UpdateRequest request = new UpdateRequest("product", "20");
        Product product = new Product();
        product.setPrice(8888);

        Gson gson = new Gson();

        request.doc(gson.toJson(product), XContentType.JSON);

        UpdateResponse update = client.update(request, RequestOptions.DEFAULT);
        System.out.println("更新状态：" + update.status());
        System.out.println(update);

    }
}
