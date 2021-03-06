package org.openzen.zenscript.codemodel.scope;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;

import java.util.List;

public class BlockScope extends StatementScope {
	private final StatementScope parent;

	public BlockScope(StatementScope parent) {
		this.parent = parent;
	}

	@Override
	public ZSPackage getRootPackage() {
		return parent.getRootPackage();
	}

	@Override
	public LocalMemberCache getMemberCache() {
		return parent.getMemberCache();
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) throws CompileException {
		IPartialExpression fromSuper = super.get(position, name);
		if (fromSuper != null)
			return fromSuper;

		return parent.get(position, name);
	}

	@Override
	public LoopStatement getLoop(String name) {
		return parent.getLoop(name);
	}

	@Override
	public FunctionHeader getFunctionHeader() {
		return parent.getFunctionHeader();
	}

	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
		return parent.getType(position, name);
	}

	@Override
	public TypeID getThisType() {
		return parent.getThisType();
	}

	@Override
	public DollarEvaluator getDollar() {
		return parent.getDollar();
	}

	@Override
	public IPartialExpression getOuterInstance(CodePosition position) throws CompileException {
		return parent.getOuterInstance(position);
	}

	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return parent.getAnnotation(name);
	}

	@Override
	public TypeMemberPreparer getPreparer() {
		return parent.getPreparer();
	}

	@Override
	public GenericMapper getLocalTypeParameters() {
		return parent.getLocalTypeParameters();
	}
}
