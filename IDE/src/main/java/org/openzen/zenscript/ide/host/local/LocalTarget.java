/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.openzen.zenscript.compiler.Target;
import org.openzen.zenscript.compiler.ZenCodeCompiler;
import org.openzen.zenscript.constructor.Library;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.constructor.Project;
import org.openzen.zenscript.constructor.module.DirectoryModuleLoader;
import org.openzen.zenscript.constructor.module.ModuleReference;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.ide.host.IDETarget;

/**
 *
 * @author Hoofdgebruiker
 */
public class LocalTarget implements IDETarget {
	private final Project project;
	private final Target target;
	
	public LocalTarget(Project project, Target target) {
		this.project = project;
		this.target = target;
	}

	@Override
	public String getName() {
		return target.getName();
	}

	@Override
	public boolean canBuild() {
		return target.canBuild();
	}

	@Override
	public boolean canRun() {
		return target.canRun();
	}

	@Override
	public void build() {
		buildInternal();
	}

	@Override
	public void run() {
		ZenCodeCompiler compiler = buildInternal();
		if (compiler != null)
			compiler.run();
	}
	
	private ZenCodeCompiler buildInternal() {
		CompilationUnit compilationUnit = new CompilationUnit();
		ModuleLoader moduleLoader = new ModuleLoader(compilationUnit);
		moduleLoader.register("stdlib", new DirectoryModuleLoader(moduleLoader, "stdlib", new File("../../StdLibs/stdlib"), true));
		Set<String> compiledModules = new HashSet<>();
		
		try {
			Project project = new Project(moduleLoader, this.project.directory);
			for (Library library : project.libraries) {
				for (ModuleReference module : library.modules)
					moduleLoader.register(module.getName(), module);
			}
			for (ModuleReference module : project.modules) {
				moduleLoader.register(module.getName(), module);
			}
			
			SemanticModule module = moduleLoader.getModule(target.getModule());
			module = module.normalize();
			module.validate();
			
			ZenCodeCompiler compiler = target.createCompiler(module);
			if (!module.isValid())
				return compiler;
			
			SemanticModule stdlib = moduleLoader.getModule("stdlib");
			stdlib = stdlib.normalize();
			stdlib.validate();
			if (!stdlib.isValid())
				return compiler;
			stdlib.compile(compiler);
			
			boolean isValid = compileDependencies(moduleLoader, compiler, compiledModules, module);
			if (!isValid)
				return compiler;
			
			module.compile(compiler);
			return compiler;
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private boolean compileDependencies(ModuleLoader loader, ZenCodeCompiler compiler, Set<String> compiledModules, SemanticModule module) {
		for (String dependency : module.dependencies) {
			if (compiledModules.contains(module.name))
				continue;
			
			SemanticModule dependencyModule = loader.getModule(dependency);
			module = module.normalize();
			module.validate();
			if (!module.isValid())
				return false;
			
			if (!compileDependencies(loader, compiler, compiledModules, dependencyModule))
				return false;
			
			module.compile(compiler);
			compiledModules.add(module.name);
		}
		
		return true;
	}
}
