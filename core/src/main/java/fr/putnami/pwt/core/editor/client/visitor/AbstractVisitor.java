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
package fr.putnami.pwt.core.editor.client.visitor;

import fr.putnami.pwt.core.editor.client.Context;
import fr.putnami.pwt.core.editor.client.Editor;
import fr.putnami.pwt.core.editor.client.Visitor;

public abstract class AbstractVisitor implements Visitor {

	@Override
	public <A, B extends Editor> boolean beforeVisit() {
		return true;
	}

	@Override
	public <A, B extends Editor> boolean visit(Context<B> context) {
		return true;
	}

	@Override
	public <A, B extends Editor> boolean afterVisit() {
		return true;
	}

	@Override
	public int getPrecedence() {
		return PRECEDENCE_NORMAL;
	}

}
