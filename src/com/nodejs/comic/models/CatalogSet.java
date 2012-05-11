package com.nodejs.comic.models;

import java.util.List;

public class CatalogSet {
	private List<Catalog> cataloglist;

    public List<Catalog> getCataloglist() {
	return cataloglist;
    }

    public void setCataloglist(List<Catalog> cataloglist) {
	this.cataloglist = cataloglist;
    }
}
