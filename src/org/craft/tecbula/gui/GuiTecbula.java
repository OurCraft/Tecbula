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
import org.craft.utils.*;

public class GuiTecbula extends Gui
{

    private OffsettedOpenGLBuffer           buff;
    private float                           yAxis;
    private float                           xAxis;

    private float                           transX;
    private float                           transY;
    private float                           transZ;
    private float                           zoom;
    private boolean                         rightMousePressed;
    private boolean                         middleMousePressed;
    private Stack                           rootStack;
    private ModelBase                       currentModel;
    private HashMap<ModelBox, OpenGLBuffer> buffers;
    private HashMap<ModelBox, OpenGLBuffer> transparentBuffers;
    private HashMap<ModelBox, OpenGLBuffer> wireframeBuffers;
    private Texture                         texture;
    private GuiList<GuiModelBoxSlot>        boxesList;
    private ModelBox                        selectedBox;

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

        boxesList = new GuiList<GuiModelBoxSlot>(4, oc.getDisplayWidth() - 196, 40, 180, oc.getDisplayHeight() - 40, 30);
        boxesList.setYSpacing(2);
        int id = 0;
        for(ModelBox box : currentModel.getChildren())
        {
            boxesList.addSlot(new GuiModelBoxSlot(box));
        }
        addWidget(boxesList);
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
                Log.message(slot.getModelBox().getName());
            }
            else
                selectedBox = null;
        }
    }

    public void draw(int mx, int my, RenderEngine engine)
    {
        glClearColor(0x2D / 255f, 0x2D / 255f, 0x2D / 255f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);
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
            OpenGLBuffer wireframeBuffer = null;
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
                    wireframeBuffer = wireframeBuffers.get(box);
                }
            }
            texture.bind();
            engine.renderBuffer(buffer);
            if(wireframeBuffer != null)
            {
                glLineWidth(2.5f);
                glDepthMask(false);
                glDepthFunc(GL_LEQUAL);
                engine.bindTexture(0, 0);
                engine.renderBuffer(wireframeBuffer, GL_LINES);
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

        engine.bindTexture(0, 0);
        Gui.drawColoredRect(engine, 0, 0, 200, oc.getDisplayHeight(), 0xFF707070);
        Gui.drawColoredRect(engine, 0, 0, oc.getDisplayWidth(), 36, 0xFF707070);
        Gui.drawColoredRect(engine, oc.getDisplayWidth() - 200, 0, 200, oc.getDisplayHeight(), 0xFF707070);
        Gui.drawColoredRect(engine, 2, 2, 196, oc.getDisplayHeight() - 4, 0xFFC0C0C0);

        Gui.drawColoredRect(engine, oc.getDisplayWidth() - 198, 2, 196, oc.getDisplayHeight() - 4, 0xFFC0C0C0);

        Gui.drawColoredRect(engine, 2, 2, 196, oc.getDisplayHeight() - 4, 0xFFC0C0C0);

        Gui.drawColoredRect(engine, 2, 2, oc.getDisplayWidth() - 4, 32, 0xFFC0C0C0);
        super.draw(mx, my, engine);

        getFontRenderer().drawShadowedString("Zoom: " + zoom, 0xFFFFFFFF, 0, 100, engine);
        getFontRenderer().drawShadowedString("xAxis: " + xAxis, 0xFFFFFFFF, 0, 120, engine);
        getFontRenderer().drawShadowedString("yAxis: " + yAxis, 0xFFFFFFFF, 0, 140, engine);
    }

    public void handleButtonReleased(int x, int y, int button)
    {
        super.handleButtonReleased(x, y, button);
        if(inCanvas(x, y))
        {
            if(button == 1)
            {
                rightMousePressed = false;
            }
            else if(button == 2)
            {
                middleMousePressed = false;
            }
        }
    }

    public void handleMouseWheelMovement(int mx, int my, int deltaWheel)
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
    }

    public void handleButtonPressed(int x, int y, int button)
    {
        super.handleButtonPressed(x, y, button);
        if(inCanvas(x, y))
        {
            if(button == 1)
            {
                rightMousePressed = true;
            }
            else if(button == 2)
            {
                middleMousePressed = true;
            }
        }
    }

    public void handleMouseMovement(int x, int y, int dx, int dy)
    {
        super.handleMouseMovement(x, y, dx, dy);
        if(inCanvas(x, y))
        {
            if(rightMousePressed)
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
    }

    private boolean inCanvas(int x, int y)
    {
        return x >= 200 && x <= oc.getDisplayWidth() - 200 && y >= 40 && y <= oc.getDisplayHeight();
    }

}
