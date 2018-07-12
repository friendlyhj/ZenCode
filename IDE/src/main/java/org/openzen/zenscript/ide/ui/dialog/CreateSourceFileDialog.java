/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.dialog;

import org.openzen.drawablegui.DAnchor;
import org.openzen.drawablegui.DButton;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DHorizontalLayout;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DInputField;
import org.openzen.drawablegui.DLabel;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.drawablegui.DVerticalLayout;
import org.openzen.drawablegui.form.DForm;
import org.openzen.drawablegui.form.DFormComponent;
import org.openzen.drawablegui.live.ImmutableLiveBool;
import org.openzen.drawablegui.live.ImmutableLiveString;
import org.openzen.drawablegui.live.SimpleLiveString;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.zenscript.ide.host.IDEModule;
import org.openzen.zenscript.ide.host.IDEPackage;
import org.openzen.zenscript.ide.ui.IDEWindow;

/**
 *
 * @author Hoofdgebruiker
 */
public class CreateSourceFileDialog {
	private final IDEWindow ideWindow;
	private final IDEModule module;
	private final IDEPackage pkg;
	
	private final SimpleLiveString name;
	
	private final DComponent root;
	private final DInputField input;
	
	private DUIWindow window;
	
	public CreateSourceFileDialog(IDEWindow ideWindow, IDEModule module, IDEPackage pkg) {
		this.ideWindow = ideWindow;
		this.module = module;
		this.pkg = pkg;
		
		name = new SimpleLiveString("");
		input = new DInputField(DStyleClass.EMPTY, name, new DDpDimension(100));
		input.setOnEnter(this::ok);
		input.setOnEscape(this::cancel);
		
		DForm form = new DForm(
				DStyleClass.EMPTY,
				new DFormComponent("Module:", new DLabel(DStyleClass.EMPTY, new ImmutableLiveString(module.getName()))),
				new DFormComponent("Package:", new DLabel(DStyleClass.EMPTY, new ImmutableLiveString(pkg.getName().isEmpty() ? "(module root)" : pkg.getName()))),
				new DFormComponent("Filename:", input));

		DButton ok = new DButton(DStyleClass.EMPTY, new SimpleLiveString("Create"), ImmutableLiveBool.FALSE, this::ok);
		DButton cancel = new DButton(DStyleClass.EMPTY, new SimpleLiveString("Cancel"), ImmutableLiveBool.FALSE, this::cancel);
		DHorizontalLayout buttons = new DHorizontalLayout(
				DStyleClass.EMPTY,
				DHorizontalLayout.Alignment.RIGHT,
				new DHorizontalLayout.Element(cancel, 0, 0, DHorizontalLayout.ElementAlignment.TOP),
				new DHorizontalLayout.Element(ok, 0, 0, DHorizontalLayout.ElementAlignment.TOP));

		root = new DVerticalLayout(
				DStyleClass.EMPTY,
				DVerticalLayout.Alignment.MIDDLE,
				new DVerticalLayout.Element(form, 1, 1, DVerticalLayout.ElementAlignment.CENTER),
				new DVerticalLayout.Element(buttons, 0, 0, DVerticalLayout.ElementAlignment.RIGHT));
	}
	
	public void open(DUIWindow parent) {
		DIRectangle rectangle = parent.getWindowBounds();
		window = parent.getContext().openDialog(rectangle.getCenterX(), rectangle.getCenterY(), DAnchor.MIDDLE_LEFT, "Create source file", root);
		window.focus(input);
	}
	
	private void cancel() {
		window.close();
	}
	
	private void ok() {
		window.close();
		pkg.createSourceFile(name.getValue() + ".zs");
	}
}