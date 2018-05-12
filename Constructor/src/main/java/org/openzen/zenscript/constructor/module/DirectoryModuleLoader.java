/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.constructor.ConstructorException;
import org.openzen.zenscript.constructor.JSONUtils;
import org.openzen.zenscript.constructor.Module;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.linker.symbol.TypeSymbol;
import org.openzen.zenscript.parser.ParsedFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class DirectoryModuleLoader implements ModuleReference {
	private final ModuleLoader loader;
	private final String moduleName;
	private final File directory;
	private final boolean isStdlib;
	
	public DirectoryModuleLoader(ModuleLoader loader, String moduleName, File directory, boolean isStdlib) {
		this.loader = loader;
		this.moduleName = moduleName;
		this.directory = directory;
		this.isStdlib = isStdlib;
	}
	
	@Override
	public String getName() {
		return moduleName;
	}

	@Override
	public SemanticModule load() {
		if (!directory.exists())
			throw new ConstructorException("Error: module directory not found: " + directory);
		
		File jsonFile = new File(directory, "module.json");
		if (!jsonFile.exists())
			throw new ConstructorException("Error: module.json file not found in module " + moduleName);
		
		try {
			JSONObject json = JSONUtils.load(jsonFile);

			List<String> dependencyNames = new ArrayList<>();
			if (!isStdlib)
				dependencyNames.add("stdlib");

			JSONArray jsonDependencies = json.optJSONArray("dependencies");
			if (jsonDependencies != null) {
				for (int i = 0; i < jsonDependencies.length(); i++) {
					dependencyNames.add(jsonDependencies.getString(i));
				}
			}

			ModuleSpace space = new ModuleSpace();
			for (String dependencyName : dependencyNames)
				space.addModule(dependencyName, loader.getModule(dependencyName));

			Module module = new Module(moduleName, directory, jsonFile);
			ZSPackage pkg = new ZSPackage(module.packageName);

			ParsedFile[] parsedFiles = module.parse(pkg);
			SemanticModule result = Module.compileSyntaxToSemantic(pkg, parsedFiles, space);
			
			JSONObject globals = json.optJSONObject("globals");
			if (globals != null) {
				for (String key : globals.keySet()) {
					JSONObject global = globals.getJSONObject(key);
					switch (global.getString("type")) {
						case "Definition":
							result.globals.put(key, new TypeSymbol(result.definitions.getDefinition(global.getString("definition"))));
							break;
						default:
							throw new ConstructorException("Invalid global type: " + global.getString("type"));
					}
				}
			}
			
			return result;
		} catch (IOException ex) {
			throw new ConstructorException("Loading module files failed: " + ex.getMessage(), ex);
		}
	}
}