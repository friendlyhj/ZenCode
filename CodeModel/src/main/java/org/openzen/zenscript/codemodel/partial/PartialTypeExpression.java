/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.List;
import java.util.stream.Collectors;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class PartialTypeExpression implements IPartialExpression {
	private final CodePosition position;
	private final ITypeID type;
	private final ITypeID[] typeParameters;
	
	public PartialTypeExpression(CodePosition position, ITypeID type, ITypeID[] typeParameters) {
		this.position = position;
		this.type = type;
		this.typeParameters = typeParameters;
	}

	@Override
	public Expression eval() {
		throw new CompileException(position, CompileExceptionCode.USING_TYPE_AS_EXPRESSION, "Not a valid expression");
	}

	@Override
	public List<ITypeID>[] predictCallTypes(TypeScope scope, List<ITypeID> hints, int arguments) {
		return new List[arguments];
	}
	
	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<ITypeID> hints, int arguments) {
		return scope.getTypeMembers(type)
				.getOrCreateGroup(OperatorType.CALL)
				.getMethodMembers().stream()
				.filter(method -> method.member.header.parameters.length == arguments && method.member.isStatic())
				.map(method -> method.member.header)
				.collect(Collectors.toList());
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<ITypeID> hints, GenericName name) {
		return scope.getTypeMembers(type).getStaticMemberExpression(position, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<ITypeID> hints, CallArguments arguments) {
		return scope.getTypeMembers(type).getOrCreateGroup(OperatorType.CALL).callStatic(position, type, scope, arguments);
	}

	@Override
	public ITypeID[] getGenericCallTypes() {
		return typeParameters;
	}
	
	@Override
	public IPartialExpression capture(CodePosition position, LambdaClosure closure) {
		return this;
	}
}
