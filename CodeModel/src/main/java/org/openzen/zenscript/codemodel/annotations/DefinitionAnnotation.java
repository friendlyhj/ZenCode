/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.scope.BaseScope;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DefinitionAnnotation {
	public static final DefinitionAnnotation[] NONE = new DefinitionAnnotation[0];
	
	public void apply(HighLevelDefinition definition, BaseScope scope);
	
	public void applyOnSubtype(HighLevelDefinition definition, BaseScope scope);
}
