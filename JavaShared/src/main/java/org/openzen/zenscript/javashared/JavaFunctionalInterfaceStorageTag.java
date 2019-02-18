/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import java.lang.reflect.Method;
import org.openzen.zenscript.codemodel.type.storage.AutoStorageTag;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.codemodel.type.storage.SharedStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageType;
import org.openzen.zenscript.codemodel.type.storage.UniqueStorageTag;

public class JavaFunctionalInterfaceStorageTag implements StorageTag {
	public final Method functionalInterfaceMethod;
	
	public JavaFunctionalInterfaceStorageTag(Method functionalInterfaceMethod) {
		this.functionalInterfaceMethod = functionalInterfaceMethod;
	}

	@Override
	public StorageType getType() {
		return JavaFunctionalInterfaceStorageType.INSTANCE;
	}

	@Override
	public boolean canCastTo(StorageTag other) {
		return other instanceof JavaFunctionalInterfaceStorageTag
				|| other instanceof AutoStorageTag
				|| other instanceof SharedStorageTag
				|| other instanceof BorrowStorageTag;
	}

	@Override
	public boolean canCastFrom(StorageTag other) {
		return other instanceof JavaFunctionalInterfaceStorageTag
				|| other instanceof AutoStorageTag
				|| other instanceof SharedStorageTag
				|| other instanceof UniqueStorageTag;
	}

	@Override
	public boolean isDestructible() {
		return false;
	}

	@Override
	public boolean isConst() {
		return true;
	}

	@Override
	public boolean isImmutable() {
		return true;
	}
}
