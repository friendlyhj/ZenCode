package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ParsedExpressionMap extends ParsedExpression {

	public static final List<BiFunction<ParsedExpressionMap, ExpressionScope, ? extends IPartialExpression>> compileOverrides = new ArrayList<>(0);

	public final List<ParsedExpression> keys;
	public final List<ParsedExpression> values;

	public ParsedExpressionMap(
			CodePosition position,
			List<ParsedExpression> keys,
			List<ParsedExpression> values) {
		super(position);

		this.keys = keys;
		this.values = values;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		for (BiFunction<ParsedExpressionMap, ExpressionScope, ? extends IPartialExpression> compileOverride : compileOverrides) {
			final IPartialExpression apply = compileOverride.apply(this, scope);
			if (apply != null)
				return apply;
		}

		TypeID usedHint = null;
		List<TypeID> keyHints = new ArrayList<>();
		List<TypeID> valueHints = new ArrayList<>();

		boolean hasAssocHint = false;
		for (TypeID hint : scope.hints) {
			if (hint instanceof AssocTypeID) {
				usedHint = hint;
				AssocTypeID assocHint = (AssocTypeID) hint;
				if (!keyHints.contains(assocHint.keyType))
					keyHints.add(assocHint.keyType);
				if (!valueHints.contains(assocHint.valueType))
					valueHints.add(assocHint.valueType);

				hasAssocHint = true;
			} else if (hint instanceof GenericMapTypeID) {
				try {
					FunctionalMemberRef constructor = scope
							.getTypeMembers(hint)
							.getOrCreateGroup(OperatorType.CONSTRUCTOR)
							.selectMethod(position, scope, CallArguments.EMPTY, true, true);
					return new NewExpression(position, hint, constructor, CallArguments.EMPTY);
				} catch (CompileException ex) {
					return new InvalidExpression(ex.position, hint, ex.code, ex.getMessage());
				}
			}
		}

		if (keys.isEmpty() && keyHints.size() == 1) {
			FunctionalMemberRef constructor = scope
					.getTypeMembers(usedHint)
					.getOrCreateGroup(OperatorType.CONSTRUCTOR)
					.selectMethod(position, scope, CallArguments.EMPTY, true, true);
			return new NewExpression(position, usedHint, constructor, CallArguments.EMPTY);
		}

		// TODO: check if this should still be commented out
		//if (!hasAssocHint && scope.hints.size() == 1) {
		//	// compile as constructor call
		//	StoredType hint = scope.hints.get(0);
		//	for (int i = 0; i < keys.size(); i++) {
		//		if (keys.get(i) != null)
		//			throw new CompileException(position, CompileExceptionCode.UNSUPPORTED_NAMED_ARGUMENTS, "Named constructor arguments not yet supported");
		//	}
		//	ParsedCallArguments arguments = new ParsedCallArguments(null, values);
		//	return ParsedNewExpression.compile(position, hint, arguments, scope);
		//}

		Expression[] cKeys = new Expression[keys.size()];
		Expression[] cValues = new Expression[values.size()];

		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i) == null)
				throw new CompileException(position, CompileExceptionCode.MISSING_MAP_KEY, "Missing key");

			cKeys[i] = keys.get(i).compileKey(scope.withHints(keyHints));
			cValues[i] = values.get(i).compile(scope.withHints(valueHints)).eval();
		}

		TypeID keyType = null;
		for (Expression key : cKeys) {
			if (key.type == keyType)
				continue;

			if (keyType == null) {
				keyType = key.type;
			} else {
				keyType = scope.getTypeMembers(keyType).union(key.type);
			}
		}
		if (keyType == null)
			throw new CompileException(position, CompileExceptionCode.UNTYPED_EMPTY_MAP, "Empty map without known type");

		for (int i = 0; i < cKeys.length; i++)
			cKeys[i] = cKeys[i].castImplicit(position, scope, keyType);

		TypeID valueType = null;
		for (Expression value : cValues) {
			if (value.type == valueType)
				continue;

			if (valueType == null) {
				valueType = value.type;
			} else {
				valueType = scope.getTypeMembers(valueType).union(value.type);
			}
		}
		if (valueType == null)
			throw new CompileException(position, CompileExceptionCode.UNTYPED_EMPTY_MAP, "Empty map without known type");

		for (int i = 0; i < cValues.length; i++)
			cValues[i] = cValues[i].castImplicit(position, scope, valueType);

		AssocTypeID asType = scope.getTypeRegistry().getAssociative(keyType, valueType);
		return new MapExpression(position, cKeys, cValues, asType);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
