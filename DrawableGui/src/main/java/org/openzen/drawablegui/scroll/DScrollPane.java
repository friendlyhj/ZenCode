/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.scroll;

import org.openzen.drawablegui.DAnchor;
import org.openzen.drawablegui.DClipboard;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DTimerHandle;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveInt;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveInt;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.draw.DSubSurface;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;
import org.openzen.drawablegui.style.DStyleSheets;

/**
 *
 * @author Hoofdgebruiker
 */
public class DScrollPane implements DComponent {
	private final DStyleClass styleClass;
	private final DComponent contents;
	private final DScrollBar scrollBar;
	private DDrawSurface surface;
	private DIRectangle bounds;
	
	private DScrollPaneStyle style;
	private int z;
	private DDrawnShape shape;
	private final LiveInt contentsHeight;
	private final LiveInt offsetX;
	private final LiveInt offsetY;
	
	private final LiveObject<DSizing> sizing;
	
	private final ListenerHandle<LiveInt.Listener> contentsHeightListener;
	private final ListenerHandle<LiveInt.Listener> offsetXListener;
	private final ListenerHandle<LiveInt.Listener> offsetYListener;
	private final ListenerHandle<LiveObject.Listener<DSizing>> contentsSizingListener;
	
	private DComponent hovering = null;
	
	private DSubSurface subSurface;
	
	public DScrollPane(DStyleClass styleClass, DComponent contents, LiveObject<DSizing> sizing) {
		this.sizing = sizing;
		this.styleClass = styleClass;
		this.contents = contents;
		
		contentsHeight = new SimpleLiveInt(0);
		offsetX = new SimpleLiveInt(0);
		offsetY = new SimpleLiveInt(0);
		
		scrollBar = new DScrollBar(DStyleClass.EMPTY, contentsHeight, offsetY);
		
		contentsHeightListener = contentsHeight.addListener(new ScrollListener());
		offsetXListener = offsetX.addListener(new ScrollListener());
		offsetYListener = offsetY.addListener(new ScrollListener());
		
		contentsSizingListener = contents.getSizing().addListener((oldPreferences, newPreferences) -> {
			if (bounds == null)
				return;
			
			contents.setBounds(new DIRectangle(0, 0, bounds.width, Math.max(bounds.height, newPreferences.preferredHeight)));
			contentsHeight.setValue(newPreferences.preferredHeight);
		});
	}
	
	@Override
	public void onMounted() {
		contents.onMounted();
	}
	
	@Override
	public void onUnmounted() {
		contents.onUnmounted();
	}

	@Override
	public void mount(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		this.z = z;
		
		DStylePath path = parent.getChild("scrollpane", styleClass);
		subSurface = surface.createSubSurface(z + 1);
		contents.mount(path, 1, subSurface);
		scrollBar.mount(path, z + 2, surface);
		style = new DScrollPaneStyle(surface.getStylesheet(path));
	}
	
	@Override
	public void unmount() {
		subSurface.close();
		
		contents.unmount();
		scrollBar.unmount();
	}

	@Override
	public LiveObject<DSizing> getSizing() {
		return sizing;
	}

	@Override
	public DIRectangle getBounds() {
		return bounds;
	}
	
	@Override
	public int getBaselineY() {
		return -1;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		
		if (shape != null)
			shape.close();
		shape = surface.shadowPath(z, style.shape.instance(style.margin.apply(bounds)), DTransform2D.IDENTITY, style.backgroundColor, style.shadow);
		style.border.update(surface, z + 1, style.margin.apply(bounds));
		
		int height = Math.max(
				bounds.height - style.border.getPaddingHorizontal(),
				contents.getSizing().getValue().preferredHeight);
		int scrollBarWidth = scrollBar.getSizing().getValue().preferredWidth;
		scrollBar.setBounds(new DIRectangle(
				bounds.x + bounds.width - scrollBarWidth - style.border.getPaddingRight() - style.margin.right,
				bounds.y + style.border.getPaddingTop() + style.margin.top,
				scrollBarWidth,
				bounds.height - style.border.getPaddingVertical() - style.margin.getVertical()));
		contents.setBounds(new DIRectangle(0, 0, bounds.width - scrollBar.getBounds().width, height));
		contentsHeight.setValue(height);
		
		subSurface.setOffset(bounds.x - offsetX.getValue(), bounds.y - offsetY.getValue());
		subSurface.setClip(new DIRectangle(
				bounds.x + style.margin.left + style.border.getPaddingLeft(),
				bounds.y + style.margin.top + style.border.getPaddingTop(),
				bounds.width - style.margin.getHorizontal() - style.border.getPaddingHorizontal(),
				bounds.height - style.margin.getVertical() - style.border.getPaddingVertical()));
	}
	
	@Override
	public void onMouseScroll(DMouseEvent e) {
		offsetY.setValue(offsetY.getValue() + e.deltaZ * 50);
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		if (e.x >= scrollBar.getBounds().x) {
			setHovering(scrollBar, e);
		} else {
			setHovering(contents, e);
		}
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		setHovering(null, e);
	}
	
	@Override
	public void onMouseMove(DMouseEvent e) {
		if (e.x >= scrollBar.getBounds().x) {
			if (hovering != scrollBar) {
				setHovering(scrollBar, e);
			} else {
				scrollBar.onMouseMove(e);
			}
		} else {
			if (hovering != contents) {
				setHovering(contents, e);
			} else {
				contents.onMouseMove(translateMouseEvent(e));
			}
		}
	}
	
	@Override
	public void onMouseDrag(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseDrag(forward(hovering, e));
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseClick(forward(hovering, e));
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseDown(forward(hovering, e));
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseRelease(forward(hovering, e));
		
		onMouseMove(e);
	}

	@Override
	public void close() {
		contents.close();
		unmount();
		
		contentsSizingListener.close();
		contentsHeightListener.close();
		offsetXListener.close();
		offsetYListener.close();
	}
	
	private DMouseEvent forward(DComponent target, DMouseEvent e) {
		return target == contents ? translateMouseEvent(e) : e;
	}
	
	private void setHovering(DComponent target, DMouseEvent e) {
		if (target == hovering)
			return;
		
		if (hovering != null)
			hovering.onMouseExit(forward(hovering, e));
		this.hovering = target;
		if (hovering != null)
			hovering.onMouseEnter(forward(hovering, e));
	}
	
	private DMouseEvent translateMouseEvent(DMouseEvent e) {
		return new DMouseEvent(
				e.window,
				toLocalX(e.x),
				toLocalY(e.y),
				e.modifiers,
				e.deltaZ,
				e.clickCount);
	}

	private int toGlobalX(int x) {
		return x + bounds.x - offsetX.getValue();
	}

	private int toGlobalY(int y) {
		return y + bounds.y - offsetY.getValue();
	}

	private int toLocalX(int x) {
		return x - bounds.x + offsetX.getValue();
	}

	private int toLocalY(int y) {
		return y - bounds.y + offsetY.getValue();
	}
	
	private class ScrollListener implements LiveInt.Listener {

		@Override
		public void onChanged(int oldValue, int newValue) {
			int value = offsetY.getValue();
			if (value > contentsHeight.getValue() - bounds.height)
				value = contentsHeight.getValue() - bounds.height;
			if (value < 0)
				value = 0;
			
			if (value != offsetY.getValue())
				offsetY.setValue(value);
			
			subSurface.setOffset(bounds.x - offsetX.getValue(), bounds.y - offsetY.getValue());
		}
	}
}
