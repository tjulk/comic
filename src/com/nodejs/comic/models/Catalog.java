/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.nodejs.comic.models;

import java.io.Serializable;
import java.util.List;

public class Catalog implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private String id;
	private String locale;
	private List<Catalog> children;
	private String iconUrl;

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public String getId() {
	    return id;
	}

	public void setId(String id) {
	    this.id = id;
	}

	public String getLocale() {
	    return locale;
	}

	public void setLocale(String locale) {
	    this.locale = locale;
	}

	public List<Catalog> getChildren() {
	    return children;
	}

	public void setChildren(List<Catalog> children) {
	    this.children = children;
	}

	public String getIconUrl() {
	    return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
	    this.iconUrl = iconUrl;
	}
	
	public int getChildSize() {
		if (children == null) {
			return 0;
		} else {
			return children.size();
		}
	}

	@Override
	public String toString() {
		return "Catalog [children=" + children + ", icon=" + iconUrl + ", id=" + id + ", name=" + name + "]";
	}
	
}
