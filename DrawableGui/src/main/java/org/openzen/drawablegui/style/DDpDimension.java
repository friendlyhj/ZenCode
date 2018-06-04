/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

import org.openzen.drawablegui.DUIContext;

/**
 *
 * @author Hoofdgebruiker
 */
public class DDpDimension implements DDimension {
	private final float value;
	
	public DDpDimension(float value) {
		this.value = value;
	}

	@Override
	public float eval(DUIContext context) {
		return value * context.getScale();
	}
}
