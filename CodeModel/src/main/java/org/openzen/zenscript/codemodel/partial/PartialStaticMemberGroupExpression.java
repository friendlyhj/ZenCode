/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;

/**
 *
 * @author Hoofdgebruiker
 */
public class PartialStaticMemberGroupExpression implements IPartialExpression {
	public static PartialStaticMemberGroupExpression forMethod(CodePosition position, String name, ITypeID target, FunctionalMemberRef method, ITypeID[] typeArguments) {
		DefinitionMemberGroup group = new DefinitionMemberGroup(true, name);
		group.addMethod(method, TypeMemberPriority.SPECIFIED);
		return new PartialStaticMemberGroupExpression(position, target, group, typeArguments);
	}
	
	private final CodePosition position;
	private final ITypeID target;
	private final DefinitionMemberGroup group;
	private final ITypeID[] typeArguments;
	
	public PartialStaticMemberGroupExpression(CodePosition position, ITypeID target, DefinitionMemberGroup group, ITypeID[] typeArguments) {
		this.position = position;
		this.group = group;
		this.target = target;
		this.typeArguments = typeArguments;
	}
	
	@Override
	public Expression eval() {
		return group.staticGetter(position);
	}

	@Override
	public List<ITypeID>[] predictCallTypes(TypeScope scope, List<ITypeID> hints, int arguments) {
		return group.predictCallTypes(scope, hints, arguments);
	}
	
	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<ITypeID> hints, int arguments) {
		return group.getMethodMembers().stream()
				.filter(method -> method.member.header.parameters.length == arguments && method.member.isStatic())
				.map(method -> method.member.header)
				.collect(Collectors.toList());
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<ITypeID> hints, GenericName name) {
		return eval().getMember(position, scope, hints, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<ITypeID> hints, CallArguments arguments) {
		return group.callStatic(position, target, scope, arguments);
	}
	
	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) {
		return group.staticSetter(position, scope, value);
	}

	@Override
	public ITypeID[] getGenericCallTypes() {
		return typeArguments;
	}
	
	@Override
	public List<ITypeID> getAssignHints() {
		if (group.getSetter() != null)
			return Collections.singletonList(group.getSetter().type);
		if (group.getField() != null)
			return Collections.singletonList(group.getField().type);
		
		return Collections.emptyList();
	}
}
