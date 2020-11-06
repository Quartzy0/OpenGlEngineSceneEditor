package com.quartzy.editor;

import com.quartzy.engine.graphics.Renderer;
import com.quartzy.engine.layers.Layer;
import com.quartzy.engine.layers.SubscribeEvent;
import com.quartzy.engine.layers.events.*;
import com.quartzy.engine.math.Vector2f;
import imgui.ImGui;
import imgui.glfw.ImGuiImplGlfw;
import lombok.CustomLog;
import org.lwjgl.glfw.GLFW;

@CustomLog
public class ImGuiLayer extends Layer{
    
    private ImGuiImplGlfw implGlfw;
    private ImGuiRenderer imguiRenderer;
    
    private boolean arrowUp;
    private boolean arrowDown;
    private boolean arrowLeft;
    private boolean arrowRight;
    
    public float cameraMoveSpeed = 1f;
    public float cameraZoomSpeed = 1f;
    
    private long lastTimeDrag = 0L;
    private Vector2f prevMousePos = new Vector2f();
    
    public ImGuiLayer(ImGuiRenderer imguiRenderer){
        this.imguiRenderer = imguiRenderer;
    }
    
    @SubscribeEvent
    public void windowResizedEvent(WindowResizeEvent event){
        this.imguiRenderer.windowResize(event.getNewWidth(), event.getNewHeight());
    }
    
    @SubscribeEvent
    public void scrollEvent(MouseScrollEvent event){
        implGlfw.scrollCallback(event.getWindowId(), event.getXOffset(), event.getYOffset());
        event.setHandled(true);
    }
    
    @SubscribeEvent
    public void keyEventPress(KeyPressedEvent event){
        if(imguiRenderer.isGameWindowFocused()){
            if(event.getKeyCode()==GLFW.GLFW_KEY_UP) this.arrowUp = true;
            if(event.getKeyCode()==GLFW.GLFW_KEY_DOWN) this.arrowDown = true;
            if(event.getKeyCode()==GLFW.GLFW_KEY_LEFT) this.arrowLeft = true;
            if(event.getKeyCode()==GLFW.GLFW_KEY_RIGHT) this.arrowRight = true;
        }
        implGlfw.keyCallback(event.getWindowId(), event.getKeyCode(), 0, GLFW.GLFW_PRESS, event.getMods().originalMods);
        event.setHandled(true);
    }
    
    @SubscribeEvent
    public void keyEventRelease(KeyReleasedEvent event){
        if(imguiRenderer.isGameWindowFocused()){
            if(event.getKeyCode()==GLFW.GLFW_KEY_UP) this.arrowUp = false;
            if(event.getKeyCode()==GLFW.GLFW_KEY_DOWN) this.arrowDown = false;
            if(event.getKeyCode()==GLFW.GLFW_KEY_LEFT) this.arrowLeft = false;
            if(event.getKeyCode()==GLFW.GLFW_KEY_RIGHT) this.arrowRight = false;
        }
        implGlfw.keyCallback(event.getWindowId(), event.getKeyCode(), 0, GLFW.GLFW_RELEASE, event.getMods().originalMods);
        event.setHandled(true);
    }
    
    @SubscribeEvent
    public void keyTyped(KeyTypedEvent event){
        implGlfw.charCallback(event.getWindowId(), event.getCodepoint());
        event.setHandled(true);
    }
    
    @SubscribeEvent
    public void mouseButtonPressed(MouseButtonPressedEvent event){
        implGlfw.mouseButtonCallback(event.getWindowId(), event.getButton(), GLFW.GLFW_PRESS, event.getMods().originalMods);
        event.setHandled(true);
    }
    
    @SubscribeEvent
    public void mouseButtonReleased(MouseButtonReleasedEvent event){
        implGlfw.mouseButtonCallback(event.getWindowId(), event.getButton(), GLFW.GLFW_RELEASE, event.getMods().originalMods);
        event.setHandled(true);
    }
    
    @SubscribeEvent
    public void mouseDragged(MouseDragEvent event){
        if(System.currentTimeMillis()-lastTimeDrag>=100){
            prevMousePos = event.getMousePos();
        }
        Vector2f delta = event.getMousePos().subtract(prevMousePos).scale(cameraMoveSpeed);
        Handler.getInstance().editorCamera.translateView(delta.x, -delta.y, 0);
        lastTimeDrag = System.currentTimeMillis();
        prevMousePos = event.getMousePos();
    }
    
    @SubscribeEvent
    public void mouseScrollEvent(MouseScrollEvent event){
        Handler.getInstance().editorCamera.addScale(event.getYOffset() > 0 ? cameraZoomSpeed : -cameraZoomSpeed);
    }
    
    @Override
    public void update(float delta){
        if(!this.imguiRenderer.isGameWindowFocused()){
            this.arrowRight = false;
            this.arrowLeft = false;
            this.arrowDown = false;
            this.arrowUp = false;
        }
        if(this.arrowUp){
            Handler.getInstance().editorCamera.translateView(0, -this.cameraMoveSpeed*delta, 0);
        }
        if(this.arrowDown){
            Handler.getInstance().editorCamera.translateView(0, this.cameraMoveSpeed*delta, 0);
        }
        if(this.arrowLeft){
            Handler.getInstance().editorCamera.translateView(this.cameraMoveSpeed*delta, 0, 0);
        }
        if(this.arrowRight){
            Handler.getInstance().editorCamera.translateView(-this.cameraMoveSpeed*delta, 0, 0);
        }
    }
    
    @Override
    public void render(Renderer renderer){
        this.imguiRenderer.render();
    }
    
    @Override
    public void onDetach(){
        this.imguiRenderer.dispose();
    }
    
    @Override
    public void onAttach(){
        this.imguiRenderer.start();
        this.implGlfw = this.imguiRenderer.getImplGlfw();
    }
}
