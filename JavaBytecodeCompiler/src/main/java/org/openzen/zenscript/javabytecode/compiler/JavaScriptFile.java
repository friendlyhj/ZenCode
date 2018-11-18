/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode.compiler;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.javabytecode.JavaScriptMethod;

/**
 * @author Hoofdgebruiker
 */
public class JavaScriptFile {
	public final JavaClassWriter classWriter;
	public final List<JavaScriptMethod> scriptMethods;

	public JavaScriptFile(JavaClassWriter classWriter) {
		this.classWriter = classWriter;
		this.scriptMethods = new ArrayList<>();
	}
}
