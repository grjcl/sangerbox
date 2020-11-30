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

public class PubmedFund extends PubmedObject {
	private int size = 10;
	public String departmentFilter = null;
	public String subjectOneFilter = null;
	public String subjectTwoFilter = null;
	public String subjectThreeFilter = null;
	public String moneyFilter = null;
	public String approvalYearFilter = null;
	public String typeFilter = null;

	public String getDepartmentFilter() {
		return departmentFilter;
	}

	public void setDepartmentFilter(String departmentFilter) {
		this.departmentFilter = departmentFilter;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getSubjectOneFilter() {
		return subjectOneFilter;
	}

	public void setSubjectOneFilter(String subjectOneFilter) {
		this.subjectOneFilter = subjectOneFilter;
	}

	public String getSubjectTwoFilter() {
		return subjectTwoFilter;
	}

	public void setSubjectTwoFilter(String subjectTwoFilter) {
		this.subjectTwoFilter = subjectTwoFilter;
	}

	public String getSubjectThreeFilter() {
		return subjectThreeFilter;
	}

	public void setSubjectThreeFilter(String subjectThreeFilter) {
		this.subjectThreeFilter = subjectThreeFilter;
	}

	public String getMoneyFilter() {
		return moneyFilter;
	}

	public void setMoneyFilter(String moneyFilter) {
		this.moneyFilter = moneyFilter;
	}

	public String getApprovalYearFilter() {
		return approvalYearFilter;
	}

	public void setApprovalYearFilter(String approvalYearFilter) {
		this.approvalYearFilter = approvalYearFilter;
	}

	public String getTypeFilter() {
		return typeFilter;
	}

	public void setTypeFilter(String typeFilter) {
		this.typeFilter = typeFilter;
	}

	/**
	 * 返回文章的部分数据
	 * 
	 * @return
	 */
	public String[] getIncludesFund() {
		return new String[] { "title", "subjectOneCode", "subjectOneType", "subjectTwoCode", "subjectTwoType",
				"subjectThreeCode", "subjectThreeType", "studentsName", "info.researchTimeScope", "unit", "money",
				"approvalNumber", "type", "approvalYear", "keyword" };
	}

	/**
	 * 
	 * 生成基金高亮
	 * 
	 * @return
	 */
	public HighlightBuilder getHighlightFund() {
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

	/**
	 * 排序基金
	 * 
	 * @param searchSourceBuilder
	 * @param sort
	 * @return
	 */
	public List<SortBuilder<?>> getSortFundData() {
		var sortList = new ArrayList<SortBuilder<?>>();
		try {
			if (queryStringQuery.trim().length() > 0 || query.trim().length() > 0) {
				sortList.add(new ScoreSortBuilder().order(SortOrder.DESC));
			}
			sortList.add(new FieldSortBuilder("approvalYear").order(SortOrder.DESC));
			if (sort != null && !sort.isBlank() && sort.split("_").length > 1) {
				var sortName = sort.split("_")[0];
				var sortorder = Integer.valueOf(sort.split("_")[1]);
				if ("money-sort".equals(sortName) && (sortorder == -1 || sortorder == 1)) {
					var fieldSortBuilder = new FieldSortBuilder("money");
					if (sortorder == 1) {
						fieldSortBuilder.order(SortOrder.DESC);
					}
					sortList.clear();
					sortList.add(fieldSortBuilder);
				} else if ("matching-sort".equals(sortName) && (sortorder == -1 || sortorder == 1)) {
					var fieldSortBuilder = new FieldSortBuilder("_score").order(SortOrder.ASC);
					if (sortorder == 1) {
						fieldSortBuilder.order(SortOrder.DESC);
					}
					sortList.clear();
					sortList.add(fieldSortBuilder);
				} else if ("approvalYear-sort".equals(sortName) && (sortorder == -1 || sortorder == 1)) {
					var fieldSortBuilder = new FieldSortBuilder("approvalYear");
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
	 * 筛选文章数据
	 * 
	 * @param boolQueryBuilder
	 * @return
	 */
	public BoolQueryBuilder getFilterFundData(BoolQueryBuilder boolQueryBuilder) {
		try {
			if (subjectOneFilter != null && !subjectOneFilter.isBlank()) {
				boolQueryBuilder.must(QueryBuilders.termQuery("subjectOne", subjectOneFilter));
			}
			if (subjectTwoFilter != null && !subjectTwoFilter.isBlank()) {
				boolQueryBuilder.must(QueryBuilders.termQuery("subjectTwo", subjectTwoFilter));
			}
			if (subjectThreeFilter != null && !subjectThreeFilter.isBlank()) {
				boolQueryBuilder.must(QueryBuilders.termQuery("subjectThree", subjectThreeFilter));
			}
			if (departmentFilter != null && !departmentFilter.isBlank()) {
				var departmentFilters = departmentFilter.split(",");
				BoolQueryBuilder boolQuerySortBuilder = QueryBuilders.boolQuery();
				for (var departmentFilter : departmentFilters) {
					if (departmentFilter.trim().length() > 0) {
						boolQuerySortBuilder.should(QueryBuilders.termQuery("department", departmentFilter));
					}
				}
				if (boolQuerySortBuilder.should() != null && boolQuerySortBuilder.should().size() > 0) {
					boolQuerySortBuilder.minimumShouldMatch(1);
				}
				boolQueryBuilder.must(boolQuerySortBuilder);
			}
			if (moneyFilter != null && !moneyFilter.isBlank()) {
				var pattern = Pattern.compile("^[0-9]+(.[0-9]+)?$");
				var moneyFilters = moneyFilter.split(",");
				BoolQueryBuilder boolQuerySortBuilder = QueryBuilders.boolQuery();
				for (var moneyFilter : moneyFilters) {
					var moneyArray = moneyFilter.split("-");
					if (moneyArray.length < 2) {
						continue;
					}
					var moneyStrat = moneyArray[0].toString().trim();
					var moneyEnd = moneyArray[1].toString().trim();
					if (pattern.matcher(moneyStrat).matches() && pattern.matcher(moneyEnd).matches()) {
						boolQuerySortBuilder.should(QueryBuilders.rangeQuery("money").gte(Float.valueOf(moneyStrat)).lte(Float.valueOf(moneyEnd)));
					}
				}
				if (boolQuerySortBuilder.should() != null && boolQuerySortBuilder.should().size() > 0) {
					boolQuerySortBuilder.minimumShouldMatch(1);
				}
				boolQueryBuilder.must(boolQuerySortBuilder);
			}
			if (approvalYearFilter != null && !approvalYearFilter.isBlank()) {
				var approvalYearFilters = approvalYearFilter.split(",");
				BoolQueryBuilder boolQuerySortBuilder = QueryBuilders.boolQuery();
				for (var approvalYearFilter : approvalYearFilters) {
					var approvalYearArray = approvalYearFilter.split("-");
					if (approvalYearArray.length < 2) {
						continue;
					}
					var approvalYearStrat = approvalYearArray[0].toString().trim();
					var approvalYearEnd = approvalYearArray[1].toString().trim();
					if (Util.isNumeric(approvalYearStrat) && Util.isNumeric(approvalYearEnd)) {
						boolQuerySortBuilder.should(QueryBuilders.rangeQuery("approvalYear").gte(approvalYearStrat).lte(approvalYearEnd));
					}
				}
				if (boolQuerySortBuilder.should() != null && boolQuerySortBuilder.should().size() > 0) {
					boolQuerySortBuilder.minimumShouldMatch(1);
				}
				boolQueryBuilder.must(boolQuerySortBuilder);
			}
			if (typeFilter != null && !typeFilter.isBlank()) {
				var typeFilters = typeFilter.split(",");
				BoolQueryBuilder boolQuerySortBuilder = QueryBuilders.boolQuery();
				for (var typeFilter : typeFilters) {
					if (typeFilter.trim().length() > 0) {
						boolQuerySortBuilder.should(QueryBuilders.termQuery("type", typeFilter));
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

	/**
	 * 生成搜索请求SearchRequest
	 * 
	 * @param pubmedFund
	 * @param boolQueryBuilder
	 * @return
	 */
	public SearchRequest getSearchRequest(BoolQueryBuilder boolQueryBuilder) {
		var searchRequest = new SearchRequest(Util.SEARCH_FUND_INDEX);
		var searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.from((page - 1) * 10);
		var sortList = getSortFundData();
		sortList.forEach((sort) -> {
			searchSourceBuilder.sort(sort);
		});
		searchSourceBuilder.highlighter(getHighlightFund());
		searchSourceBuilder.fetchSource(getIncludesFund(), null);
		boolQueryBuilder = getFilterFundData(boolQueryBuilder);
		searchSourceBuilder.query(boolQueryBuilder);
		searchRequest.source(searchSourceBuilder);
		return searchRequest;
	}

}
