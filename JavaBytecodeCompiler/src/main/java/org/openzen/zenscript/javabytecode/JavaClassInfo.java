/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.Type;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaClassInfo {
	public static JavaClassInfo get(Class<?> cls) {
		return new JavaClassInfo(Type.getInternalName(cls));
	}
	
	public final String internalClassName;
	
	public JavaClassInfo(String internalClassName) {
		this.internalClassName = internalClassName;
	}
}
