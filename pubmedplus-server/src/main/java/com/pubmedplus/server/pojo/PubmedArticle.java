package com.pubmedplus.server.pojo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder.Item;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.pubmedplus.server.utils.Util;

public class PubmedArticle extends PubmedObject {
	private int similarity = 30;
	private String yearsFilter = null;
	private String typeFilter = null;
	private String language = "en";
	private int size = 10;

	public int getSimilarity() {
		return similarity;
	}

	public void setSimilarity(int similarity) {
		if (similarity >= 0 && similarity <= 100) {
			this.similarity = similarity;
		}
	}

	public String getYearsFilter() {
		return yearsFilter;
	}

	public void setYearsFilter(String yearsFilter) {
		this.yearsFilter = yearsFilter;
	}

	public String getTypeFilter() {
		return typeFilter;
	}

	public void setTypeFilter(String typeFilter) {
		this.typeFilter = typeFilter;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		language = "zh".equals(language) ? "zh" : "en";
		this.language = language;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		if(size>0) {
			this.size = size;
		}
	}

	public String[] getIncludesArticle() {
		return new String[] { "PMID", "title*", "abstractText*", "authorList", "doi", "journal.*", "pubTime",
				"publicationTypeList", "volume", "pagination" };
	}

	public Item[] getSimilarityItem() {
		Item[] Items = null;
		if (Util.isNumeric(query)) {
			Items = new Item[] { new Item(Util.SEARCH_PUBMED_INDEX, query) };
		}
		return Items;
	}

	public String[] getSimilaritylikeTexts() {
		String[] likeTexts = null;
		if (!Util.isNumeric(query)) {
			likeTexts = new String[] { query };
		}
		return likeTexts;
	}

	public HighlightBuilder getHighlightArticle() {
		if (query.length() < 20 || queryStringQuery.length() < 20) {
			var highlightBuilder = new HighlightBuilder();
			highlightBuilder.preTags("<font color='red'>");
			highlightBuilder.postTags("</font>");
			highlightBuilder.numOfFragments(0);
			highlightBuilder.field("title*");
			highlightBuilder.field("abstractText*");
			return highlightBuilder;
		}
		return null;
	}

	public List<SortBuilder<?>> getSortArticleData() {
		var sortList = new ArrayList<SortBuilder<?>>();
		try {
			if (queryStringQuery.trim().length() > 0 || query.trim().length() > 0) {
				sortList.add(new ScoreSortBuilder().order(SortOrder.DESC));
			}
			sortList.add(new FieldSortBuilder("pubTime").order(SortOrder.DESC));
			sortList.add(new FieldSortBuilder("PMID").order(SortOrder.DESC));
			if (sort != null && !sort.isBlank() && sort.split("_").length > 1) {
				var sortName = sort.split("_")[0];
				var sortOrder = Integer.valueOf(sort.split("_")[1]);
				if ("published-sort".equals(sortName) && (sortOrder == -1 || sortOrder == 1)) {
					var fieldSortBuilder = sortOrder==-1?new FieldSortBuilder("pubTime"):new FieldSortBuilder("pubTime").order(SortOrder.DESC);
					sortList.clear();
					sortList.add(fieldSortBuilder);
				} else if ("factor-sort".equals(sortName) && (sortOrder == -1 || sortOrder == 1)) {
					var fieldSortBuilder = sortOrder==-1?new FieldSortBuilder("journal.nowIfs"):new FieldSortBuilder("journal.nowIfs").order(SortOrder.DESC);
					sortList.clear();
					sortList.add(fieldSortBuilder);
				} else if ("matching-sort".equals(sortName) && (sortOrder == -1 || sortOrder == 1)) {
					var fieldSortBuilder = sortOrder==-1?new FieldSortBuilder("_score").order(SortOrder.ASC):new FieldSortBuilder("_score").order(SortOrder.DESC);
					sortList.clear();
					sortList.add(fieldSortBuilder);
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sortList;
	}

	public BoolQueryBuilder getFilterArticleData(BoolQueryBuilder boolQueryBuilder) {
		try {
			if (ifsFilter != null && !ifsFilter.isBlank()) {
				var pattern = Pattern.compile("^[0-9]+(.[0-9]+)?$");
				var ifsFilters = ifsFilter.split(",");
				BoolQueryBuilder boolQuerySortBuilder = QueryBuilders.boolQuery();
				for (var ifsFilter : ifsFilters) {
					var ifsArray = ifsFilter.split("-");
					if (ifsArray.length < 2) {
						continue;
					}
					var ifsStrat = ifsArray[0].toString().trim();
					var ifsEnd = ifsArray[1].toString().trim();
					if (pattern.matcher(ifsStrat).matches() && pattern.matcher(ifsEnd).matches()) {
						boolQuerySortBuilder.should(QueryBuilders.rangeQuery("journal.nowIfs").gte(Float.valueOf(ifsStrat)).lte(Float.valueOf(ifsEnd)));
					}
				}
				if(boolQuerySortBuilder.should()!=null&&boolQuerySortBuilder.should().size()>0) {
					boolQuerySortBuilder.minimumShouldMatch(1);
				}
				boolQueryBuilder.must(boolQuerySortBuilder);
			}
			if (yearsFilter != null && !yearsFilter.isBlank()) {
				var pattern = Pattern.compile("^[-\\+]?[\\d]*$");
				var yearFilters = yearsFilter.split(",");
				BoolQueryBuilder boolQuerySortBuilder = QueryBuilders.boolQuery();
				for (var yearsFilter : yearFilters) {
					var f = new SimpleDateFormat("yyyy-MM-dd");
					if (yearsFilter.split("-").length > 1 && pattern.matcher(yearsFilter.split("-")[0]).matches() && pattern.matcher(yearsFilter.split("-")[1]).matches()) {
						boolQuerySortBuilder.should(QueryBuilders.rangeQuery("pubTime").gte(f.format(Util.getYearFirst(Integer.valueOf(yearsFilter.split("-")[0])))).lte(f.format(Util.getYearLast(Integer.valueOf(yearsFilter.split("-")[1])))));
					}else if(pattern.matcher(yearsFilter).matches()){
						boolQuerySortBuilder.should(QueryBuilders.rangeQuery("pubTime").gte(f.format(Util.getYearFirst(Integer.valueOf(yearsFilter)))).lte(f.format(Util.getYearLast(Integer.valueOf(yearsFilter)))));
					}
				}
				if (boolQuerySortBuilder.should() != null && boolQuerySortBuilder.should().size() > 0) {
					boolQuerySortBuilder.minimumShouldMatch(1);
				}
				boolQueryBuilder.must(boolQuerySortBuilder);
			}
			if (typeFilter != null && !typeFilter.isBlank()) {
				var typeFilters = typeFilter.split(",");
				if (Arrays.asList(typeFilters).contains("rests")) {
					var excludeTerm = new String[] { "Journal Article", "Review", "Practice Guideline", "Meta-Analysis", "Clinical Trial", "Multicenter Study", "Case Reports", "English Abstract", "Randomized Controlled Trial", "Comparative Study" };
					boolQueryBuilder.mustNot(QueryBuilders.termsQuery("publicationType", excludeTerm));
				} else if (typeFilters.length > 0) {
					boolQueryBuilder.filter(QueryBuilders.termsQuery("publicationType", typeFilters));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return boolQueryBuilder;
	}

	public SearchRequest getSearchRequest(BoolQueryBuilder boolQueryBuilder) {
		var searchRequest = new SearchRequest(Util.SEARCH_PUBMED_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.from((page - 1) * 10);
		var sortList = getSortArticleData();
		sortList.forEach((sort) -> {
			searchSourceBuilder.sort(sort);
		});
		searchSourceBuilder.highlighter(getHighlightArticle());
		searchSourceBuilder.fetchSource(getIncludesArticle(), null);
		if(queryStringQuery.length()==0) {
			boolQueryBuilder.filter(QueryBuilders.rangeQuery("pubTime").gte("now-10d/d").lte("now/d"));
		}else {
			boolQueryBuilder.filter(QueryBuilders.rangeQuery("pubTime").lte("now"));
		}
		boolQueryBuilder = getFilterArticleData(boolQueryBuilder);
		searchSourceBuilder.query(boolQueryBuilder);
		searchRequest.source(searchSourceBuilder);
		return searchRequest;
	}
	
	
}
