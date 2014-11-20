package org.craft.tecbula.gui;

import org.craft.client.*;
import org.craft.client.gui.*;
import org.craft.client.gui.widgets.*;
import org.craft.client.models.*;
import org.craft.client.render.*;
import org.craft.client.render.fonts.*;

public class GuiModelBoxSlot extends GuiListSlot
{

    private ModelBox box;

    public GuiModelBoxSlot(ModelBox box)
    {
        this.box = box;
    }

    @Override
    public void render(int index, int x, int y, int w, int h, int mx, int my, boolean selected, RenderEngine engine, GuiList<?> owner)
    {
        int backgroundColor = 0;
        int color = 0;
        if(isMouseOver(mx, my, x, y, w, h) || selected)
        {
            color = 0xFFE3E000;
            backgroundColor = 0xFF909090;
        }
        else
        {
            color = 0xFFFFFFFF;
            backgroundColor = 0xFF404040;
        }
        Gui.drawColoredRect(engine, x, y, w, h, 0xFF202020);
        Gui.drawColoredRect(engine, x + 1, y + 1, w - 2, h - 2, backgroundColor);
        FontRenderer fontRenderer = OurCraft.getOurCraft().getFontRenderer();
        fontRenderer.setScale(1.25f);
        fontRenderer.drawShadowedString(box.getName(), color, (int) (x + w / 2 - fontRenderer.getTextWidth(box.getName()) / 2), (int) (y + h / 2 - fontRenderer.getCharHeight('A') / 2), engine);
        fontRenderer.setScale(1f);
    }

    public ModelBox getModelBox()
    {
        return box;
    }

}
