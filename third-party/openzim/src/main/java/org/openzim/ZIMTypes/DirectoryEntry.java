/*
 * Copyright (C) 2011 Arunesh Mathur
 * 
 * This file is a part of zimreader-java.
 *
 * zimreader-java is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3.0 as 
 * published by the Free Software Foundation.
 *
 * zimreader-java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with zimreader-java.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openzim.ZIMTypes;

public abstract class DirectoryEntry {

	final int mimeType;

	final char namespace;

	final int revision;

	final String url;

	final String title;

	final int urlListindex;

	public DirectoryEntry(int mimeType, char namespace, int revision,
			String url, String title, int index) {
		this.mimeType = mimeType;
		this.namespace = namespace;
		this.revision = revision;
		this.url = url;
		this.title = title;
		this.urlListindex = index;
	}

	public int getMimeType() {
		return mimeType;
	}

	public char getNamespace() {
		return namespace;
	}

	public int getRevision() {
		return revision;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}

	public int getUrlListindex() {
		return urlListindex;
	}

}
