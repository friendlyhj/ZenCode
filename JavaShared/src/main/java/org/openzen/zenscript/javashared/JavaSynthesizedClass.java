/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSynthesizedClass {
	public final JavaClass cls;
	public final ITypeID[] typeArguments;
	
	public JavaSynthesizedClass(JavaClass cls, ITypeID[] typeArguments) {
		this.cls = cls;
		this.typeArguments = typeArguments;
	}
}
