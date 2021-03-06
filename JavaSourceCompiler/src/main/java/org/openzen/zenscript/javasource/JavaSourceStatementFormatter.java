/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openzen.zencode.shared.StringExpansion;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.RangeExpression;
import org.openzen.zenscript.codemodel.expression.switchvalue.CharSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.EnumConstantSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.IntSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.StringSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValueVisitor;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.BreakStatement;
import org.openzen.zenscript.codemodel.statement.CatchClause;
import org.openzen.zenscript.codemodel.statement.ContinueStatement;
import org.openzen.zenscript.codemodel.statement.DoWhileStatement;
import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ForeachStatement;
import org.openzen.zenscript.codemodel.statement.IfStatement;
import org.openzen.zenscript.codemodel.statement.LockStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.SwitchCase;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.codemodel.statement.ThrowStatement;
import org.openzen.zenscript.codemodel.statement.TryCatchStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.WhileStatement;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.formattershared.ExpressionString;
import org.openzen.zenscript.formattershared.StatementFormatter;
import org.openzen.zenscript.formattershared.StatementFormattingSubBlock;
import org.openzen.zenscript.formattershared.StatementFormattingTarget;
import org.openzen.zenscript.javasource.scope.JavaSourceStatementScope;
import org.openzen.zenscript.javashared.JavaClass;

/**
 * @author Hoofdgebruiker
 */
public class JavaSourceStatementFormatter implements StatementFormatter.Formatter, SwitchValueVisitor<String> {
	protected final JavaSourceStatementScope scope;

	public JavaSourceStatementFormatter(JavaSourceStatementScope scope) {
		this.scope = scope;
	}

	@Override
	public JavaSourceStatementFormatter forLoop(LoopStatement statement) {
		return new JavaSourceStatementFormatter(scope.createBlockScope(statement));
	}

	@Override
	public void formatBlock(StatementFormattingTarget target, BlockStatement statement) {
		target.writeBlock("{", statement, "}");
	}

	@Override
	public void formatBreak(StatementFormattingTarget target, BreakStatement statement) {
		if (scope.innerLoop == statement.target)
			target.writeLine("break;");
		else
			target.writeLine("break " + statement.target.label + ";");
	}

	@Override
	public void formatContinue(StatementFormattingTarget target, ContinueStatement statement) {
		if (scope.innerLoop == statement.target)
			target.writeLine("continue;");
		else
			target.writeLine("continue " + statement.target.label + ";");
	}

	@Override
	public void formatDoWhile(StatementFormattingTarget target, DoWhileStatement statement) {
		String condition = scope.expression(target, statement.condition).value;
		target.writeInner("do", statement.content, statement, "while (" + condition + ");");
	}

	@Override
	public void formatEmpty(StatementFormattingTarget target, EmptyStatement statement) {
		target.writeLine(";");
	}

	@Override
	public void formatExpression(StatementFormattingTarget target, ExpressionStatement statement) {
		target.writeLine(scope.expression(target, statement.expression) + ";");
	}

	@Override
	public void formatForeach(StatementFormattingTarget target, ForeachStatement statement) {
		if (statement.iterator.target.getBuiltin() == null) {
			iterateOverCustomIterator(target, statement);
			return;
		}

		switch (statement.iterator.target.getBuiltin()) {
			case ITERATOR_INT_RANGE:
				iterateOverIntRange(target, statement);
				break;
			case ITERATOR_ARRAY_VALUES:
				iterateOverArrayValues(target, statement);
				break;
			case ITERATOR_ARRAY_KEY_VALUES:
				iteratorOverArrayKeyValues(target, statement);
				break;
			case ITERATOR_ASSOC_KEYS:
				iterateOverAssocKeys(target, statement);
				break;
			case ITERATOR_ASSOC_KEY_VALUES:
				iterateOverAssocKeyValues(target, statement);
				break;
			case ITERATOR_STRING_CHARS:
				iterateOverStringCharacters(target, statement);
				break;
			default:
				throw new IllegalArgumentException("Invalid iterator: " + statement.iterator.target.getBuiltin());
		}
	}

	@Override
	public void formatIf(StatementFormattingTarget target, IfStatement statement) {
		target.writeInner("if (" + scope.expression(target, statement.condition).value + ")", statement.onThen, null, "");
		if (statement.onElse != null)
			target.writeInner("else", statement.onElse, null, "");
	}

	@Override
	public void formatLock(StatementFormattingTarget target, LockStatement statement) {
		target.writeInner("lock (" + scope.expression(target, statement.object) + ")", statement.content, null, "");
	}

	@Override
	public void formatReturn(StatementFormattingTarget target, ReturnStatement statement) {
		if (statement.value == null)
			target.writeLine("return;");
		else if (statement.value.aborts()) // throw or panic
			target.writeLine(scope.expression(target, statement.value).value + ";");
		else
			target.writeLine("return " + scope.expression(target, statement.value).value + ";");
	}

	@Override
	public void formatSwitch(StatementFormattingTarget target, SwitchStatement statement) {
		if (statement.value.type.isVariant()) {
			Expression value = scope.duplicable(target, statement.value);
			ExpressionString valueString = scope.expression(target, value);
			List<StatementFormattingSubBlock> blocks = new ArrayList<>();

			DefinitionTypeID variantType = statement.value.type.asDefinition();
			HighLevelDefinition variant = variantType.definition;
			String variantTypeName = scope.type(variant);
			for (SwitchCase switchCase : statement.cases) {
				VariantOptionSwitchValue switchValue = (VariantOptionSwitchValue) switchCase.value;
				String header = switchValue == null ? "default:" : "case " + switchValue.option.getName() + ":";
				List<String> statements = new ArrayList<>();
				if (switchValue != null) {
					for (int i = 0; i < switchValue.parameters.length; i++) {
						StringBuilder statementOutput = new StringBuilder();
						statementOutput.append(scope.type(switchValue.option.types[i])).append(" ").append(switchValue.parameters[i]).append(" = ((").append(variantTypeName).append(".").append(switchValue.option.getName());
						if (variant.typeParameters != null && variant.typeParameters.length > 0) {
							statementOutput.append("<");
							for (int j = 0; j < variantType.typeArguments.length; j++) {
								if (j > 0)
									statementOutput.append(", ");
								statementOutput.append(scope.type(variantType.typeArguments[j]));
							}
							statementOutput.append(">");
						}
						statementOutput.append(")").append(valueString.value).append(").").append("value").append(";");
						statements.add(statementOutput.toString());
					}
				}
				blocks.add(new StatementFormattingSubBlock(header, statements, switchCase.statements));
			}

			target.writeInnerMulti("switch (" + valueString.value + ".getDiscriminant()) {", blocks, statement, "}");
		} else {
			List<StatementFormattingSubBlock> blocks = new ArrayList<>();
			for (SwitchCase switchCase : statement.cases) {
				String header = switchCase.value == null ? "default:" : "case " + switchCase.value.accept(this) + ":";
				blocks.add(new StatementFormattingSubBlock(header, Collections.emptyList(), switchCase.statements));
			}

			target.writeInnerMulti("switch (" + scope.expression(target, statement.value) + ") {", blocks, statement, "}");
		}
	}

	@Override
	public void formatThrow(StatementFormattingTarget target, ThrowStatement statement) {
		target.writeLine("throw " + scope.expression(target, statement.value).value + ";");
	}

	@Override
	public void formatTryCatch(StatementFormattingTarget target, TryCatchStatement statement) {
		target.writeInner("try", statement.content, null, "");
		for (CatchClause catchClause : statement.catchClauses)
			target.writeInner("catch " + catchClause.exceptionVariable.name, catchClause.content, null, "");
		if (statement.finallyClause != null)
			target.writeInner("finally", statement.finallyClause, null, "");
	}

	@Override
	public void formatVar(StatementFormattingTarget target, VarStatement statement) {
		StringBuilder result = new StringBuilder();
		result.append(scope.type(statement.type));
		result.append(" ");
		result.append(statement.name);
		if (statement.initializer != null) {
			result.append(" = ");
			result.append(scope.expression(target, statement.initializer));
		}
		result.append(";");

		target.writeLine(result.toString());
	}

	@Override
	public void formatWhile(StatementFormattingTarget target, WhileStatement statement) {
		target.writeInner("while (" + scope.expression(target, statement.condition) + ")", statement.content, statement, "");
	}

	@Override
	public String acceptInt(IntSwitchValue value) {
		return Integer.toString(value.value);
	}

	@Override
	public String acceptChar(CharSwitchValue value) {
		return StringExpansion.escape(Character.toString(value.value), '\'', true);
	}

	@Override
	public String acceptString(StringSwitchValue value) {
		return StringExpansion.escape(value.value, '"', true);
	}

	@Override
	public String acceptEnumConstant(EnumConstantSwitchValue value) {
		return value.constant.name;
	}

	@Override
	public String acceptVariantOption(VariantOptionSwitchValue value) {
		return value.option.getName();
	}

	private void iterateOverIntRange(StatementFormattingTarget target, ForeachStatement statement) {
		String name = statement.loopVariables[0].name;

		if (statement.list instanceof RangeExpression) {
			String limitName = "limitFor" + StringExpansion.capitalize(name);
			RangeExpression range = (RangeExpression) (statement.list);
			target.writeLine("int " + limitName + " = " + scope.expression(target, range.to) + ";");
			target.writeInner(
					"for (int " + name + " = " + scope.expression(target, range.from) + "; " + name + " < " + limitName + "; " + name + "++)",
					statement.content,
					statement,
					"");
		} else {
			target.writeLine("IntRange rangeFor" + name + " = " + scope.expression(target, statement.list) + ";");
			target.writeInner(
					"for (int " + name + " = rangeFor" + name + ".from; i < rangeFor" + name + ".to; " + name + "++)",
					statement.content,
					statement,
					"");
		}
	}

	private void iterateOverArrayValues(StatementFormattingTarget target, ForeachStatement statement) {
		target.writeInner(
				"for (" + scope.type(statement.loopVariables[0].type) + " " + statement.loopVariables[0].name + " : " + scope.expression(target, statement.list) + ")",
				statement.content,
				statement,
				"");
	}

	private void iteratorOverArrayKeyValues(StatementFormattingTarget target, ForeachStatement statement) {
		ExpressionString list = scope.expression(target, scope.duplicable(target, statement.list));

		target.writeInner(
				"for (int " + statement.loopVariables[0].name + " = 0; i < " + list.value + ".length; i++) {",
				new String[]{
						scope.type(statement.loopVariables[1].type) + " " + statement.loopVariables[1].name + " = " + list.value + "[" + statement.loopVariables[0].name + "];"
				},
				statement.content,
				statement,
				"}");
	}

	private void iterateOverCustomIterator(StatementFormattingTarget target, ForeachStatement statement) {
		target.writeInner(
				"for (" + scope.type(statement.loopVariables[0].type) + " " + statement.loopVariables[0].name + " : " + scope.expression(target, statement.list) + ")",
				statement.content,
				statement,
				"");
	}

	private void iterateOverStringCharacters(StatementFormattingTarget target, ForeachStatement statement) {
		target.writeInner(
				"for (char " + statement.loopVariables[0].name + " : " + scope.expression(target, statement.list).value + ".toCharArray())",
				statement.content,
				statement,
				"");
	}

	private void iterateOverAssocKeys(StatementFormattingTarget target, ForeachStatement statement) {
		VarStatement key = statement.loopVariables[0];
		target.writeInner(
				"for (" + scope.type(key.type) + " " + key + " : " + scope.expression(target, statement.list).value + ".keySet())",
				statement.content,
				statement,
				"");
	}

	private void iterateOverAssocKeyValues(StatementFormattingTarget target, ForeachStatement statement) {
		String temp = scope.createTempVariable();
		VarStatement key = statement.loopVariables[0];
		VarStatement value = statement.loopVariables[1];
		target.writeInner(
				"for (Map.Entry<" + scope.type(key.type) + ", " + scope.type(value.type) + "> " + temp + " : " + scope.expression(target, statement.list).value + ".entrySet()) {",
				new String[]{
						scope.type(key.type) + " " + key.name + " = " + temp + ".getKey();",
						scope.type(value.type) + " " + value.name + " = " + temp + ".getValue();"
				},
				statement.content,
				statement,
				"}");
	}
}
