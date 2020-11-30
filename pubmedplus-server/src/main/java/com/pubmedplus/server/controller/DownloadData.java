package com.pubmedplus.server.controller;


import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.pojo.PubmedArticle;
import com.pubmedplus.server.utils.ElasticSearchUtil;
import com.pubmedplus.server.utils.Util;

import net.sf.json.JSONObject;

@RestController
public class DownloadData {

	@PostMapping("/downloadPubmedArticleAll")
	public String downloadPubmedArticleAll(PubmedArticle pubmedArticle) throws Exception {
		var responseJson = new JSONObject();
		var multiSearchRequest = new MultiSearchRequest();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
		if (Util.isNumeric(pubmedArticle.getQueryStringQuery())) {
			boolQueryBuilder.should(QueryBuilders.termQuery("PMID", pubmedArticle.getQueryStringQuery()));
		}
		boolQueryBuilder.should(QueryBuilders.termQuery("doi", pubmedArticle.getQueryStringQuery()));
		boolQueryBuilder.should(QueryBuilders.termQuery("title.lowercase", pubmedArticle.getQueryStringQuery()));
		multiSearchRequest.add(getAggsArticleSearchRequest(boolQueryBuilder,pubmedArticle,pubmedArticle.getQueryStringQuery()));
		boolQueryBuilder = QueryBuilders.boolQuery();
		if(pubmedArticle.getQueryStringQuery().length()>0) {
			boolQueryBuilder.must(QueryBuilders.queryStringQuery(pubmedArticle.getQueryStringQuery())
					.field("title").field("titleZh").field("abstractText").field("abstractTextZh")
					.lenient(true));
		}
		multiSearchRequest.add(getAggsArticleSearchRequest(boolQueryBuilder,pubmedArticle,pubmedArticle.getQueryStringQuery()));
		var searchResponse = Util.util.restHighLevelClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
		org.elasticsearch.action.search.MultiSearchResponse.Item[] responsesItems = searchResponse.getResponses();
		for (org.elasticsearch.action.search.MultiSearchResponse.Item item : responsesItems) {
			if (item.getResponse()!=null&&item.getResponse().getHits().getTotalHits().value > 0) {
				responseJson.put("journalList", ElasticSearchUtil.getSearchElasticsearchData(item.getResponse(), "title","abstractText", "titleZh", "abstractTextZh"));
				break;
			}
		}
		return responseJson.toString();
	}
	
	@PostMapping("/downloadMoreLikeThisPubmedArticleAll")
	public String downloadMoreLikeThisPubmedArticleAll(PubmedArticle pubmedArticle) throws Exception {
		var responseJson = new JSONObject();
		var fields = "zh".equals(pubmedArticle.getLanguage()) ? new String[] { "titleZh", "abstractTextZh" }: new String[] { "title", "abstractText" };
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
		boolQueryBuilder.filter(QueryBuilders.rangeQuery("pubTime").lte("now"));
		boolQueryBuilder.should(new MoreLikeThisQueryBuilder(fields, pubmedArticle.getSimilaritylikeTexts(),
				pubmedArticle.getSimilarityItem()).maxQueryTerms(50).minTermFreq(1)
						.minimumShouldMatch(pubmedArticle.getSimilarity() + "%"));
		boolQueryBuilder.should(new MoreLikeThisQueryBuilder(fields, pubmedArticle.getSimilaritylikeTexts(),
				pubmedArticle.getSimilarityItem()).maxQueryTerms(50).minTermFreq(2)
						.minimumShouldMatch(pubmedArticle.getSimilarity() + "%"));
		var searchResponse = Util.util.restHighLevelClient.search(getAggsArticleSearchRequest(boolQueryBuilder,pubmedArticle,pubmedArticle.getQuery()), RequestOptions.DEFAULT);
		responseJson.put("journalList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));
		return responseJson.toString();
	}
	
	public SearchRequest getAggsArticleSearchRequest(BoolQueryBuilder boolQueryBuilder,PubmedArticle pubmedArticle,String query){
		var searchRequest = new SearchRequest(Util.SEARCH_PUBMED_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().size(0);
		if(query.length()==0) {
			boolQueryBuilder.filter(QueryBuilders.rangeQuery("pubTime").gte("now/d").lte("now/d"));
		}else {
			boolQueryBuilder.filter(QueryBuilders.rangeQuery("pubTime").lte("now"));
		}
		boolQueryBuilder = pubmedArticle.getFilterArticleData(boolQueryBuilder);
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.aggregation(AggregationBuilders.filter("ifsJournalAggs", QueryBuilders.existsQuery("journal.nowIfs"))
				.subAggregation(AggregationBuilders.terms("journalAggs").field("journal.nlmId").size(Integer.MAX_VALUE)
				.subAggregation(AggregationBuilders.topHits("journalData").fetchSource("journal.*", null).size(1))));
		searchRequest.source(searchSourceBuilder);
		return searchRequest;
	}
	
}
