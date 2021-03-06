/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.form;

import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DPxDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 * @author Hoofdgebruiker
 */
public class DFormStyle {
	public final int paddingLeft;
	public final int paddingTop;
	public final int paddingRight;
	public final int paddingBottom;
	public final int spacing;

	public final int labelColor;
	public final DFont labelFont;
	public final int minimumLabelSize;
	public final int minimumFieldSize;

	public DFormStyle(DStyleDefinition style) {
		paddingLeft = style.getDimension("paddingLeft", DPxDimension.ZERO);
		paddingTop = style.getDimension("paddingTop", DPxDimension.ZERO);
		paddingRight = style.getDimension("paddingRight", DPxDimension.ZERO);
		paddingBottom = style.getDimension("paddingBottom", DPxDimension.ZERO);
		spacing = style.getDimension("spacing", new DDpDimension(8));

		labelColor = style.getColor("labelColor", 0xFF000000);
		labelFont = style.getFont("labelFont", context -> new DFont(DFontFamily.UI, false, false, false, (int) (14 * context.getScale())));
		minimumLabelSize = style.getDimension("minimumLabelSize", new DDpDimension(150));
		minimumFieldSize = style.getDimension("minimumFieldSize", new DDpDimension(150));
	}
}
