package com.pubmedplus.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.pojo.PubmedArticle;
import com.pubmedplus.server.pojo.PubmedFund;
import com.pubmedplus.server.utils.ElasticSearchUtil;
import com.pubmedplus.server.utils.Util;

import net.sf.json.JSONObject;

@RestController
public class Statistics {

	@PostMapping("/statisticsArticle")
	public String statisticsArticle(PubmedArticle pubmedArticle,HttpServletRequest request) throws Exception {
		var responseJson = new JSONObject();
		var multiSearchRequest = new MultiSearchRequest();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
		if (Util.isNumeric(pubmedArticle.getQueryStringQuery())) {
			boolQueryBuilder.should(QueryBuilders.termQuery("PMID", pubmedArticle.getQueryStringQuery()));
		}
		boolQueryBuilder.should(QueryBuilders.termQuery("doi", pubmedArticle.getQueryStringQuery()));
		boolQueryBuilder.should(QueryBuilders.termQuery("title.lowercase", pubmedArticle.getQueryStringQuery()));
		multiSearchRequest.add(getStatisticsArticleSearchRequest(boolQueryBuilder,pubmedArticle));
		
		boolQueryBuilder = QueryBuilders.boolQuery();
		if(pubmedArticle.getQueryStringQuery().length()>0) {
			boolQueryBuilder.must(QueryBuilders.queryStringQuery(pubmedArticle.getQueryStringQuery())
					.field("title").field("titleZh").field("abstractText").field("abstractTextZh")
					.lenient(true));
		}
		multiSearchRequest.add(getStatisticsArticleSearchRequest(boolQueryBuilder,pubmedArticle));
		
//		var userPhone = request.getHeader("userPhone");
//		if (pubmedArticle.queryStringQuery.length() == 0 && Util.isPhone(userPhone)) {
//			pubmedArticle.setQueryStringQuery("user");
//			boolQueryBuilder = QueryBuilders.boolQuery();
//			var pmidList = Util.util.pubmedPlusMapper.list_ArticleCollect(userPhone);
//			boolQueryBuilder.must(QueryBuilders.termsQuery("PMID", pmidList));
//			multiSearchRequest.add(getStatisticsArticleSearchRequest(boolQueryBuilder,pubmedArticle));
//		}
		
		var searchResponse = Util.util.restHighLevelClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
		Item[] responsesItems=searchResponse.getResponses();
		for (Item item : responsesItems) {
			if (item.getResponse() != null && item.getResponse().getHits().getTotalHits().value > 0) {
				responseJson.put("aggs", ElasticSearchUtil.getSearchElasticsearchData(item.getResponse(), "title"));
				break;
			}
		}
		return responseJson.toString();
	}
	
	public SearchRequest getStatisticsArticleSearchRequest(BoolQueryBuilder boolQueryBuilder,PubmedArticle pubmedArticle){
		var searchRequest = new SearchRequest(Util.SEARCH_PUBMED_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().size(0);
		if(pubmedArticle.getQueryStringQuery().trim().length()==0) {
			boolQueryBuilder.filter(QueryBuilders.rangeQuery("pubTime").gte("now-10d/d").lte("now/d"));
		}else {
			boolQueryBuilder.filter(QueryBuilders.rangeQuery("pubTime").lte("now"));
		}
		boolQueryBuilder = pubmedArticle.getFilterArticleData(boolQueryBuilder);
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.aggregation(AggregationBuilders.terms("journalAggs").field("journal.fullName").size(pubmedArticle.getSize())
				.subAggregation(AggregationBuilders.filter("chinaAggs", QueryBuilders.termQuery("pubCountry", "China"))));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("firstAuthorAggs").field("firstAuthor").size(pubmedArticle.getSize()));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("lastAuthorAggs").field("lastAuthor").size(pubmedArticle.getSize()));
		searchSourceBuilder.aggregation(AggregationBuilders.range("ifsAggs").field("journal.nowIfs")
				.addRange(0,1).addRange(1,2).addRange(2,3).addRange(3,4).addRange(4,5).addRange(5,10).addRange(10,20).addRange(20,500)
				.subAggregation(AggregationBuilders.filter("chinaAggs", QueryBuilders.termQuery("pubCountry", "China"))));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("keywordAggs").field("keyword").size(pubmedArticle.getSize()));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("meshAggs").field("mesheading").size(pubmedArticle.getSize()));
		searchSourceBuilder.aggregation(AggregationBuilders.dateRange("nearly7YearsAggs").field("pubTime").format("yyyy").addRange("now-7y/y", "now")
				.subAggregation(AggregationBuilders.dateHistogram("yearTimeAggs").field("pubTime").calendarInterval(DateHistogramInterval.YEAR).format("yyyy")
				.subAggregation(AggregationBuilders.filter("chinaAggs", QueryBuilders.termQuery("pubCountry", "China")))));
		searchSourceBuilder.aggregation(AggregationBuilders.filter("chinaPubAggs", QueryBuilders.termQuery("pubCountry", "China"))
				.subAggregation(AggregationBuilders.terms("journalAggs").field("journal.fullName").size(pubmedArticle.getSize())));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("pubCountryAggs").field("pubCountry").size(pubmedArticle.getSize()));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("courseAggs").field("journal.course").size(pubmedArticle.getSize()));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("directionAggs").field("journal.direction").size(pubmedArticle.getSize()));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("qClassAggs").field("journal.qClass").size(pubmedArticle.getSize()));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("areaAggs").field("journal.area").size(pubmedArticle.getSize()));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("publicationTypeAggs").field("publicationType").size(pubmedArticle.getSize()));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("authorAggs").field("author").size(pubmedArticle.getSize()));
		searchRequest.source(searchSourceBuilder);
		return searchRequest;
	}

	@PostMapping("/statisticsFund")
	public String statisticsFund(PubmedFund pubmedFund) throws Exception {
		var responseJson = new JSONObject();
		var multiSearchRequest = new MultiSearchRequest();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		boolQueryBuilder.should(QueryBuilders.termQuery("title.lowercase", pubmedFund.getQueryStringQuery()));
		boolQueryBuilder.should(QueryBuilders.termQuery("approvalNumber", pubmedFund.getQueryStringQuery()));
		boolQueryBuilder.should(QueryBuilders.termQuery("studentsName", pubmedFund.getQueryStringQuery()));
		boolQueryBuilder.should(QueryBuilders.termQuery("unit", pubmedFund.getQueryStringQuery()));
		boolQueryBuilder.minimumShouldMatch(1);
		multiSearchRequest.add(getStatisticsFundSearchRequest(boolQueryBuilder,pubmedFund));
		boolQueryBuilder = QueryBuilders.boolQuery();
		if (pubmedFund.getQueryStringQuery().length() > 0) {
			boolQueryBuilder.must(QueryBuilders.queryStringQuery(pubmedFund.getQueryStringQuery()).field("title")
					.fuzziness(Fuzziness.ZERO)
					.fuzzyTranspositions(false).autoGenerateSynonymsPhraseQuery(false));
		}
		multiSearchRequest.add(getStatisticsFundSearchRequest(boolQueryBuilder,pubmedFund));
		var searchResponse = Util.util.restHighLevelClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
		Item[] responsesItems=searchResponse.getResponses();
		for (Item item : responsesItems) {
			if (item.getResponse() != null && item.getResponse().getHits().getTotalHits().value > 0) {
				responseJson.put("aggs", ElasticSearchUtil.getSearchElasticsearchData(item.getResponse(), "title"));
				break;
			}
		}
		return responseJson.toString();
	}
	
	public SearchRequest getStatisticsFundSearchRequest(BoolQueryBuilder boolQueryBuilder,PubmedFund pubmedFund){
		var searchRequest = new SearchRequest(Util.SEARCH_FUND_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().size(0);
		boolQueryBuilder = pubmedFund.getFilterFundData(boolQueryBuilder);
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.aggregation(AggregationBuilders.significantText("titleAggs", "title").size(pubmedFund.getSize()));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("departmentAggs").field("department").size(pubmedFund.getSize())
				.subAggregation(AggregationBuilders.sum("moneySum").field("money")));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("unitAggs").field("unit").size(pubmedFund.getSize())
				.subAggregation(AggregationBuilders.sum("moneySum").field("money")));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("studentsNameAggs").field("studentsName").size(pubmedFund.getSize())
				.subAggregation(AggregationBuilders.sum("moneySum").field("money")));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("typeAggs").field("type").size(pubmedFund.getSize())
				.subAggregation(AggregationBuilders.sum("moneySum").field("money")));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("subjectOneAggs").field("subjectOne").size(pubmedFund.getSize())
				.subAggregation(AggregationBuilders.sum("moneySum").field("money")));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("subjectTwoAggs").field("subjectTwo").size(pubmedFund.getSize())
				.subAggregation(AggregationBuilders.sum("moneySum").field("money")));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("subjectThreeAggs").field("subjectThree").size(pubmedFund.getSize())
				.subAggregation(AggregationBuilders.sum("moneySum").field("money")));
		searchSourceBuilder.aggregation(AggregationBuilders.dateRange("nearly10YearsAggs").field("approvalYear").format("yyyy").addRange("now-10y/y", "now")
				.subAggregation(AggregationBuilders.dateHistogram("yearAggs").field("approvalYear").calendarInterval(DateHistogramInterval.YEAR).format("yyyy")
				.subAggregation(AggregationBuilders.sum("moneySum").field("money"))));
		searchRequest.source(searchSourceBuilder);
		return searchRequest;
	}
	
	
}
