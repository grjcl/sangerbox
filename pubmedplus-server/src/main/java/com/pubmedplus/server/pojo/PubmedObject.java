package com.pubmedplus.server.pojo;

import org.apache.lucene.queryparser.classic.QueryParser;

public class PubmedObject {
	public String queryStringQuery = "";
	public String query = "";
	public int page = 1;
	public String sort = null;
	public String ifsFilter = null;
	public int isSci = 0;
	public String nlmId = null;

	public String getQueryStringQuery() {
		return queryStringQuery;
	}

	public void setQueryStringQuery(String queryStringQuery) {
		queryStringQuery = queryStringQuery != null ? queryStringQuery.trim() : "";
		if (queryStringQuery.length() == 0) {
			return;
		}
		queryStringQuery=queryStringQuery.length()-queryStringQuery.replaceAll("\"", "").length()>=2?queryStringQuery:queryStringQuery.replaceAll("\"", "");
		if ("\"".equals(queryStringQuery.substring(0, 1)) && "\"".equals(queryStringQuery.substring(queryStringQuery.length() - 1, queryStringQuery.length()))) {
			this.queryStringQuery = queryStringQuery;
			return;
		}
		queryStringQuery = queryStringQuery.replaceAll(" and ", " AND ").replaceAll(" or ", " OR ").replaceAll(" not "," NOT ");
		this.queryStringQuery = queryStringQuery;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		query = query != null ? QueryParser.escape(query.trim()) : query;
		this.query = query;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		if (page > 0) {
			this.page = page;
		}
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getIfsFilter() {
		return ifsFilter;
	}

	public void setIfsFilter(String ifsFilter) {
		this.ifsFilter = ifsFilter;
	}

	public int getIsSci() {
		return isSci;
	}

	public void setIsSci(int isSci) {
		this.isSci = isSci;
	}

	public String getNlmId() {
		return nlmId;
	}

	public void setNlmId(String nlmId) {
		this.nlmId = nlmId;
	}

}
