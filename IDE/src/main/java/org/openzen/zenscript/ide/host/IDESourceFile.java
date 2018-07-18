/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host;

import java.io.IOException;
import java.io.Reader;
import org.openzen.drawablegui.live.LiveString;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IDESourceFile {
	public LiveString getName();
	
	public Reader read() throws IOException;
	
	public void update(String content);
}
