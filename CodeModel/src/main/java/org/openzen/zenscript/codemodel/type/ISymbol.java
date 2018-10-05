/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.BaseScope;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ISymbol {
	public IPartialExpression getExpression(CodePosition position, BaseScope scope, TypeArgument[] typeArguments);
	
	public TypeID getType(CodePosition position, TypeResolutionContext context, TypeArgument[] typeArguments);
}
