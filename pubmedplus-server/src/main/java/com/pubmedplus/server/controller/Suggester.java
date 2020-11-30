package com.pubmedplus.server.controller;

import javax.validation.constraints.NotBlank;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.utils.ElasticSearchUtil;
import com.pubmedplus.server.utils.Util;

import net.sf.json.JSONObject;

@RestController
@Validated
public class Suggester {
	
	@GetMapping("/completionJournalSuggester")
	public String completionJournalSuggester(@NotBlank String query) throws Exception {
		var responseJson = new JSONObject();
		var searchRequest = new SearchRequest(Util.SEARCH_JOURNAL_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().fetchSource("suggest", null);
		var suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("journal-completion-suggest", SuggestBuilders.completionSuggestion("fullName.completion").prefix(query.trim()));
		searchSourceBuilder.suggest(suggestBuilder);
		searchRequest.source(searchSourceBuilder);
		var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		responseJson.put("completionList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));
		return responseJson.toString();
	}
	
	@GetMapping("/completionFundSuggester")
	public String completionFundSuggester(@NotBlank String query) throws Exception {
		var responseJson = new JSONObject();
		var searchRequest = new SearchRequest(Util.SEARCH_FUND_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().fetchSource("suggest", null);
		var suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("students-completion-suggest", SuggestBuilders.completionSuggestion("studentsName.completion").skipDuplicates(true).prefix(query.trim()));
		suggestBuilder.addSuggestion("unit-completion-suggest", SuggestBuilders.completionSuggestion("unit.completion").skipDuplicates(true).prefix(query.trim()));
		suggestBuilder.addSuggestion("title-completion-suggest", SuggestBuilders.completionSuggestion("title.completion").skipDuplicates(true).prefix(query.trim()));
		searchSourceBuilder.suggest(suggestBuilder);
		searchRequest.source(searchSourceBuilder);
		var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		responseJson.put("completionList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));
		return responseJson.toString();
	}

	@GetMapping("/completionGenesSuggester")
	public String completionGenesSuggester(@NotBlank String query) throws Exception {
		var responseJson = new JSONObject();
		var searchRequest = new SearchRequest(Util.SEARCH_GENES_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().fetchSource("suggest", null);
		var suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("name-completion-suggest", SuggestBuilders.completionSuggestion("name.completion").prefix(query.trim()));
		searchSourceBuilder.suggest(suggestBuilder);
		searchRequest.source(searchSourceBuilder);
		var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		responseJson.put("completionList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));
		return responseJson.toString();
	}
	
	@GetMapping("/completionGeoSuggester")
	public String completionGeoSuggester(@NotBlank String query) throws Exception {
		var responseJson = new JSONObject();
		var searchRequest = new SearchRequest(Util.SEARCH_GEO_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().fetchSource("suggest", null);
		var suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("title-completion-suggest", SuggestBuilders.completionSuggestion("title.completion").prefix(query.trim()));
		suggestBuilder.addSuggestion("accession-completion-suggest", SuggestBuilders.completionSuggestion("accession.completion").prefix(query.trim()));
		searchSourceBuilder.suggest(suggestBuilder);
		searchRequest.source(searchSourceBuilder);
		var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		responseJson.put("completionList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));
		return responseJson.toString();
	}
	
	@GetMapping("/completionGeoPlatformIDsSuggester")
	public String completionGeoPlatformIDsSuggester(@NotBlank String query) throws Exception {
		var responseJson = new JSONObject();
		var searchRequest = new SearchRequest(Util.SEARCH_GEO_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().fetchSource("suggest", null);
		var suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("platformIDs-completion-suggest", SuggestBuilders.completionSuggestion("platformIDs.completion")
				.size(10).skipDuplicates(true).prefix(query.trim()));
		searchSourceBuilder.suggest(suggestBuilder);
		searchRequest.source(searchSourceBuilder);
		var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		responseJson.put("completionList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));
		return responseJson.toString();
	}
	
	@GetMapping("/completionGeoOrganismSuggester")
	public String completionGeoOrganismSuggester(@NotBlank String query) throws Exception {
		var responseJson = new JSONObject();
		var searchRequest = new SearchRequest(Util.SEARCH_GEO_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().fetchSource("suggest", null);
		var suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("organism-completion-suggest", SuggestBuilders.completionSuggestion("organismAll.completion")
				.size(10).skipDuplicates(true).prefix(query.trim()));
		searchSourceBuilder.suggest(suggestBuilder);
		searchRequest.source(searchSourceBuilder);
		var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		responseJson.put("completionList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));
		return responseJson.toString();
	}
	
	@GetMapping("/completionGeoSampleTypesJsonSuggester")
	public String completionGeoSampleTypesJsonSuggester(@NotBlank String query) throws Exception {
		var responseJson = new JSONObject();
		var searchRequest = new SearchRequest(Util.SEARCH_GEO_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder().fetchSource("suggest", null);
		var suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("sampleTypesJson-completion-suggest", SuggestBuilders.completionSuggestion("sampleTypesJson.completion")
				.size(10).skipDuplicates(true).prefix(query.trim()));
		searchSourceBuilder.suggest(suggestBuilder);
		searchRequest.source(searchSourceBuilder);
		var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		responseJson.put("completionList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));	
		return responseJson.toString();
	}
	
}
