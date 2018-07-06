/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IDefinitionMember {
	public CodePosition getPosition();
	
	public String describe();
	
	public BuiltinID getBuiltin();
	
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper);
	
	public <T> T accept(MemberVisitor<T> visitor);
	
	public <T> T getTag(Class<T> tag);
	
	public <T> void setTag(Class<T> tag, T value);

	boolean hasTag(Class<?> tag);
}
