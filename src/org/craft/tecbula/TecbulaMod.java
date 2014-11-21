package org.craft.tecbula;

import java.util.*;

import org.craft.client.*;
import org.craft.client.gui.*;
import org.craft.client.gui.widgets.*;
import org.craft.modding.*;
import org.craft.modding.events.*;
import org.craft.modding.events.gui.*;
import org.craft.tecbula.gui.*;
import org.slf4j.*;

@Mod(id = "tecbula", author = "jglrxavpok", name = "Tecbula", version = "Secret 0.0.1")
public class TecbulaMod
{
    public static final int TECBULA_BUTTON_ID = 404;
    private Logger          logger;

    @OurModEventHandler
    public void onPreInit(ModPreInitEvent evt)
    {
        logger = evt.getLogger();
        logger.info("Tecbula loading...");
    }

    @OurModEventHandler
    public void onGuiPostBuilding(GuiBuildingEvent.Post evt)
    {
        if(evt.getMenu() instanceof GuiMainMenu)
        {
            GuiPanel gui = evt.getMenu();
            List<GuiWidget> widgets = gui.getAllWidgets();
            int x = 0;
            int y = 0;
            int w = 0;
            int h = 0;
            for(GuiWidget widget : widgets)
            {
                if(widget.getID() == 2) // 2 is the id of the settings button
                {
                    GuiButton settingsButton = (GuiButton) widget;
                    settingsButton.setWidth(settingsButton.getWidth() / 2 - 10);
                    x = settingsButton.getX();
                    y = settingsButton.getY();
                    w = settingsButton.getWidth();
                    h = settingsButton.getHeight();
                    settingsButton.setLocation(x + settingsButton.getWidth() + 20, y);
                }
            }
            GuiButton tecbulaButton = new GuiButton(TECBULA_BUTTON_ID, x, y, w, h, "Tecbula", gui.getFontRenderer());
            gui.addWidget(tecbulaButton);
        }
    }

    @OurModEventHandler
    public void onActionPerformed(GuiActionPerformedEvent evt)
    {
        if(evt.getMenu() instanceof GuiMainMenu)
        {
            if(evt.getWidget().getID() == TECBULA_BUTTON_ID)
            {
                OurCraft.getOurCraft().openMenu(new GuiTecbula(OurCraft.getOurCraft()));
            }
        }
    }

    @OurModEventHandler
    public void onPostInit(ModPostInitEvent evt)
    {
        logger.info("Tecbula fully loaded!");
    }
}
