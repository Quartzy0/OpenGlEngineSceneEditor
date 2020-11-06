package com.quartzy.editor;

import com.quartzy.editor.componentHandlers.ComponentHandler;
import com.quartzy.engine.Client;
import com.quartzy.engine.ecs.Component;
import com.quartzy.engine.ecs.ComponentManager;
import com.quartzy.engine.ecs.ECSManager;
import com.quartzy.engine.ecs.components.CustomRenderer;
import com.quartzy.engine.ecs.components.TransformComponent;
import com.quartzy.engine.graphics.Framebuffer;
import com.quartzy.engine.graphics.Renderer;
import com.quartzy.engine.graphics.Window;
import com.quartzy.engine.math.Vector2f;
import com.quartzy.engine.utils.Logger;
import com.quartzy.engine.world.World;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import lombok.CustomLog;
import lombok.Getter;
import org.dyn4j.geometry.Transform;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.util.*;

@CustomLog
public class ImGuiRenderer{
    @Getter
    private ImGuiImplGl3 implGl3;
    @Getter
    private ImGuiImplGlfw implGlfw;
    
    private short selectedEntity;
    private short prevSelectedEntity;
    
    @Getter
    private boolean gameWindowFocused;
    
    public void start(){
        log.info("Initializing ImGui renderer");
        implGl3 = new ImGuiImplGl3();
        implGlfw = new ImGuiImplGlfw();
        
        ImGui.createContext();
        Window window = Client.getInstance().getWindow();
        implGlfw.init(window.getId(), false);
    
        ImGuiIO io = ImGui.getIO();
        io.setDisplaySize(600, 400);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        implGl3.init("#version 150");
    }
    
    private Component selected = null;
    private int selectedIndex = 0;
    
    private ImString text;
    
    public void render(){
        implGlfw.newFrame();
        ImGui.newFrame();
        
        this.setupDockspace();
        
//        ImGui.setNextWindowSize(200, 200, ImGuiCond.Once);
//        ImGui.setNextWindowPos(10, 10, ImGuiCond.Once);
    
        if(ImGui.beginMainMenuBar()){
            if(ImGui.beginMenu("File")){
                if(ImGui.menuItem("Open")){
                    open();
                }
                if(ImGui.menuItem("Save As")){
                    saveAs();
                }
                if(ImGui.menuItem("Save")){
                    File file = Handler.getInstance().currentlyOpenedFile;
                    if(file==null || !file.exists()){
                        saveAs();
                    }else{
                        World.saveToFile(file, World.getCurrentWorld());
                    }
                }
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }
    
        ECSManager ecsManager = World.getCurrentWorld().getEcsManager();
        HashMap<Short, List<Component>> allEntities = new HashMap<>();
    
        Collection<ComponentManager> values = ecsManager.getComponents().values();
        for(ComponentManager value : values){
            HashMap<Short, List<Component>> components = value.getComponents();
            for(Map.Entry<Short, List<Component>> entry : components.entrySet()){
                if(entry.getKey()==Handler.getInstance().editorCameraEntity)continue;
                if(allEntities.containsKey(entry.getKey())){
                    allEntities.get(entry.getKey()).addAll(entry.getValue());
                }else {
                    allEntities.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
            }
        }
        
        ImGui.begin("Scene hierarchy");
    
        if(ImGui.button("+")){
            short object = ecsManager.createObject();
            ecsManager.addComponentToEntityNoCheck(object, new TransformComponent(new Transform()));
        }
    
        for(Short entry : allEntities.keySet()){
            String tag = ecsManager.getTag(entry);
            if(ImGui.button("-")){
                ecsManager.removeEntity(entry);
            }
            ImGui.sameLine();
            if(ImGui.selectable(tag !=null ? tag : "Entity " + entry, selectedEntity==entry)){
                selectedEntity = entry;
            }
        }
    
        ImGui.end();
        
        ImGui.begin("Entity");
        
        List<Component> value = allEntities.get(selectedEntity);
        if(value!=null){
            String tag = ecsManager.getTag(selectedEntity);
            if(prevSelectedEntity!=selectedEntity)text=null;
            if(text==null){
                text = new ImString(tag);
                if(text.getData().length<128){
                    text.resize(128);
                }
            }
            ImGui.inputText("Tag", text);
            String text1 = this.text.get();
            if((tag==null || !tag.equals(text1)) && !text1.isEmpty()){
                ecsManager.setEntityTag(selectedEntity, text1);
            }
            
            ImGui.newLine();
    
            if(ImGui.beginCombo("Add component", "+", ImGuiComboFlags.NoPreview)){
    
                ArrayList<Class<? extends Component>> supportedComponents = ComponentHandler.getSupportedComponents();
                for(Class<? extends Component> component : supportedComponents){
                    if(ecsManager.entityHasComponent(selectedEntity, component)){
                        if(!ComponentHandler.canHaveMultiple(component))continue;
                    }
                    String s = ComponentHandler.classToFriendlyName(component);
                    if(ImGui.selectable(s)){
                        ecsManager.addComponentToEntity(selectedEntity, ComponentHandler.createDefComponent(component));
                    }
                }
    
                ImGui.endCombo();
            }
            
            HashMap<String, Integer> nameCounter = new HashMap<>();
    
            for(Component component : value){
                if(ImGui.button("-")){
                    ecsManager.removeComponent(selectedEntity, component);
                }
                ImGui.sameLine();
                String label = ComponentHandler.classToFriendlyName(component.getClass());
                int orDefault = nameCounter.getOrDefault(label, 0);
                nameCounter.put(label, orDefault + 1);
                if(ImGui.selectable(label + (orDefault==0 ? "" : " " + orDefault))){
                    selected = component;
                    selectedIndex = orDefault;
                }
            }
        }
        
        ImGui.end();

        ImGui.begin("Component Editor");
        
        if(prevSelectedEntity!=selectedEntity){
            this.selected = null;
        }
    
        if(this.selected!=null){
            ComponentHandler.getHandler(this.selected, this.selectedIndex).draw(this.selected);
        }

        ImGui.end();
        
        ImGui.begin("Game view", ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);
        
        this.gameWindowFocused = ImGui.isWindowFocused(ImGuiFocusedFlags.None);
    
        Vector2f windowSize = getLargestSizeForViewport();
        Vector2f windowPos = getCenteredPositionForViewport(windowSize);
        
        ImGui.setCursorPos(windowPos.x, windowPos.y);
        
        Framebuffer framebuffer = Client.getInstance().getRenderer().getFramebuffer();
        ImGui.image(framebuffer.getTexture().getId(), windowSize.x, windowSize.y, 0, 1, 1, 0);
        ImGui.end();
        
//        ImGui.showDemoWindow();
        
        prevSelectedEntity = selectedEntity;
        
//        ImGui.showDemoWindow();
        
        this.renderImGui();
    }
    
    private Vector2f getCenteredPositionForViewport(Vector2f aspectSize){
        ImVec2 windowSize = new ImVec2();
        ImGui.getContentRegionAvail(windowSize);
        windowSize.x -= ImGui.getScrollX();
        windowSize.y -= ImGui.getScrollY();
        
        float viewportX = (windowSize.x / 2.0f) - (aspectSize.x / 2.0f);
        float viewportY = (windowSize.y / 2.0f) - (aspectSize.y / 2.0f);
        
        return new Vector2f(viewportX, viewportY);
    }
    
    private Vector2f getLargestSizeForViewport(){
        ImVec2 windowSize = new ImVec2();
        ImGui.getContentRegionAvail(windowSize);
        windowSize.x -= ImGui.getScrollX();
        windowSize.y -= ImGui.getScrollY();
        
        float aspectWidth = windowSize.x;
        float aspectHeight = aspectWidth / Client.getInstance().getWindow().getAspectRatio();
        if(aspectHeight > windowSize.y){
            aspectHeight = windowSize.y;
            aspectWidth = aspectHeight * Client.getInstance().getWindow().getAspectRatio();
        }
        
        return new Vector2f(aspectWidth, aspectHeight);
    }
    
    public void windowResize(int newWidth, int newHeight){
        ImGui.getIO().setDisplaySize(newWidth, newHeight);
    }
    
    public void dispose(){
        implGl3.dispose();
        implGlfw.dispose();
        
        ImGui.destroyContext();
    }
    
    private void setupDockspace(){
        Window window = Client.getInstance().getWindow();
        
        int windowFlags = ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoDocking;
    
        ImGui.setNextWindowPos(0f, 0f, ImGuiCond.Always);
        ImGui.setNextWindowSize(window.getWidth(), window.getHeight());
        
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f);
        windowFlags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus;
        
        ImGui.begin("Dockspace", new ImBoolean(true), windowFlags);
        ImGui.popStyleVar(2);
        
        //Dockspace
        ImGui.dockSpace(ImGui.getID("Dockspace"));
    }
    
    private void renderImGui(){
        ImGui.end();
        
        ImGui.render();
    
        implGl3.render(ImGui.getDrawData());
    }
    
    public void saveAs(){
        MemoryStack stack = MemoryStack.stackPush();
        PointerBuffer filters = stack.mallocPointer(1);
        filters.put(stack.UTF8("*.wrld"));
        filters.flip();
        String path = TinyFileDialogs.tinyfd_saveFileDialog("Save world", null, filters, "A world file");
        stack.pop();
        if(path!=null && !path.isEmpty()){
            if(!path.endsWith(".wrld"))path = path + ".wrld";
            File file = new File(path);
            Handler.getInstance().currentlyOpenedFile = file;
            World.saveToFile(file, World.getCurrentWorld(), Handler.getInstance().editorCameraEntity);
        }
    }
    
    public void open(){
        MemoryStack stack = MemoryStack.stackPush();
        PointerBuffer filters = stack.mallocPointer(1);
        filters.put(stack.UTF8("*.wrld"));
        filters.flip();
        String path = TinyFileDialogs.tinyfd_openFileDialog("Open world file", null, filters, "A world file", false);
        stack.pop();
        if(path!=null && !path.isEmpty()){
            File file = new File(path);
            World world = World.loadWorld(file, Handler.getInstance().editorCameraEntity);
            if(world!=null){
                World.setCurrentWorld(world);
                Handler.getInstance().currentlyOpenedFile = file;
            }else {
                TinyFileDialogs.tinyfd_messageBox("Error", "The file was either not found or is empty/invalid", "ok", "error", true);
            }
        }
    }
}
