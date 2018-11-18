/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;

/**
 *
 * @author Hoofdgebruiker
 */
public class ScriptBlock extends Taggable {
	public final SourceFile file;
	public final Module module;
	public final ZSPackage pkg;
	public final FunctionHeader scriptHeader;
	public final List<Statement> statements;
	
	public ScriptBlock(SourceFile file, Module module, ZSPackage pkg, FunctionHeader scriptHeader, List<Statement> statements) {
		this.file = file;
		this.module = module;
		this.pkg = pkg;
		this.scriptHeader = scriptHeader;
		this.statements = statements;
	}
	
	public ScriptBlock withStatements(List<Statement> newStatements) {
		ScriptBlock result = new ScriptBlock(file, module, pkg, scriptHeader, newStatements);
		result.addAllTagsFrom(this);
		return result;
	}
	
	public ScriptBlock normalize(TypeScope scope) {
		List<Statement> normalized = new ArrayList<>();
		for (Statement statement : statements) {
			normalized.add(statement.normalize(scope, ConcatMap.empty(LoopStatement.class, LoopStatement.class)));
		}
		ScriptBlock result = new ScriptBlock(file, module, pkg, scriptHeader, normalized);
		result.addAllTagsFrom(this);
		return result;
	}
}
