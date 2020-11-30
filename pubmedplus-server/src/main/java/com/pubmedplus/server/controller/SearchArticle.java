package com.pubmedplus.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder.Item;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.TopHitsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.pojo.PubmedArticle;
import com.pubmedplus.server.utils.ElasticSearchUtil;
import com.pubmedplus.server.utils.Util;

import net.sf.json.JSONObject;

@RestController
public class SearchArticle {
	
	@PostMapping("/searchPubmedArticle")
	public String searchPubmedArticle(PubmedArticle pubmedArticle,HttpServletRequest request) throws Exception{
//		var userPhone = request.getHeader("userPhone");
//		if (pubmedArticle.queryStringQuery.length() == 0 && Util.isPhone(userPhone)) {
//			var pmidList = Util.util.pubmedPlusMapper.list_ArticleCollect(userPhone);
//			String pmids[] = (String[]) ConvertUtils.convert(pmidList, String[].class);
//			var res = moreLikeThisPubmedArticle("searchArticle", pmids);
//			if(!JSONObject.fromObject(res).isEmpty()) {
//				return res;
//			}
//		}
		var responseJson = new JSONObject();
		var multiSearchRequest = new MultiSearchRequest();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
		if (Util.isNumeric(pubmedArticle.getQueryStringQuery())) {
			boolQueryBuilder.should(QueryBuilders.termQuery("PMID", pubmedArticle.queryStringQuery));
		}
		boolQueryBuilder.should(QueryBuilders.termQuery("doi", pubmedArticle.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("title.lowercase", pubmedArticle.queryStringQuery));
		multiSearchRequest.add(pubmedArticle.getSearchRequest(boolQueryBuilder));
		boolQueryBuilder = QueryBuilders.boolQuery();
		if(pubmedArticle.queryStringQuery.length()>0) {
			boolQueryBuilder.minimumShouldMatch(1);
			boolQueryBuilder.should(QueryBuilders.queryStringQuery(pubmedArticle.queryStringQuery)
					.field("title").field("titleZh").field("abstractText").field("abstractTextZh")
					.lenient(true));
			var queryStringQuery = QueryParser.escape(pubmedArticle.queryStringQuery);
			boolQueryBuilder.should(QueryBuilders.matchPhraseQuery("title", queryStringQuery).boost(5));
			boolQueryBuilder.should(QueryBuilders.matchPhraseQuery("titleZh", queryStringQuery).boost(5));
			boolQueryBuilder.should(QueryBuilders.matchPhraseQuery("abstractText", queryStringQuery).boost(10));
			boolQueryBuilder.should(QueryBuilders.matchPhraseQuery("abstractTextZh", queryStringQuery).boost(10));
		}
		multiSearchRequest.add(pubmedArticle.getSearchRequest(boolQueryBuilder));
		var searchResponse = Util.util.restHighLevelClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
		org.elasticsearch.action.search.MultiSearchResponse.Item[] responsesItems = searchResponse.getResponses();
		for (org.elasticsearch.action.search.MultiSearchResponse.Item item : responsesItems) {
			if (item.getResponse()!=null&&item.getResponse().getHits().getTotalHits().value > 0) {
				responseJson.put("articleList", ElasticSearchUtil.getSearchElasticsearchData(item.getResponse(), "title","abstractText", "titleZh", "abstractTextZh"));
				break;
			}
		}
		return responseJson.toString();
	}
	
	@GetMapping("/getPmidArticle")
	public String getPmidArticle(String pmid) throws Exception {
		var responseJson = new JSONObject();
		if (Util.isNumeric(pmid)) {
			var getRequest = new GetRequest(Util.SEARCH_PUBMED_INDEX, pmid);
			var searchResponse = Util.util.restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
			responseJson.put("articleDetails", searchResponse.getSourceAsMap());
		}
		return responseJson.toString();
	}
	
	@GetMapping("/moreLikeThisPubmedArticle")
	public String moreLikeThisPubmedArticle(String isType,String... pmid) throws Exception {
		var responseJson = new JSONObject();
		if (pmid.length > 0) {
			var searchRequest = new SearchRequest(Util.SEARCH_PUBMED_INDEX);
			var searchSourceBuilder = new SearchSourceBuilder();
			var includes = "searchArticle".equals(isType)?
					new String[] { "PMID", "title*", "abstractText*", "authorList", "doi", "journal.*", "pubTime","publicationTypeList", "volume", "pagination", "userIsCollectionPhone" }:
					new String[] { "PMID", "title*", "journal.nowIfs", "journal.subName", "pubTime"};
			searchSourceBuilder.fetchSource(includes, null);
			searchSourceBuilder.trackTotalHitsUpTo(10);
			var Items = new Item[pmid.length];
			for (int i = 0; i < pmid.length; i++) {
				Items[i] = new Item(Util.SEARCH_PUBMED_INDEX, pmid[i]);
			}
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
			boolQueryBuilder.filter(QueryBuilders.rangeQuery("pubTime").lte("now"));
			boolQueryBuilder.should(new MoreLikeThisQueryBuilder(new String[] { "title", "abstractText" }, null, Items)
					.maxQueryTerms(50).minTermFreq(1).minimumShouldMatch("searchArticle".equals(isType)?"50%":"30%"));
			boolQueryBuilder.should(new MoreLikeThisQueryBuilder(new String[] { "title", "abstractText" }, null, Items)
					.maxQueryTerms(50).minTermFreq(2).minimumShouldMatch("searchArticle".equals(isType)?"50%":"30%"));
			searchSourceBuilder.query(boolQueryBuilder);
			searchRequest.source(searchSourceBuilder);
			var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
			responseJson.put("articleList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));
		}
		return responseJson.toString();
	}
	
	@PostMapping("/moreLikeThisPubmedArticleAll")
	public String moreLikeThisPubmedArticleAll(PubmedArticle pubmedArticle) throws Exception {
		var responseJson = new JSONObject();
		var searchRequest = new SearchRequest(Util.SEARCH_PUBMED_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.fetchSource(pubmedArticle.getIncludesArticle(), null);
		var fields = "zh".equals(pubmedArticle.getLanguage()) ? new String[] { "titleZh", "abstractTextZh" }: new String[] { "title", "abstractText" };
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
		boolQueryBuilder.filter(QueryBuilders.rangeQuery("pubTime").lte("now"));
		boolQueryBuilder.should(new MoreLikeThisQueryBuilder(fields, pubmedArticle.getSimilaritylikeTexts(),
				pubmedArticle.getSimilarityItem()).maxQueryTerms(50).minTermFreq(1)
						.minimumShouldMatch(pubmedArticle.getSimilarity() + "%"));
		boolQueryBuilder.should(new MoreLikeThisQueryBuilder(fields, pubmedArticle.getSimilaritylikeTexts(),
				pubmedArticle.getSimilarityItem()).maxQueryTerms(50).minTermFreq(2)
						.minimumShouldMatch(pubmedArticle.getSimilarity() + "%"));
		boolQueryBuilder = pubmedArticle.getFilterArticleData(boolQueryBuilder);
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.from((pubmedArticle.getPage() - 1) * 10);
		var sortList = pubmedArticle.getSortArticleData();
		sortList.forEach((sort) -> {
			searchSourceBuilder.sort(sort);
		});
		searchRequest.source(searchSourceBuilder);
		var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		responseJson.put("articleList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));
		return responseJson.toString();
	}
	
	@PostMapping("/moreLikeThisPubmedArticleJournalAll")
	public String moreLikeThisPubmedArticleJournalAll(PubmedArticle pubmedArticle) throws Exception {
		var responseJson = new JSONObject();
		var searchRequest = new SearchRequest(Util.SEARCH_PUBMED_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().fetchSource(false).size(0);
		var fields = "zh".equals(pubmedArticle.getLanguage()) ? new String[] { "titleZh", "abstractTextZh" } : new String[] { "title", "abstractText" };
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
		boolQueryBuilder.filter(QueryBuilders.rangeQuery("pubTime").lte("now"));
		boolQueryBuilder.should(new MoreLikeThisQueryBuilder(fields, pubmedArticle.getSimilaritylikeTexts(),
				pubmedArticle.getSimilarityItem()).maxQueryTerms(50).minTermFreq(1)
						.minimumShouldMatch(pubmedArticle.getSimilarity() + "%"));
		boolQueryBuilder.should(new MoreLikeThisQueryBuilder(fields, pubmedArticle.getSimilaritylikeTexts(),
				pubmedArticle.getSimilarityItem()).maxQueryTerms(50).minTermFreq(2)
						.minimumShouldMatch(pubmedArticle.getSimilarity() + "%"));
		boolQueryBuilder = pubmedArticle.getFilterArticleData(boolQueryBuilder);
		if (pubmedArticle.getNlmId() != null && !pubmedArticle.getNlmId().isBlank()) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("journal.nlmId", pubmedArticle.getNlmId()));
		}
		if (pubmedArticle.getIsSci() == 1) {
			boolQueryBuilder.filter(QueryBuilders.existsQuery("journal.nowIfs"));
		}
		searchSourceBuilder.query(boolQueryBuilder);
		TermsAggregationBuilder termsAggs = AggregationBuilders.terms("journalTermsAggs").field("journal.nlmId").size(1);
		TopHitsAggregationBuilder topHitsAggs = AggregationBuilders.topHits("journalTopHits")
				.fetchSource(pubmedArticle.getIncludesArticle(), null).from((pubmedArticle.getPage() - 1) * 10)
				.size(10);
		if (pubmedArticle.getNlmId() == null) {
			termsAggs.size(pubmedArticle.getPage() * 10);
			topHitsAggs.fetchSource(new String[] { "journal.*" },new String[] { "journal.accept", "journal.direction", "journal.essn", "journal.jcrId","journal.jrId", "journal.qClass", "journal.selfCitationRate", "journal.startYear" });
			topHitsAggs.from(0);
			topHitsAggs.size(1);
			searchSourceBuilder.aggregation(AggregationBuilders.cardinality("journalTotal").field("journal.nlmId"));
			var sort = pubmedArticle.getSort();
			if (sort != null && !sort.isBlank() && sort.split("_").length > 1) {
				var sortName = sort.split("_")[0];
				var sortorder = Integer.valueOf(sort.split("_")[1]);
				if ("articleCount-sort".equals(sortName) && sortorder == -1) {
					termsAggs.order(BucketOrder.count(true));
				} else if ("factor-sort".equals(sortName) && (sortorder == -1 || sortorder == 1)) {
					termsAggs.subAggregation(AggregationBuilders.max("nowIfsMax").field("journal.nowIfs"));
					termsAggs.order(sortorder == 1 ? BucketOrder.aggregation("nowIfsMax", false) : BucketOrder.aggregation("nowIfsMax", true));
				}
			}
		} else {
			var sortList = pubmedArticle.getSortArticleData();
			sortList.forEach((sort) -> {
				topHitsAggs.sort(sort);
			});
		}
		termsAggs.subAggregation(topHitsAggs);
		searchSourceBuilder.aggregation(termsAggs);
		searchRequest.source(searchSourceBuilder);
		var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		responseJson.put("journalList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));
		return responseJson.toString();
	}
	
	
	
	
	
}
