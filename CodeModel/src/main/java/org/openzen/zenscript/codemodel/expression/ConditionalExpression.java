/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConditionalExpression extends Expression {
	public final Expression condition;
	public final Expression ifThen;
	public final Expression ifElse;
	
	public ConditionalExpression(
			CodePosition position,
			Expression condition,
			Expression ifThen,
			Expression ifElse,
			ITypeID type) {
		super(position, ifThen.type, binaryThrow(position, condition.thrownType, binaryThrow(position, ifThen.thrownType, ifElse.thrownType)));
		
		if (ifThen.type != ifElse.type)
			throw new AssertionError();
		
		this.condition = condition;
		this.ifThen = ifThen;
		this.ifElse = ifElse;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConditional(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tCondition = transformer.transform(condition);
		Expression tIfThen = transformer.transform(ifThen);
		Expression tIfElse = transformer.transform(ifElse);
		return tCondition == condition && tIfThen == ifThen && tIfElse == ifElse
				? this
				: new ConditionalExpression(position, tCondition, tIfThen, tIfElse, type);
	}
}
