package com.pubmedplus.server.controller;

import javax.validation.constraints.NotBlank;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.pojo.PubmedFund;
import com.pubmedplus.server.utils.ElasticSearchUtil;
import com.pubmedplus.server.utils.Util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@Validated
public class SearchFund {

	@PostMapping("/searchFund")
	public String searchFund(PubmedFund pubmedFund) throws Exception {
		var responseJson = new JSONObject();
		var multiSearchRequest = new MultiSearchRequest();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
		boolQueryBuilder.should(QueryBuilders.termQuery("title.keyword", pubmedFund.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("approvalNumber", pubmedFund.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("type", pubmedFund.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("studentsName", pubmedFund.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("unit", pubmedFund.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("studentsAll", pubmedFund.queryStringQuery));
		multiSearchRequest.add(pubmedFund.getSearchRequest(boolQueryBuilder));
		boolQueryBuilder = QueryBuilders.boolQuery();
		if(pubmedFund.getQueryStringQuery().length()>0) {
			boolQueryBuilder.minimumShouldMatch(1);
			boolQueryBuilder.should(QueryBuilders.queryStringQuery(pubmedFund.queryStringQuery).field("title").lenient(true));
			boolQueryBuilder.should(QueryBuilders.matchPhraseQuery("title", QueryParser.escape(pubmedFund.queryStringQuery)).boost(10));
		}
		multiSearchRequest.add(pubmedFund.getSearchRequest(boolQueryBuilder));
		var searchResponse = Util.util.restHighLevelClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
		Item[] responsesItems=searchResponse.getResponses();
		for (Item item : responsesItems) {
			if (item.getResponse() != null && item.getResponse().getHits().getTotalHits().value > 0) {
				responseJson.put("fundList", ElasticSearchUtil.getSearchElasticsearchData(item.getResponse(), "title"));
				break;
			}
		}
		return responseJson.toString();
	}
	
	@GetMapping("/getFundInfo")
	public String getFundInfo(@NotBlank String number) throws Exception {
		var responseJson = new JSONObject();
		var getRequest = new GetRequest(Util.SEARCH_FUND_INDEX, number.trim());
		var searchResponse = Util.util.restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
		var sourceAsMap = searchResponse.getSourceAsMap();
		if(sourceAsMap.size()==0) {
			return responseJson.toString();
		}
		responseJson.put("fundDetails", sourceAsMap);
		
		var multiSearchRequest = new MultiSearchRequest();
		var searchRequest = new SearchRequest(Util.SEARCH_FUND_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().size(0);
		if (sourceAsMap != null && sourceAsMap.containsKey("studentsAll")) {
			var studentsAllList = JSONArray.fromObject(sourceAsMap.get("studentsAll"));
			for (var students : studentsAllList) {
			searchSourceBuilder.aggregation(AggregationBuilders.filter(students.toString(), QueryBuilders.termQuery("studentsAll", students))
					.subAggregation(AggregationBuilders.topHits("fund").fetchSource(new String[] {"title", "approvalNumber"}, null).size(100)));
			}
		}
		searchRequest.source(searchSourceBuilder);
		multiSearchRequest.add(searchRequest);
		
		searchRequest = new SearchRequest(Util.SEARCH_FUND_INDEX);
		searchSourceBuilder = new SearchSourceBuilder().size(10).fetchSource(new String[] {"title","approvalNumber"}, null);
		searchSourceBuilder.query(QueryBuilders.moreLikeThisQuery(new String[] {"title"},null
				,new org.elasticsearch.index.query.MoreLikeThisQueryBuilder.Item[] {
						new org.elasticsearch.index.query.MoreLikeThisQueryBuilder.Item(Util.SEARCH_FUND_INDEX, number)
				}).maxQueryTerms(100).minTermFreq(1));
		searchRequest.source(searchSourceBuilder);
		multiSearchRequest.add(searchRequest);
		
		searchRequest = new SearchRequest(Util.SEARCH_PUBMED_INDEX);
		searchSourceBuilder = new SearchSourceBuilder().size(100)
				.fetchSource(new String[] { "PMID", "title", "abstractText", "authorList", "doi", "journal.*", "pubTime", "publicationTypeList", "volume", "pagination" }, null);
		searchSourceBuilder.query(QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("fundNumber", number)));
		searchRequest.source(searchSourceBuilder);
		multiSearchRequest.add(searchRequest);

		var multiSearchResponse = Util.util.restHighLevelClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
		responseJson.put("aggs", ElasticSearchUtil.getSearchElasticsearchData(multiSearchResponse.getResponses()[0].getResponse()));
		responseJson.put("fundList", ElasticSearchUtil.getSearchElasticsearchData(multiSearchResponse.getResponses()[1].getResponse()));
		responseJson.put("articleList", ElasticSearchUtil.getSearchElasticsearchData(multiSearchResponse.getResponses()[2].getResponse()));
		return responseJson.toString();
	}
	
}
