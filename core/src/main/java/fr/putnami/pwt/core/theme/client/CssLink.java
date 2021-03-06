/**
 * This file is part of pwt.
 *
 * pwt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pwt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pwt.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.core.theme.client;

import com.google.common.base.Objects;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;

public class CssLink implements Comparable<CssLink> {

	private final int precedence;
	private final LinkElement link = Document.get().createLinkElement();

	public CssLink(String href, int precedence) {
		super();
		this.precedence = precedence;
		link.setRel("stylesheet");
		link.setHref(GWT.getModuleBaseForStaticFiles() + href);
	}

	public int getPrecedence() {
		return precedence;
	}

	public LinkElement getLink() {
		return link;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(link.getHref());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CssLink) {
			CssLink other = (CssLink) obj;
			return Objects.equal(link.getHref(), other.link.getHref());
		}
		return false;
	}

	@Override
	public int compareTo(CssLink o) {
		if (o == null) {
			return 1;
		}
		return Integer.valueOf(precedence).compareTo(o.precedence);

	}
}
