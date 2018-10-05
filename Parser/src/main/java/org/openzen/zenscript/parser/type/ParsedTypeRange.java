/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeArgument;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeRange implements IParsedType {
	private final CodePosition position;
	private final IParsedType from;
	private final IParsedType to;
	private final int modifiers;
	
	public ParsedTypeRange(CodePosition position, IParsedType from, IParsedType to) {
		this.position = position;
		this.from = from;
		this.to = to;
		this.modifiers = 0;
	}
	
	private ParsedTypeRange(CodePosition position, IParsedType from, IParsedType to, int modifiers) {
		this.position = position;
		this.from = from;
		this.to = to;
		this.modifiers = modifiers;
	}

	@Override
	public IParsedType withOptional() {
		return new ParsedTypeRange(position, from, to, modifiers | ModifiedTypeID.MODIFIER_OPTIONAL);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedTypeRange(position, from, to, this.modifiers | modifiers);
	}
	
	@Override
	public StoredType compile(TypeResolutionContext context) {
		StoredType from = this.from.compile(context);
		StoredType to = this.to.compile(context);
		if (!from.equals(to))
			return new InvalidTypeID(position, CompileExceptionCode.NO_SUCH_TYPE, "from and to in a range must be the same type").stored(from.storage);
		
		return context.getTypeRegistry().getModified(modifiers, context.getTypeRegistry().getRange(from)).stored(from.storage);
	}
	
	@Override
	public TypeID compileUnstored(TypeResolutionContext context) {
		StoredType from = this.from.compile(context);
		StoredType to = this.to.compile(context);
		if (!from.equals(to))
			return new InvalidTypeID(position, CompileExceptionCode.NO_SUCH_TYPE, "from and to in a range must be the same type");
		
		return context.getTypeRegistry().getModified(modifiers, context.getTypeRegistry().getRange(from));
	}
	
	@Override
	public TypeArgument compileArgument(TypeResolutionContext context) {
		StoredType from = this.from.compile(context);
		StoredType to = this.to.compile(context);
		if (!from.equals(to))
			return new TypeArgument(new InvalidTypeID(position, CompileExceptionCode.NO_SUCH_TYPE, "from and to in a range must be the same type"), null);
		
		return new TypeArgument(context.getTypeRegistry().getModified(modifiers, context.getTypeRegistry().getRange(from)), from.storage);
	}
}
