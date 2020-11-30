package com.pubmedplus.server.pojo;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.pubmedplus.server.utils.Util;

public class PubmedGenes extends PubmedObject{
	
	public SearchRequest getSearchRequest(BoolQueryBuilder boolQueryBuilder) {
		var searchRequest = new SearchRequest(Util.SEARCH_GENES_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.from((page - 1) * 10);
		searchSourceBuilder.fetchSource(new String[] {"geneID","name","desc","geneType"}, null);
		searchSourceBuilder.query(boolQueryBuilder);
		searchRequest.source(searchSourceBuilder);
		return searchRequest;
	}
	
}
