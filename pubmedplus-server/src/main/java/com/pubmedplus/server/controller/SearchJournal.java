package com.pubmedplus.server.controller;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.pojo.PubmedJournal;
import com.pubmedplus.server.utils.ElasticSearchUtil;
import com.pubmedplus.server.utils.Util;

import net.sf.json.JSONObject;

@RestController
@Validated
public class SearchJournal {
	
	@PostMapping("/searchPubmedJournal")
	public String searchPubmedJournal(PubmedJournal pubmedJournal) throws Exception {
		var responseJson = new JSONObject();
		var multiSearchRequest = new MultiSearchRequest();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
		boolQueryBuilder.should(QueryBuilders.termQuery("titleAll", pubmedJournal.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("issn", pubmedJournal.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("essn", pubmedJournal.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("nlmId", pubmedJournal.queryStringQuery));
		multiSearchRequest.add(pubmedJournal.getSearchRequest(boolQueryBuilder));
		boolQueryBuilder = QueryBuilders.boolQuery();
		if(pubmedJournal.getQueryStringQuery().trim().length()>0) {
			boolQueryBuilder.minimumShouldMatch(1);
			boolQueryBuilder.should(QueryBuilders.queryStringQuery(pubmedJournal.queryStringQuery)
					.field("fullName").field("subName")
					.lenient(true));
			var queryStringQuery = QueryParser.escape(pubmedJournal.queryStringQuery);
			boolQueryBuilder.should(QueryBuilders.matchPhraseQuery("fullName", queryStringQuery).boost(10));
			boolQueryBuilder.should(QueryBuilders.matchPhraseQuery("subName", queryStringQuery).boost(5));
		}
		multiSearchRequest.add(pubmedJournal.getSearchRequest(boolQueryBuilder));
//		System.out.println(multiSearchRequest.toString());
		var searchResponse = Util.util.restHighLevelClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
		Item[] responsesItems=searchResponse.getResponses();
		for (Item item : responsesItems) {
			if (item.getResponse() != null && item.getResponse().getHits().getTotalHits().value > 0) {
				responseJson.put("journalList", ElasticSearchUtil.getSearchElasticsearchData(item.getResponse(), "fullName", "subName"));
				break;
			}
		}
		return responseJson.toString();
	}
	
	@PostMapping("/getPubmedJournalId")
	public String getPubmedJournalId(@NotBlank String journalId,@NotNull String typeTop) throws Exception {
		var responseJson = new JSONObject();
		var multiSearchRequest = new MultiSearchRequest();

		multiSearchRequest.add(new SearchRequest(Util.SEARCH_JOURNAL_INDEX).source(new SearchSourceBuilder()
				.query(QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("nlmId", journalId)))));

		multiSearchRequest.add(new SearchRequest(Util.SEARCH_JOURNAL_INDEX).source(
				new SearchSourceBuilder().size(10).fetchSource(new String[] { "nlmId", "subName", "course" }, null)
						.query(QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("course", typeTop)))
						.sort("nowIfs", SortOrder.DESC)));

		multiSearchRequest.add(new SearchRequest(Util.SEARCH_PUBMED_INDEX).source(new SearchSourceBuilder().size(0)
				.query(QueryBuilders.termQuery("journal.nlmId", journalId)).aggregation(AggregationBuilders.dateRange("nearly12YearsAggs").field("pubTime").format("yyyy").addRange("now-12y/y", "now")
						.subAggregation(AggregationBuilders.dateHistogram("yearTimeAggs").field("pubTime").calendarInterval(DateHistogramInterval.YEAR).format("yyyy")
								.subAggregation(AggregationBuilders.filter("chinaAggs", QueryBuilders.termQuery("pubCountry", "China")))))));

		var searchResponse = Util.util.restHighLevelClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
		responseJson.put("journalDetails",ElasticSearchUtil.getSearchElasticsearchData(searchResponse.getResponses()[0].getResponse()));
		responseJson.put("journalTypeTop",ElasticSearchUtil.getSearchElasticsearchData(searchResponse.getResponses()[1].getResponse()));
		responseJson.put("articleYearAggs",ElasticSearchUtil.getSearchElasticsearchData(searchResponse.getResponses()[2].getResponse()));
		return responseJson.toString();
	}

	/**
	 *获取杂志高级搜索所需要的聚合下拉数据
	 * @param requestJson
	 * @param logJson
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/getPubmedJournalAdvancedAggs")
	public String getPubmedJournalAdvancedAggs() throws Exception {
		var responseJson = new JSONObject();
		var searchRequest = new SearchRequest(Util.SEARCH_JOURNAL_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().size(0);
		searchSourceBuilder.aggregation(AggregationBuilders.terms("courseAggs").field("course").size(100000));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("subCourseAggs").field("subCourse").size(100000));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("directionAggs").field("direction").size(100000));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("subjectAggs").field("subject").size(100000));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("typeAggs").field("type").size(100000));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("territoryAggs").field("territory").size(100000));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("publishingCycleAggs").field("publishingCycle").size(100000));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("countryZhAggs").field("countryZh").size(100000));
		searchRequest.source(searchSourceBuilder);
		var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		responseJson.put("aggs", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));
		return responseJson.toString();
	}
	

}
