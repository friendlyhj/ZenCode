package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javabytecode.JavaMethodInfo;
import org.openzen.zenscript.javabytecode.JavaModule;
import org.openzen.zenscript.javabytecode.compiler.*;
import org.openzen.zenscript.javashared.JavaClass;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class JavaDefinitionVisitor implements DefinitionVisitor<byte[]> {
	private static final JavaMethodInfo CLASS_FORNAME = new JavaMethodInfo(
			JavaClass.CLASS,
			"forName",
			"(Ljava/lang/String;)Ljava/lang/Class;",
			Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC);


	private static final JavaMethodInfo ENUM_VALUEOF = new JavaMethodInfo(
			JavaClass.ENUM,
			"valueOf",
			"(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;",
			Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC);

	private final JavaClassWriter outerWriter;

	public JavaDefinitionVisitor(JavaClassWriter outerWriter) {

		this.outerWriter = outerWriter;
	}

	@Override
	public byte[] visitClass(ClassDefinition definition) {
		//Classes will always be created in a new File/Class

		final Type superType;
		if (definition.getSuperType() == null)
			superType = Type.getType(Object.class);
		else
			superType = Type.getType(definition.getSuperType().accept(JavaTypeClassVisitor.INSTANCE));

		JavaClass toClass = new JavaClass(definition.pkg.fullName, definition.name, JavaClass.Kind.CLASS);
		JavaClassWriter writer = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);

		//TODO: Calculate signature from generic parameters
		//TODO: Interfaces?
		String signature = null;


		writer.visit(Opcodes.V1_8, definition.modifiers, definition.name, signature, superType.getInternalName(), null);
		JavaMemberVisitor memberVisitor = new JavaMemberVisitor(writer, toClass, definition);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberVisitor);
		}
		memberVisitor.end();
		writer.visitEnd();
		return writer.toByteArray();
	}

	@Override
	public byte[] visitInterface(InterfaceDefinition definition) {
		JavaClass toClass = new JavaClass(definition.pkg.fullName, definition.name, JavaClass.Kind.INTERFACE);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		//TODO: Calculate signature from generic parameters
		//TODO: Extending Interfaces?
		String signature = null;
		writer.visit(Opcodes.V1_8, definition.modifiers | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, definition.name, signature, Type.getInternalName(Object.class), null);
		JavaMemberVisitor memberVisitor = new JavaMemberVisitor(writer, toClass, definition);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberVisitor);
		}
		memberVisitor.end();
		writer.visitEnd();
		return writer.toByteArray();
	}

	@Override
	public byte[] visitEnum(EnumDefinition definition) {
		System.out.println("Compiling enum " + definition.name + " in " + definition.position.getFilename());

		final Type superType;
		if (definition.getSuperType() == null)
			superType = Type.getType(Object.class);
		else
			superType = Type.getType(definition.getSuperType().accept(JavaTypeClassVisitor.INSTANCE));

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		writer.visit(Opcodes.V1_8, Opcodes.ACC_ENUM | Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER | Opcodes.ACC_FINAL, definition.name, "Ljava/lang/Enum<L" + definition.name + ";>;", superType.getInternalName(), null);

		JavaClass toClass = new JavaClass(definition.pkg.fullName, definition.name, JavaClass.Kind.ENUM);

		//Enum Stuff(required!)
		writer.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC, "$VALUES", "[L" + definition.name + ";", null, null).visitEnd();

		final JavaMemberVisitor visitor = new JavaMemberVisitor(writer, toClass, definition);
		for (IDefinitionMember member : definition.members) {
			member.accept(visitor);
		}

		JavaClass arrayClass = JavaClass.fromInternalName("[L" + definition.name + ";", JavaClass.Kind.ARRAY);
		JavaMethodInfo arrayClone = new JavaMethodInfo(arrayClass, "clone", "()Ljava/lang/Object;", Opcodes.ACC_PUBLIC);

		JavaMethodInfo valuesMethodInfo = new JavaMethodInfo(toClass, "values", "()[L" + definition.name + ";", Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC);
		JavaWriter valuesWriter = new JavaWriter(writer, true, valuesMethodInfo, definition, null, null);
		valuesWriter.start();
		valuesWriter.getStaticField(definition.name, "$VALUES", "[L" + definition.name + ";");
		valuesWriter.invokeVirtual(arrayClone);
		valuesWriter.checkCast("[L" + definition.name + ";");
		valuesWriter.returnObject();
		valuesWriter.end();

		JavaMethodInfo valueOfMethodInfo = new JavaMethodInfo(toClass, "valueOf", "(Ljava/lang/String;)L" + definition.name + ";", Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC);
		JavaWriter valueOfWriter = new JavaWriter(writer, true, valueOfMethodInfo, definition, null, null);
		valueOfWriter.start();
		valueOfWriter.invokeStatic(CLASS_FORNAME);
		valueOfWriter.loadObject(0);
		valueOfWriter.invokeStatic(ENUM_VALUEOF);
		valueOfWriter.checkCast("L" + definition.name + ";");
		valueOfWriter.returnObject();
		valueOfWriter.end();


		writer.visitEnd();
		return writer.toByteArray();
	}

	@Override
	public byte[] visitStruct(StructDefinition definition) {
		return null;
	}

	@Override
	public byte[] visitFunction(FunctionDefinition definition) {
		CompilerUtils.tagMethodParameters(definition.header, true);

		final String signature = CompilerUtils.calcSign(definition.header, false);

		final JavaClass toClass = new JavaClass(definition.pkg.fullName, CompilerUtils.calcClasName(definition.position), JavaClass.Kind.CLASS);
		final JavaMethodInfo methodInfo = new JavaMethodInfo(toClass, definition.name, CompilerUtils.calcDesc(definition.header, false), CompilerUtils.calcAccess(definition.modifiers) | Opcodes.ACC_STATIC);

		final JavaWriter writer = new JavaWriter(outerWriter, true, methodInfo, definition, signature, null);
		final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(writer);
		statementVisitor.start();
		boolean returns = definition.statement.accept(statementVisitor);
		if (!returns) {
			ITypeID type = definition.header.returnType;
			if (CompilerUtils.isPrimitive(type))
				writer.iConst0();
			else if (type != BasicTypeID.VOID)
				writer.aConstNull();
			writer.returnType(type.accept(JavaTypeVisitor.INSTANCE));
		}

		statementVisitor.end();

		definition.setTag(JavaMethodInfo.class, methodInfo);
		definition.caller.setTag(JavaMethodInfo.class, methodInfo);
		return null;
	}

	@Override
	public byte[] visitExpansion(ExpansionDefinition definition) {
		return null;
	}

	@Override
	public byte[] visitAlias(AliasDefinition definition) {
		throw new AssertionError("Aliases shouldn't exist here...");
	}

	@Override
	public byte[] visitVariant(VariantDefinition variant) {

		final String variantName = variant.name;
		final JavaClass toClass = new JavaClass("", variantName, JavaClass.Kind.CLASS);
		final JavaClassWriter writer = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);

		writer.visit(Opcodes.V1_8, Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, variantName, null, "java/lang/Object", null);
		writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "getDenominator", "()I", null, null).visitEnd();


		final JavaMemberVisitor visitor = new JavaMemberVisitor(writer, toClass, variant);

		final List<VariantDefinition.Option> options = variant.options;
		//Each option is one of the possible child classes
		for (final VariantDefinition.Option option : options) {
			final String optionClassName = variantName + "$" + option.name;
			final JavaClass optionClass = new JavaClass("", optionClassName, JavaClass.Kind.CLASS);
			final JavaClassWriter optionWriter = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);

			writer.visitInnerClass(optionClassName, variantName, option.name, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL);


			option.setTag(JavaClass.class, optionClass);


			//Generic option signature
			final String signature;
			{
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < option.types.length; ++i) {
					builder.append("<T").append(i).append(":");
					builder.append(option.types[i].accept(JavaTypeVisitor.INSTANCE).getDescriptor());
				}
				builder.append(">");
				builder.append("L").append(variantName).append(";");


				signature = builder.toString();
			}

			optionWriter.visit(Opcodes.V1_8, Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, optionClassName, signature, variantName, null);
			final JavaMemberVisitor optionVisitor = new JavaMemberVisitor(optionWriter, optionClass, variant);


			final StringBuilder optionInitDescBuilder = new StringBuilder("(");

			ITypeID[] types = option.types;
			for (int i = 0; i < types.length; ++i) {
				final Type type = types[i].accept(JavaTypeVisitor.INSTANCE);
				final String internalName = type.getDescriptor();
				optionInitDescBuilder.append(internalName);
				optionWriter.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "Field" + i, internalName, "TT" + i + ";", null).visitEnd();
			}
			optionInitDescBuilder.append(")V");


			final JavaWriter initWriter = new JavaWriter(optionWriter, new JavaMethodInfo(optionClass, "<init>", optionInitDescBuilder.toString(), Opcodes.ACC_PUBLIC), variant, optionInitDescBuilder.toString(), null);
			initWriter.start();
			initWriter.loadObject(0);
			initWriter.dup();
			initWriter.invokeSpecial(variantName, "<init>", "()V");
			for (int i = 0; i < types.length; ++i) {
				initWriter.dup();
				initWriter.loadObject(i + 1);

				final Type type = types[i].accept(JavaTypeVisitor.INSTANCE);
				final String internalName = type.getDescriptor();

				initWriter.putField(optionClassName, "Field" + i, internalName);
			}
			initWriter.pop();
			initWriter.ret();
			initWriter.end();


			//Denominator for switch-cases
			final JavaWriter getDenominator = new JavaWriter(optionWriter, new JavaMethodInfo(optionClass, "getDenominator", "()I", Modifiers.PUBLIC), null, null, null, "java/lang/Override");
			getDenominator.start();
			getDenominator.constant(option.ordinal);
			getDenominator.returnInt();
			getDenominator.end();


			optionVisitor.end();
			optionWriter.visitEnd();
			final byte[] byteArray = optionWriter.toByteArray();
			JavaModule.classes.put(optionClassName, byteArray);

			//Print the option files, won't be in production
			try (FileOutputStream out = new FileOutputStream(optionClassName + ".class")) {
				out.write(byteArray);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		for (final IDefinitionMember member : variant.members) {
			member.accept(visitor);
		}

		final JavaWriter superInitWriter = new JavaWriter(writer, new JavaMethodInfo(toClass, "<init>", "()V", Opcodes.ACC_PUBLIC), variant, "()V", null);
		superInitWriter.start();
		superInitWriter.loadObject(0);
		superInitWriter.invokeSpecial("java/lang/Object", "<init>", "()V");
		superInitWriter.ret();
		superInitWriter.end();

		visitor.end();
		writer.visitEnd();


		return writer.toByteArray();
	}
}
