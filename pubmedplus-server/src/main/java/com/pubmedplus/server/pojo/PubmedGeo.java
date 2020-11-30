package com.pubmedplus.server.pojo;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.pubmedplus.server.utils.Util;

public class PubmedGeo extends PubmedObject {
	private int sampleCount;
	private String sampleCountFilter;
	private String platformIDsFilter;
	private String organismAllFilter;
	private String sampleTypesJsonFilter;
	private String platformTypesFilter;

	public String getPlatformTypesFilter() {
		return platformTypesFilter;
	}

	public void setPlatformTypesFilter(String platformTypesFilter) {
		this.platformTypesFilter = platformTypesFilter;
	}

	public int getSampleCount() {
		return sampleCount;
	}

	public void setSampleCount(int sampleCount) {
		this.sampleCount = sampleCount;
	}

	public String getSampleCountFilter() {
		return sampleCountFilter;
	}

	public void setSampleCountFilter(String sampleCountFilter) {
		this.sampleCountFilter = sampleCountFilter;
	}

	public String getPlatformIDsFilter() {
		return platformIDsFilter;
	}

	public void setPlatformIDsFilter(String platformIDsFilter) {
		this.platformIDsFilter = platformIDsFilter;
	}

	public String getOrganismAllFilter() {
		return organismAllFilter;
	}

	public void setOrganismAllFilter(String organismAllFilter) {
		this.organismAllFilter = organismAllFilter;
	}

	public String getSampleTypesJsonFilter() {
		return sampleTypesJsonFilter;
	}

	public void setSampleTypesJsonFilter(String sampleTypesJsonFilter) {
		this.sampleTypesJsonFilter = sampleTypesJsonFilter;
	}

	public HighlightBuilder getHighlightGeo() {
		if (query.length() < 20 || queryStringQuery.length() < 20) {
			var highlightBuilder = new HighlightBuilder();
			highlightBuilder.preTags("<font color='red'>");
			highlightBuilder.postTags("</font>");
			highlightBuilder.numOfFragments(0);
			highlightBuilder.field("title");
			return highlightBuilder;
		}
		return null;
	}

	public List<SortBuilder<?>> getSortGeoData() {
		var sortList = new ArrayList<SortBuilder<?>>();
		try {
			if (queryStringQuery.trim().length() > 0 || query.trim().length() > 0) {
				sortList.add(new ScoreSortBuilder().order(SortOrder.DESC));
			}
			sortList.add(new FieldSortBuilder("lastUpdateDate").order(SortOrder.DESC));
			if (sort != null && !sort.isBlank() && sort.split("_").length > 1) {
				var sortName = sort.split("_")[0];
				var sortOrder = Integer.valueOf(sort.split("_")[1]);
				if ("updateDate-sort".equals(sortName) && (sortOrder == -1 || sortOrder == 1)) {
					var fieldSortBuilder = sortOrder == -1 ? new FieldSortBuilder("lastUpdateDate")
							: new FieldSortBuilder("lastUpdateDate").order(SortOrder.DESC);
					sortList.clear();
					sortList.add(fieldSortBuilder);
				} else if ("sampleCount-sort".equals(sortName) && (sortOrder == -1 || sortOrder == 1)) {
					var fieldSortBuilder = sortOrder == -1 ? new FieldSortBuilder("sampleCount")
							: new FieldSortBuilder("sampleCount").order(SortOrder.DESC);
					sortList.clear();
					sortList.add(fieldSortBuilder);
				} else if ("matching-sort".equals(sortName) && (sortOrder == -1 || sortOrder == 1)) {
					var fieldSortBuilder = sortOrder == -1 ? new FieldSortBuilder("_score").order(SortOrder.ASC)
							: new FieldSortBuilder("_score").order(SortOrder.DESC);
					sortList.clear();
					sortList.add(fieldSortBuilder);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sortList;
	}

	public BoolQueryBuilder getFilterGeoData(BoolQueryBuilder boolQueryBuilder,int size) {
		try {
			if (sampleCountFilter != null && !sampleCountFilter.isBlank()) {
				var sampleCountFilters = sampleCountFilter.split(",");
				BoolQueryBuilder boolQuerySortBuilder = QueryBuilders.boolQuery();
				for (var sampleCountFilter : sampleCountFilters) {
					var sampleCountArray = sampleCountFilter.split("-");
					if (sampleCountArray.length < 2) {
						continue;
					}
					var sampleCountStrat = sampleCountArray[0].toString().trim();
					var sampleCountEnd = sampleCountArray[1].toString().trim();
					if (Util.isNumeric(sampleCountStrat) && Util.isNumeric(sampleCountEnd)) {
						boolQuerySortBuilder.should(QueryBuilders.rangeQuery("sampleCount")
								.gte(Integer.valueOf(sampleCountStrat)).lte(Integer.valueOf(sampleCountEnd)));
					}
				}
				if (boolQuerySortBuilder.should() != null && boolQuerySortBuilder.should().size() > 0) {
					boolQuerySortBuilder.minimumShouldMatch(1);
				}
				boolQueryBuilder.must(boolQuerySortBuilder);
			}
			if (platformIDsFilter != null && !platformIDsFilter.isBlank()) {
				var platformIDsFilters = platformIDsFilter.split(",");
				BoolQueryBuilder boolQuerySortBuilder = QueryBuilders.boolQuery();
				for (var platformIDs : platformIDsFilters) {
					if (platformIDs.trim().length() > 0) {
						boolQuerySortBuilder.should(QueryBuilders.termQuery("platformIDs", platformIDs));
					}
				}
				if (boolQuerySortBuilder.should() != null && boolQuerySortBuilder.should().size() > 0) {
					boolQuerySortBuilder.minimumShouldMatch(1);
				}
				boolQueryBuilder.must(boolQuerySortBuilder);
			}
			if (organismAllFilter != null && !organismAllFilter.isBlank()) {
				var organismAllFilters = organismAllFilter.split(",");
				BoolQueryBuilder boolQuerySortBuilder = QueryBuilders.boolQuery();
				for (var organismAll : organismAllFilters) {
					if (organismAll.trim().length() > 0) {
						boolQuerySortBuilder.should(QueryBuilders.termQuery("organismAll", organismAll));
					}
				}
				if (boolQuerySortBuilder.should() != null && boolQuerySortBuilder.should().size() > 0) {
					boolQuerySortBuilder.minimumShouldMatch(1);
				}
				boolQueryBuilder.must(boolQuerySortBuilder);
			}
			if (sampleTypesJsonFilter != null && !sampleTypesJsonFilter.isBlank()) {
				var sampleTypesJsonFilters = sampleTypesJsonFilter.split(",");
				BoolQueryBuilder boolQuerySortBuilder = QueryBuilders.boolQuery();
				for (var sampleTypesJson : sampleTypesJsonFilters) {
					if (sampleTypesJson.trim().length() > 0) {
						if(size==10) {
							boolQuerySortBuilder.should(QueryBuilders.regexpQuery("sampleTypesJson", ".*?" + QueryParser.escape(sampleTypesJson) + ".*?"));
						}else {
							boolQuerySortBuilder.mustNot(QueryBuilders.regexpQuery("sampleTypesJson", ".*?" + QueryParser.escape(sampleTypesJson) + ".*?"));
						}
					}
				}
				if (boolQuerySortBuilder.should() != null && boolQuerySortBuilder.should().size() > 0) {
					boolQuerySortBuilder.minimumShouldMatch(1);
				}
				boolQueryBuilder.must(boolQuerySortBuilder);
			}
			if (platformTypesFilter != null && !platformTypesFilter.isBlank()) {
				var platformTypesFilters = platformTypesFilter.split(",");	
				BoolQueryBuilder boolQuerySortBuilder = QueryBuilders.boolQuery();
				for (var platformTypes : platformTypesFilters) {
					if (platformTypes.trim().length() > 0) {
						boolQuerySortBuilder.should(QueryBuilders.termQuery("platformTypes", platformTypes));
					}
				}
				if (boolQuerySortBuilder.should() != null && boolQuerySortBuilder.should().size() > 0) {
					boolQuerySortBuilder.minimumShouldMatch(1);
				}
				boolQueryBuilder.must(boolQuerySortBuilder);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return boolQueryBuilder;
	}

	public SearchRequest getSearchRequest(BoolQueryBuilder boolQueryBuilder,int size) {
		var searchRequest = new SearchRequest(Util.SEARCH_GEO_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder();
		if (size == 10) {
			searchSourceBuilder.from((page - 1) * 10);
		}else {
			searchRequest.scroll("1m");
			searchSourceBuilder.size(size);
		}
		var sortList = getSortGeoData();
		sortList.forEach((sort) -> {
			searchSourceBuilder.sort(sort);
		});
		searchSourceBuilder.highlighter(getHighlightGeo());
		var include = size==10?new String[] { "accession", "platformIDs", "sampleCount", "title", "pubmedIDs", "platformTypes",
				"sampleTypeIDs", "sampleTypesJson" }:new String[] {"sampleTypesJson" };
		searchSourceBuilder.fetchSource(include, null);
		boolQueryBuilder = getFilterGeoData(boolQueryBuilder,size);
		searchSourceBuilder.query(boolQueryBuilder);
		searchRequest.source(searchSourceBuilder);
		return searchRequest;
	}

}
