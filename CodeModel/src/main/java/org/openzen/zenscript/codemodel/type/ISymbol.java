/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ISymbol {
	public IPartialExpression getExpression(CodePosition position, GlobalTypeRegistry types, ITypeID[] typeArguments);
	
	public ITypeID getType(CodePosition position, GlobalTypeRegistry types, ITypeID[] typeArguments);
}
