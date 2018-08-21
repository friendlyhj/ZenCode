/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import java.io.File;
import org.json.JSONObject;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.compiler.Target;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaBytecodeJarTarget implements Target {
	private final String module;
	private final String name;
	private final File file;
	private final boolean debugCompiler;
	
	public JavaBytecodeJarTarget(JSONObject definition) {
		module = definition.getString("module");
		name = definition.getString("name");
		file = new File(definition.getString("output"));
		debugCompiler = definition.optBoolean("debugCompiler", false);
	}
	
	@Override
	public JavaCompiler createCompiler(SemanticModule module) {
		return new JavaCompiler(debugCompiler, file);
	}

	@Override
	public String getModule() {
		return module;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean canRun() {
		return true;
	}

	@Override
	public boolean canBuild() {
		return true;
	}
}
