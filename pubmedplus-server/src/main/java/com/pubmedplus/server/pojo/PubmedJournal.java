package com.pubmedplus.server.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

public class PubmedJournal extends PubmedObject {
	private String areaFilter = null;
	private String qClassFilter = null;

	public String getAreaFilter() {
		return areaFilter;
	}

	public void setAreaFilter(String areaFilter) {
		this.areaFilter = areaFilter;
	}

	public String getqClassFilter() {
		return qClassFilter;
	}

	public void setqClassFilter(String qClassFilter) {
		this.qClassFilter = qClassFilter;
	}

	/**
	 * 生成杂志高亮
	 * 
	 * @return
	 */
	public HighlightBuilder getHighlightJournal() {
		if (query.length() < 200) {
			var highlightBuilder = new HighlightBuilder();
			highlightBuilder.preTags("<font color='red'>");
			highlightBuilder.postTags("</font>");
			highlightBuilder.numOfFragments(0);
			highlightBuilder.requireFieldMatch(false);
			highlightBuilder.field("fullName");
			highlightBuilder.field("subName");
			return highlightBuilder;
		}
		return null;
	}

	/**
	 * 排序杂志
	 * 
	 * @param searchSourceBuilder
	 * @return
	 */
	public List<SortBuilder<?>> getSortJournalData() {
		var sortList = new ArrayList<SortBuilder<?>>();
		try {
			if (queryStringQuery.trim().length() > 0 || query.trim().length() > 0) {
				sortList.add(new ScoreSortBuilder().order(SortOrder.DESC));
			}
			sortList.add(new FieldSortBuilder("nowIfs").order(SortOrder.DESC));;
			if (sort != null && !sort.isBlank() && sort.split("_").length > 1) {
				var sortName = sort.split("_")[0];
				var sortorder = Integer.valueOf(sort.toString().split("_")[1]);
				if ("matching-sort".equals(sortName) && (sortorder == -1 || sortorder == 1)) {
					var scoreSortBuilder = new ScoreSortBuilder().order(SortOrder.ASC);
					if (sortorder == 1) {
						scoreSortBuilder.order(SortOrder.DESC);
					}
					sortList.clear();
					sortList.add(scoreSortBuilder);
				} else if ("factor-sort".equals(sortName) && (sortorder == -1 || sortorder == 1)) {
					var fieldSortBuilder = new FieldSortBuilder("nowIfs");
					if (sortorder == 1) {
						fieldSortBuilder.order(SortOrder.DESC);
					}
					sortList.clear();
					sortList.add(fieldSortBuilder);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sortList;
	}

	/**
	 * 筛选杂志数据
	 * 
	 * @param boolQueryBuilder
	 * @return
	 */
	public BoolQueryBuilder getFilterJournalData(BoolQueryBuilder boolQueryBuilder) {
		try {
			if (ifsFilter != null && !ifsFilter.isBlank()) {
				var pattern = Pattern.compile("^[0-9]+(.[0-9]+)?$");
				var ifsFilters = ifsFilter.split(",");
				BoolQueryBuilder boolQuerySortBuilder = QueryBuilders.boolQuery();
				for (var ifsFilter : ifsFilters) {
					var ifsArray = ifsFilter.split("-");
					if (ifsArray.length > 1) {
						var ifsName = ifsArray[0].toString().trim();
						var ifsType = ifsArray[1].toString().trim();
						if (pattern.matcher(ifsName).matches() && pattern.matcher(ifsType).matches()) {
							boolQuerySortBuilder.should(QueryBuilders.rangeQuery("nowIfs").gte(ifsName).lt(ifsType));
						}
					}
				}
				boolQueryBuilder.must(boolQuerySortBuilder);
			}
			if (areaFilter != null && !areaFilter.isBlank()) {
				boolQueryBuilder.filter(QueryBuilders.termsQuery("area", areaFilter.split(",")));
			}
			if (qClassFilter != null && !qClassFilter.isBlank()) {
				boolQueryBuilder.filter(QueryBuilders.termsQuery("qClass", qClassFilter.split(",")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return boolQueryBuilder;
	}
	
	
	/**
	 * 生成搜索请求SearchRequest
	 * 
	 * @param pubmedFund
	 * @param boolQueryBuilder
	 * @return
	 */
	public SearchRequest getSearchRequest(BoolQueryBuilder boolQueryBuilder) {
		var searchRequest = new SearchRequest(Util.SEARCH_JOURNAL_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.from((page - 1) * 10);
		var sortList = getSortJournalData();
		sortList.forEach((sort) -> {
			searchSourceBuilder.sort(sort);
		});
		searchSourceBuilder.highlighter(getHighlightJournal());
		searchSourceBuilder.fetchSource(null, new String[] { "@timestamp", "accept", "direction", "jcrId", "jrId", "qClass",
				"selfCitationRate", "startYear", "comments", "jourcacheScience"});
		if (isSci == 1) {
			boolQueryBuilder.filter(QueryBuilders.existsQuery("nowIfs"));
		}
		boolQueryBuilder = getFilterJournalData(boolQueryBuilder);
		searchSourceBuilder.query(boolQueryBuilder);
		//searchSourceBuilder.trackTotalHits(true);
		searchRequest.source(searchSourceBuilder);
		return searchRequest;
	}
	
	
	
}




