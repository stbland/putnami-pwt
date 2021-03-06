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
package fr.putnami.pwt.plugin.code.client.render;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fr.putnami.pwt.plugin.code.client.aspect.CodeEditorAspect;
import fr.putnami.pwt.plugin.code.client.token.Token;
import fr.putnami.pwt.plugin.code.client.token.TokenContent;
import fr.putnami.pwt.plugin.code.client.token.TokenEvaluator;
import fr.putnami.pwt.plugin.code.client.token.TokenScanner;

public class PartitionnedTextRendererAspect extends AbstractTextRendererAspect {

	private boolean addPartitionWithoutScanner;

	private TokenScanner tokenPartitionner = buildScanner();
	private Map<TokenContent, TokenScanner> partitionScanners = Maps.newHashMap();

	public PartitionnedTextRendererAspect() {
		super();
	}

	public PartitionnedTextRendererAspect(boolean addPartitionWithoutScanner) {
		super();
		this.addPartitionWithoutScanner = addPartitionWithoutScanner;
	}

	public PartitionnedTextRendererAspect(boolean addPartitionWithoutScanner, boolean autoAddEOLToken) {
		super(autoAddEOLToken);
		this.addPartitionWithoutScanner = addPartitionWithoutScanner;
	}

	public PartitionnedTextRendererAspect(List<TokenEvaluator> partitionEvaluators) {
		this.tokenPartitionner.registerAllEvaluator(partitionEvaluators);
	}

	@Override
	protected List<Token<?>> extractTokenList(String value) {
		List<Token<?>> resultTokenList = Lists.newArrayList();

		// Partition of the String
		List<Token<?>> partitionnedValue = getTokens(value, tokenPartitionner);

		// Tokenization of each partition
		for (Token<?> curToken : partitionnedValue) {
			if (curToken.isEOF()) {
				resultTokenList.add(curToken);
				break;
			}
			TokenScanner curScanner = partitionScanners.get(curToken.getContent());
			if (curScanner != null) {
				resultTokenList.addAll(getTokens(curToken.getText(), curScanner));
			}
			else if (addPartitionWithoutScanner) {
				resultTokenList.add(curToken);
			}
		}
		return resultTokenList;
	}

	private List<Token<?>> getTokens(String value, TokenScanner scanner) {
		List<Token<?>> resultTokens = Lists.newArrayList();
		scanner.setValueToScan(value);
		Token<?> token = scanner.nextToken();
		while (!token.isEOF()) {
			resultTokens.add(token);
			token = scanner.nextToken();
		}
		return resultTokens;
	}

	public void registerPartitionner(TokenEvaluator partitionner) {
		tokenPartitionner.registerEvaluator(partitionner);
	}

	public void registerPartitionScanner(TokenContent key, TokenEvaluator evaluator) {
		TokenScanner curScanner = getOrRegisterScanner(key);
		curScanner.registerEvaluator(evaluator);
	}

	public void registerPartitionScanners(TokenContent key, TokenEvaluator... evaluators) {
		registerPartitionScanners(key, Lists.newArrayList(evaluators));
	}

	public void registerPartitionScanners(TokenContent key, List<TokenEvaluator> evaluators) {
		TokenScanner curScanner = getOrRegisterScanner(key);
		curScanner.registerAllEvaluator(evaluators);
	}

	private TokenScanner getOrRegisterScanner(TokenContent key) {
		TokenScanner curScanner = partitionScanners.get(key);
		if (curScanner == null) {
			curScanner = buildScanner();
			partitionScanners.put(key, curScanner);
		}
		return curScanner;
	}

	public void setAddPartitionWithoutScanner(boolean addPartitionWithoutScanner) {
		this.addPartitionWithoutScanner = addPartitionWithoutScanner;
	}

	@Override
	public CodeEditorAspect copy() {
		PartitionnedTextRendererAspect copy = new PartitionnedTextRendererAspect(tokenPartitionner.getEvaluators());
		for (Entry<TokenContent, TokenScanner> entry : partitionScanners.entrySet()) {
			copy.registerPartitionScanners(entry.getKey(), entry.getValue().getEvaluators());
		}
		copy.setAddPartitionWithoutScanner(addPartitionWithoutScanner);
		copy.setAutoAddEOLToken(getAutoAddEOLToken());
		return copy;
	}

}
