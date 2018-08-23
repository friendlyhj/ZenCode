/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import java.util.HashMap;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.expression.ParsedExpression;

/**
 *
 * @author Hoofdgebruiker
 */
public class PrefixedBracketParser implements BracketExpressionParser {
	private final Map<String, BracketExpressionParser> subParsers = new HashMap<>();
	private final BracketExpressionParser defaultParser;
	
	public PrefixedBracketParser(BracketExpressionParser defaultParser) {
		this.defaultParser = defaultParser;
	}
	
	public void register(String name, BracketExpressionParser parser) {
		subParsers.put(name, parser);
	}

	@Override
	public ParsedExpression parse(CodePosition position, ZSTokenParser tokens) {
		if (defaultParser == null) {
			ZSToken start = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected");
			tokens.required(ZSTokenType.T_COLON, ": expected");
			BracketExpressionParser subParser = subParsers.get(start.content);
			if (subParser == null)
				throw new CompileException(position, CompileExceptionCode.INVALID_BRACKET_EXPRESSION, "Invalid bracket expression");
			
			return subParser.parse(position, tokens);
		} else {
			tokens.pushMark();
			ZSToken start = tokens.next();
			if (start.type == ZSTokenType.T_IDENTIFIER && subParsers.containsKey(start.content)) {
				tokens.popMark();
				tokens.required(ZSTokenType.T_COLON, ": expected");
				return subParsers.get(start.content).parse(position, tokens);
			} else {
				tokens.reset();
				return defaultParser.parse(position, tokens);
			}
		}
	}
}