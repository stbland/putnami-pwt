/**
 * This file is part of pwt-code-editor.
 *
 * pwt-code-editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pwt-code-editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pwt-code-editor.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.plugin.code.client.assist;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import fr.putnami.pwt.core.widget.client.assist.AbstractContentAssistHandler;
import fr.putnami.pwt.core.widget.client.assist.ContentAssistAspect;
import fr.putnami.pwt.core.widget.client.assist.ContentAssistHandler;
import fr.putnami.pwt.plugin.code.client.aspect.CodeEditorAspect;
import fr.putnami.pwt.plugin.code.client.base.CodeEditorDriver;
import fr.putnami.pwt.plugin.code.client.event.LiveValueChangeEvent;
import fr.putnami.pwt.plugin.code.client.input.CodeInput;

public class CodeContentAssistAspect extends ContentAssistAspect implements CodeEditorAspect {

	public CodeContentAssistAspect() {
		this(new CodeDefaultContentAssistHandler());
	}

	public CodeContentAssistAspect(ContentAssistHandler assistHandler) {
		super(assistHandler);
	}

	@Override
	protected void setNewSelection(Suggestion curSuggestion) {
		CodeInput codeInput = (CodeInput) getInput();
		String oldText = codeInput.getText();
		super.setNewSelection(curSuggestion);
		String newText = codeInput.getText();
		LiveValueChangeEvent.fireIfNotEqual(codeInput, oldText, newText);
	}

	@Override
	public void apply(CodeEditorDriver driver) {
		setInput(driver.getCodeInput());
	}

	@Override
	public List<AspectTrigger> trigerOn() {
		return Arrays.asList(AspectTrigger.INITALIZE);
	}

	@Override
	public CodeEditorAspect copy() {
		return new CodeContentAssistAspect(assistHandler.copy());
	}

	static class CodeDefaultContentAssistHandler extends AbstractContentAssistHandler {

		public CodeDefaultContentAssistHandler() {
			super(new MultiWordSuggestOracle());
		}

		@Override
		public String getQueryText(IsWidget textInput) {
			CodeInput codeInput = (CodeInput) textInput;
			return codeInput.getText().substring(0, codeInput.getCursorPosition());

		}

		@Override
		public void handleSuggestionSelected(IsWidget textInput, Suggestion suggestion) {
			CodeInput codeInput = (CodeInput) textInput;
			String oldText = codeInput.getText();
			String newText = suggestion.getReplacementString()
					+ oldText.substring(codeInput.getCursorPosition(), oldText.length());
			codeInput.setText(newText);
			codeInput.setCursorPosition(suggestion.getReplacementString().length());
		}

		@Override
		public ContentAssistHandler copy() {
			return new CodeDefaultContentAssistHandler();
		}
	}
}
