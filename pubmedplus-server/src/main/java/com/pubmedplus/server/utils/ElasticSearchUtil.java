package com.pubmedplus.server.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilters;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.ParsedDateRange;
import org.elasticsearch.search.aggregations.bucket.range.ParsedRange;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.ParsedSignificantStringTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedCardinality;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.TopHitsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ElasticSearchUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchUtil.class);
	
	public static JSONObject getSearchElasticsearchData(SearchResponse searchResponse, String... highlightFieldsArray) {
		var jsonObject = new JSONObject();

		analysisHits(searchResponse.getHits(), jsonObject, highlightFieldsArray);
//		System.out.println(jsonObject.toString());
		var suggest = searchResponse.getSuggest();
		if (suggest != null) {
			var completionTexts = new ArrayList<String>();
			List<? extends Option> suggestionList = null;
			for (var suggestion : suggest) {
				if(suggestion.getEntries().size()==0) {
					continue;
				}
				suggestionList = suggestion.getEntries().get(0).getOptions();
				for (var option : suggestionList) {
					completionTexts.add(option.getText().toString());
				}
			}
			if (completionTexts.size() > 0) {
				Collections.sort(completionTexts, new SortByLengthComparator());
			}
			jsonObject.put("searchCompletion", completionTexts);
		}
		if (searchResponse.getAggregations()!=null) {
			iterationAggregations(searchResponse.getAggregations().asList(),jsonObject);
		}
		jsonObject.put("searchTime", searchResponse.getTook().toString());
		return jsonObject;
	}
	
	public static void iterationAggregations(List<Aggregation> aggregationList,JSONObject responseObject) {
		if (aggregationList == null) {
			return;
		}
		for (Aggregation aggregation : aggregationList) {
			responseObject.put(aggregation.getName(), typeAggregation(aggregation, null));
		}
	}
	
	public static Object typeAggregation(Aggregation aggregation,JSONObject json) {
		var jsonList = new JSONArray();
		if (StringTerms.NAME.equals(aggregation.getType())) {
			var elasticBucket = ((ParsedStringTerms) aggregation);
			for (var Bucket : elasticBucket.getBuckets()) {
				json = json==null?new JSONObject():json;
				json.put("name", Bucket.getKeyAsString());
				json.put("count", Bucket.getDocCount());
				if(Bucket.getAggregations().asList()!=null) {
					iterationAggregations(Bucket.getAggregations().asList(),json);
				}
				jsonList.add(json);
			}
		}else if(RangeAggregationBuilder.NAME.equals(aggregation.getType())){
			var elasticBucket = ((ParsedRange) aggregation);
			for (var Bucket : elasticBucket.getBuckets()) {
				json = json==null?new JSONObject():json;
				json.put("name", Bucket.getKeyAsString());
				json.put("count", Bucket.getDocCount());
				if(Bucket.getAggregations().asList()!=null) {
					iterationAggregations(Bucket.getAggregations().asList(),json);
				}
				jsonList.add(json);
			}
		}else if(DateRangeAggregationBuilder.NAME.equals(aggregation.getType())){
			var elasticBucket = ((ParsedDateRange) aggregation);
			for (var Bucket : elasticBucket.getBuckets()) {
				json = json==null?new JSONObject():json;
				json.put("name", Bucket.getKeyAsString());
				json.put("count", Bucket.getDocCount());
				if(Bucket.getAggregations().asList()!=null) {
					iterationAggregations(Bucket.getAggregations().asList(),json);
				}
				jsonList.add(json);
			}
		}else if(DateHistogramAggregationBuilder.NAME.equals(aggregation.getType())){
			var elasticBucket = ((ParsedDateHistogram) aggregation);
			for (var Bucket : elasticBucket.getBuckets()) {
				json = json==null?new JSONObject():json;
				json.put("name", Bucket.getKeyAsString());
				json.put("count", Bucket.getDocCount());
				if(Bucket.getAggregations().asList()!=null) {
					iterationAggregations(Bucket.getAggregations().asList(),json);
				}
				jsonList.add(json);
			}
		}else if(SignificantStringTerms.NAME.equals(aggregation.getType())){
			var elasticBucket = ((ParsedSignificantStringTerms) aggregation);
			for (var Bucket : elasticBucket.getBuckets()) {
				json = json==null?new JSONObject():json;
				json.put("name", Bucket.getKeyAsString());
				json.put("count", Bucket.getDocCount());
				if(Bucket.getAggregations().asList()!=null) {
					iterationAggregations(Bucket.getAggregations().asList(),json);
				}
				jsonList.add(json);
			}
		}else if(FiltersAggregationBuilder.NAME.equals(aggregation.getType())){
			var elasticBucket = ((ParsedFilters) aggregation);	
			json = json==null?new JSONObject():json;
			for (var buckets : elasticBucket.getBuckets()) {
				if (buckets.getAggregations() == null) {
					continue;
				}
				var subJson = new JSONObject();
				iterationAggregations(buckets.getAggregations().asList(), subJson);
				if (subJson.isEmpty()) {
					continue;
				}
				json.put(buckets.getKeyAsString(), subJson);
			}
			return json;
		}else if(FilterAggregationBuilder.NAME.equals(aggregation.getType())){
			var elasticBucket = ((ParsedFilter) aggregation);	
			json = json==null?new JSONObject():json;
			json.put(elasticBucket.getName(), elasticBucket.getDocCount());
			if(elasticBucket.getAggregations()!=null) {
				iterationAggregations(elasticBucket.getAggregations().asList(),json);
			}
			return json;
		}else if(CardinalityAggregationBuilder.NAME.equals(aggregation.getType())){
			var elasticBucket = ((ParsedCardinality) aggregation);
			json = json==null?new JSONObject():json;
			json.put(elasticBucket.getName(), elasticBucket.getValue());
			return json;
		}else if(SumAggregationBuilder.NAME.equals(aggregation.getType())){
			var elasticBucket = ((ParsedSum) aggregation);
			json = json == null ? new JSONObject() : json;
			json.put(elasticBucket.getName(), elasticBucket.getValue());
			return json;
		}else if(TopHitsAggregationBuilder.NAME.equals(aggregation.getType())){
			var elasticBucket = ((ParsedTopHits) aggregation);
			if(elasticBucket.getHits().getHits().length==0) {
				return null;
			}
			json = json == null ? new JSONObject() : json;
			analysisHits(elasticBucket.getHits(), json);
			return json.isEmpty()?null:json;
		}
		return jsonList;
	}

	public static JSONObject analysisHits(SearchHits searchHits,JSONObject jsonObject, String... highlightFieldsArray) {
		var hits = searchHits.getHits();
//		System.out.println(hits.toString());
		if (hits.length > 0) {
			var jsonArray = new JSONArray();
			Map<String, Object> responseMap = null;
			Map<String, HighlightField> highlightFields = null;
			StringBuffer sb = null;
			HighlightField highlightField = null;
			Text[] texts = null;
			for (SearchHit hit : searchHits) {
				responseMap = hit.getSourceAsMap();
				if (hit.getHighlightFields().size() > 0) {
					highlightFields = hit.getHighlightFields();
					for (var highlightFieldsStr : highlightFieldsArray) {
						if (highlightFields.get(highlightFieldsStr) != null) {
							sb = new StringBuffer();
							highlightField = highlightFields.get(highlightFieldsStr);
							texts = highlightField.getFragments();
							for (var text : texts) {
								sb.append(text);
							}
							responseMap.put(highlightFieldsStr.split("\\.")[0], sb.toString());
						}
					}
				}
				jsonArray.add(JSONObject.fromObject(responseMap));
			}
			jsonObject.put("searchData", jsonArray);
			jsonObject.put("searchTotal", searchHits.getTotalHits().value);
		}
		return jsonObject;
	}

	public static BulkProcessor getEsBulkProcessor() {
		BulkProcessor.Listener listener = new BulkProcessor.Listener() {
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
				if (response.hasFailures()) {
					LOG.error("添加es数据失败:" + response.buildFailureMessage());
				} else {
					LOG.info("添加es数据成功,耗时:" + response.getTook().getMillis());
				}
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
				LOG.error("添加es数据错误:" + failure);
			}
		};

		BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer = (request,
				bulkListener) -> Util.util.restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
		BulkProcessor bulkProcessor = BulkProcessor.builder(bulkConsumer, listener)
				.setBulkActions(1000)
				.setBulkSize(new ByteSizeValue(500, ByteSizeUnit.MB))
				.setConcurrentRequests(3)
				.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L), 3)).build();
		return bulkProcessor;
	}

	public static void closeBulkProcessor(BulkProcessor bulkProcessor) {
		try {
			bulkProcessor.flush();
			var terminated = bulkProcessor.awaitClose(600, TimeUnit.SECONDS);
			if (terminated) {
				LOG.info("bulkProcessor关闭成功:");
			} else {
				LOG.error("bulkProcessor关闭失败:");
			}
		} catch (Exception e) {
			LOG.error("bulkProcessor关闭报错:" + e.getMessage());
		}
	}
	
	public static void refreshIndex(String indexName) {
		try {
			var refreshRequest = new RefreshRequest(indexName);
			var refreshResponse = Util.util.restHighLevelClient.indices().refresh(refreshRequest,  RequestOptions.DEFAULT);
			var totalShards = refreshResponse.getTotalShards(); 
			var successfulShards = refreshResponse.getSuccessfulShards(); 
			var failedShards = refreshResponse.getFailedShards();
			LOG.info("刷新索引成功,刷新请求命中的分片总数:"+totalShards+",刷新成功的分片数:"+successfulShards+",刷新失败的分片数:"+failedShards);
		} catch (Exception e) {
			LOG.error("刷新索引异常:"+e.getMessage());
		}
	}


}
