package it.musichub.server.rest.model.utils;

import java.util.List;

public class PaginatedList<T> {

	public int total;
	public List<T> results;
	public PagingLinks _links;

	public PaginatedList(int total, List<T> results, PagingLinks _links) {
		super();
		this.total = total;
		this.results = results;
		this._links = _links;
	}
}
