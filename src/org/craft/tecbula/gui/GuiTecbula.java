package org.craft.tecbula.gui;

import static org.lwjgl.opengl.GL11.*;

import java.io.*;
import java.util.*;

import com.google.common.collect.*;

import org.craft.blocks.*;
import org.craft.client.*;
import org.craft.client.gui.*;
import org.craft.client.gui.widgets.*;
import org.craft.client.models.*;
import org.craft.client.render.*;
import org.craft.inventory.Stack;
import org.craft.maths.*;
import org.craft.resources.*;

public class GuiTecbula extends Gui
{

    private OffsettedOpenGLBuffer           buff;
    private float                           yAxis;
    private float                           xAxis;

    private float                           transX;
    private float                           transY;
    private float                           transZ;
    private float                           zoom;
    private boolean                         leftMousePressed;
    private boolean                         middleMousePressed;
    private Stack                           rootStack;
    private ModelBase                       currentModel;
    private HashMap<ModelBox, OpenGLBuffer> buffers;
    private HashMap<ModelBox, OpenGLBuffer> transparentBuffers;
    private HashMap<ModelBox, OpenGLBuffer> wireframeBuffers;
    private Texture                         texture;
    private GuiList<GuiModelBoxSlot>        boxesList;
    private ModelBox                        selectedBox;
    private GuiSpinner                      widthSpinner;
    private GuiSpinner                      heightSpinner;
    private GuiSpinner                      depthSpinner;
    private GuiSpinner                      xOffSpinner;
    private GuiSpinner                      yOffSpinner;
    private GuiSpinner                      zOffSpinner;

    private static final int                BACKGROUND_COLOR   = 0xFF333333;
    private static final int                BORDER_COLOR       = 0xFF5D5D5D;
    private static final int                PANEL_BACK_COLOR   = 0xFF666666;
    private static final int                PANEL_BORDER_COLOR = 0xFFA9A9A9;

    public GuiTecbula(OurCraft game)
    {
        super(game);
        buffers = Maps.newHashMap();
        transparentBuffers = Maps.newHashMap();
        wireframeBuffers = Maps.newHashMap();
    }

    @Override
    public boolean requiresMouse()
    {
        return true;
    }

    @Override
    public void init()
    {
        xAxis = -0.47f;
        yAxis = 0.50f;
        zoom = 2.0f;
        currentModel = new ModelBase();

        rootStack = new Stack(Blocks.log, 1);
        buff = new OffsettedOpenGLBuffer();
        buff.setToPlane(-2f, 0, -2f, 5f, 0, 5f);
        buff.setOffsetToEnd();
        ModelBox body = new ModelBox(0.15f, 1f, 0.15f, 0.70f, 0.70f, 0.70f);
        body.setName("body");
        body.setPixelRatio(11.428f);

        ModelBox bodyBase = new ModelBox(0f, 0f, 0f, 1f, 1f, 1f);
        bodyBase.setName("bodyBase");
        bodyBase.setPixelRatio(8);

        ModelBox head = new ModelBox(0.25f, 1.7f, 0.25f, 0.50f, 0.50f, 0.50f);
        head.setName("head");
        head.setPixelRatio(16f);

        currentModel.addBox(body);
        currentModel.addBox(bodyBase);
        currentModel.addBox(head);
        try
        {
            texture = OpenGLHelper.loadTexture(oc.getAssetsLoader().getResource(new ResourceLocation("ourcraft", "textures/entities/test.png")));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        buff.clearAndDisposeVertices();
        addWidget(new GuiIconButton(1, oc.getDisplayWidth() - 38, 3, new ResourceLocation("tecbula", "textures/gui/exit.png")));

        addWidget(new GuiIconButton(2, 4, 3, new ResourceLocation("tecbula", "textures/gui/new.png")));
        addWidget(new GuiIconButton(3, 38, 3, new ResourceLocation("tecbula", "textures/gui/save.png")));

        int offsetY = (int) getFontRenderer().getCharHeight('A') + 42;
        xOffSpinner = new GuiSpinner(8, 4, offsetY, 190 / 3, 40, getFontRenderer());
        yOffSpinner = new GuiSpinner(9, 4 + 190 / 3, offsetY, 190 / 3, 40, getFontRenderer());
        zOffSpinner = new GuiSpinner(10, 4 + 190 / 3 * 2, offsetY, 190 / 3, 40, getFontRenderer());
        addWidget(xOffSpinner);
        addWidget(yOffSpinner);
        addWidget(zOffSpinner);

        offsetY = (int) getFontRenderer().getCharHeight('A') + 110;
        widthSpinner = new GuiSpinner(5, 4, offsetY, 190 / 3, 40, getFontRenderer());
        heightSpinner = new GuiSpinner(6, 4 + 190 / 3, offsetY, 190 / 3, 40, getFontRenderer());
        depthSpinner = new GuiSpinner(7, 4 + 190 / 3 * 2, offsetY, 190 / 3, 40, getFontRenderer());
        addWidget(widthSpinner);
        addWidget(heightSpinner);
        addWidget(depthSpinner);

        boxesList = new GuiList<GuiModelBoxSlot>(4, oc.getDisplayWidth() - 192, 80, 175, oc.getDisplayHeight() - 100, 30);
        boxesList.setYSpacing(2);
        updateModelTree();
        addWidget(boxesList);
    }

    private void updateModelTree()
    {
        boxesList.clear();
        for(ModelBox box : currentModel.getChildren())
        {
            boxesList.addSlot(new GuiModelBoxSlot(box));
        }
    }

    public void actionPerformed(GuiWidget w)
    {
        if(w.getID() == 1)
        {
            oc.openMenu(new GuiMainMenu(oc));
        }
        else if(w.getID() == 4)
        {
            GuiModelBoxSlot slot = boxesList.getSelected();
            if(slot != null)
            {
                selectedBox = slot.getModelBox();
                xOffSpinner.setValue(selectedBox.getX());
                yOffSpinner.setValue(selectedBox.getY());
                zOffSpinner.setValue(selectedBox.getZ());

                widthSpinner.setValue(selectedBox.getWidth());
                heightSpinner.setValue(selectedBox.getHeight());
                depthSpinner.setValue(selectedBox.getDepth());
            }
            else
                selectedBox = null;
        }
        else if(w.getID() == 5 || w.getID() == 6 || w.getID() == 7
                || w.getID() == 8 || w.getID() == 9 || w.getID() == 10)
        {
            if(selectedBox != null)
            {
                selectedBox.setX(xOffSpinner.getValue());
                selectedBox.setY(yOffSpinner.getValue());
                selectedBox.setZ(zOffSpinner.getValue());

                selectedBox.setWidth(widthSpinner.getValue());
                selectedBox.setHeight(heightSpinner.getValue());
                selectedBox.setDepth(depthSpinner.getValue());
                buffers.remove(selectedBox);
                wireframeBuffers.remove(selectedBox);
                transparentBuffers.remove(selectedBox);
            }
        }

    }

    public void render(int mx, int my, RenderEngine engine)
    {
        // E9CDFA
        glClearColor(0xE9 / 255f, 0xCD / 255f, 0xFA / 255f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);
        renderWorkspace(mx, my, engine);
        engine.bindTexture(0, 0);
        Gui.drawColoredRect(engine, 0, 0, 200, oc.getDisplayHeight(), BORDER_COLOR);
        Gui.drawColoredRect(engine, 0, 0, oc.getDisplayWidth(), 36, BORDER_COLOR);
        Gui.drawColoredRect(engine, oc.getDisplayWidth() - 200, 0, 200, oc.getDisplayHeight(), BORDER_COLOR);
        Gui.drawColoredRect(engine, 2, 2, 196, oc.getDisplayHeight() - 4, BACKGROUND_COLOR);

        Gui.drawColoredRect(engine, oc.getDisplayWidth() - 198, 2, 196, oc.getDisplayHeight() - 4, BACKGROUND_COLOR);

        Gui.drawColoredRect(engine, 2, 2, 196, oc.getDisplayHeight() - 4, BACKGROUND_COLOR);

        Gui.drawColoredRect(engine, 2, 2, oc.getDisplayWidth() - 4, 32, BACKGROUND_COLOR);

        renderModelTree(mx, my, engine);
        super.render(mx, my, engine);

        String s = "Offset";
        getFontRenderer().drawShadowedString(s, 0xFFFFFFFF, (int) (200 / 2 - getFontRenderer().getTextWidth(s) / 2), 40, engine);
        s = "Size";
        getFontRenderer().drawShadowedString(s, 0xFFFFFFFF, (int) (200 / 2 - getFontRenderer().getTextWidth(s) / 2), 110, engine);
    }

    private void renderWorkspace(int mx, int my, RenderEngine engine)
    {
        engine.begin();
        engine.switchToPerspective();
        engine.enableGLCap(GL_ALPHA_TEST);
        engine.setAlphaFunc(GL_GREATER, 0.0001f);
        engine.enableGLCap(GL_BLEND);
        engine.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        engine.enableGLCap(GL_DEPTH_TEST);

        Matrix4 oldModelView = engine.getModelviewMatrix();
        Matrix4 modelView = Matrix4.get().initTranslation(-0.5f, -1, zoom)
                .mul(Matrix4.get().initTranslation(0.5f, 0, 0.5f)
                        .mul(Matrix4.get().initTranslation(transX, transY, transZ))
                        .mul(new Quaternion(Vector3.xAxis, xAxis)
                                .mul(new Quaternion(Vector3.yAxis, yAxis)).toRotationMatrix())
                        .mul(Matrix4.get().initTranslation(-0.5f, 0, -0.5f)));
        engine.setModelviewMatrix(modelView);
        engine.bindLocation(new ResourceLocation("tecbula", "textures/grid.png"));
        engine.renderBuffer(buff);
        oc.getRenderItems().renderItem(engine, rootStack, null, 0, -1, 0);

        for(ModelBox box : currentModel.getChildren())
        {
            if(box == null)
                continue;

            Matrix4 rot = box.getRotation().toRotationMatrix();
            Matrix4 translation = Matrix4.get().initTranslation(0, 0, 0);
            Quaternion erot = new Quaternion(Vector3.yAxis, 0);
            Matrix4 rot1 = erot.toRotationMatrix();

            Matrix4 finalMatrix = (rot.mul(rot1));
            engine.setModelviewMatrix(modelView.mul(finalMatrix));

            OpenGLBuffer buffer = null;
            OpenGLBuffer transpBuffer = null;
            float alpha = 1.0f;
            if(selectedBox != null && box != selectedBox)
            {
                alpha = 0.5f;
                if(!transparentBuffers.containsKey(box))
                {
                    buffer = new OpenGLBuffer();
                    box.prepareBuffer(texture, buffer, alpha);
                    transparentBuffers.put(box, buffer);
                }
                buffer = transparentBuffers.get(box);
            }
            else
            {
                if(!buffers.containsKey(box))
                {
                    buffer = new OpenGLBuffer();
                    box.prepareBuffer(texture, buffer, alpha);
                    buffers.put(box, buffer);
                }
                buffer = buffers.get(box);
                if(selectedBox == box)
                {
                    if(!wireframeBuffers.containsKey(box))
                    {
                        OpenGLBuffer wireframe = new OpenGLBuffer();
                        box.prepareWireframeBuffer(wireframe);
                        wireframeBuffers.put(box, wireframe);
                    }
                    transpBuffer = wireframeBuffers.get(box);
                }
            }
            texture.bind();
            engine.renderBuffer(buffer);
            if(transpBuffer != null)
            {
                glLineWidth(2.5f);
                glDepthMask(false);
                glDepthFunc(GL_LEQUAL);
                engine.bindTexture(0, 0);
                engine.renderBuffer(transpBuffer, GL_LINES);
                glDepthFunc(GL_LESS);
                glDepthMask(true);
                glLineWidth(1f);
            }
            translation.dispose();
            rot1.dispose();
            rot.dispose();
            finalMatrix.dispose();
        }

        modelView.dispose();
        engine.setModelviewMatrix(oldModelView);
        engine.disableGLCap(GL_ALPHA_TEST);
        engine.disableGLCap(GL_DEPTH_TEST);
        engine.end();
        engine.switchToOrtho();

    }

    public void renderModelTree(int mx, int my, RenderEngine engine)
    {
        getFontRenderer().setScale(1.5f);
        Gui.drawColoredRect(engine, oc.getDisplayWidth() - 196, 36, 192, oc.getDisplayHeight() - 34 * 2, PANEL_BORDER_COLOR);
        Gui.drawColoredRect(engine, oc.getDisplayWidth() - 194, (int) (38 + getFontRenderer().getCharWidth('A') + 5), 188, oc.getDisplayHeight() - 24 * 2, PANEL_BACK_COLOR);
        String s = "Model tree";
        getFontRenderer().drawShadowedString(s, 0xFFFFFFFF, (int) (oc.getDisplayWidth() - 196 / 2 - getFontRenderer().getTextWidth(s) / 2), 36, engine);
        getFontRenderer().setScale(1f);
    }

    public boolean onButtonReleased(int x, int y, int button)
    {
        super.onButtonReleased(x, y, button);
        if(inCanvas(x, y))
        {
            if(button == 0)
            {
                leftMousePressed = false;
            }
            else if(button == 2)
            {
                middleMousePressed = false;
            }
        }
        return false;
    }

    public boolean handleMouseWheelMovement(int mx, int my, int deltaWheel)
    {
        super.handleMouseWheelMovement(mx, my, deltaWheel);
        if(inCanvas(mx, my))
        {
            zoom -= deltaWheel / 240f;

            if(zoom < 0f)
            {
                zoom = 0f;
            }
        }
        return false;
    }

    public boolean onButtonPressed(int x, int y, int button)
    {
        super.onButtonPressed(x, y, button);
        if(inCanvas(x, y))
        {
            if(button == 0)
            {
                leftMousePressed = true;
            }
            else if(button == 2)
            {
                middleMousePressed = true;
            }
        }
        return false;
    }

    public boolean handleMouseMovement(int x, int y, int dx, int dy)
    {
        super.handleMouseMovement(x, y, dx, dy);
        if(inCanvas(x, y))
        {
            if(leftMousePressed)
            {
                yAxis -= Math.toRadians(dx);
                xAxis += Math.toRadians(dy);
            }
            if(middleMousePressed)
            {
                transY += dy / 20f;
                transX += dx / 20f;
            }
        }
        else
        {
            leftMousePressed = false;
            middleMousePressed = false;
        }
        return false;
    }

    private boolean inCanvas(int x, int y)
    {
        return x >= 200 && x <= oc.getDisplayWidth() - 200 && y >= 40 && y <= oc.getDisplayHeight();
    }

    public void buildMenu(GuiPopupMenu popupMenu)
    {
        super.buildMenu(popupMenu);
        popupMenu.addSlot(new GuiPopupElement(0, "Add a new box", fontRenderer));
        popupMenu.addSlot(new GuiPopupElement(1, "Copy current box", fontRenderer));
        popupMenu.addSlot(new GuiPopupElement(2, "Paste current box", fontRenderer));
        popupMenu.addSlot(new GuiPopupElement(3, "Delete current box", fontRenderer));
    }

    public void onPopupMenuClicked(GuiPopupElement elem)
    {
        super.onPopupMenuClicked(elem);
        if(elem.getID() == 0)
        {
            ModelBox box = new ModelBox(0, 0, 0, 1, 1, 1);
            currentModel.addBox(box);
            box.setPixelRatio(8);
            box.setName("Test" + currentModel.getChildren().size());
            updateModelTree();
        }
    }

}
