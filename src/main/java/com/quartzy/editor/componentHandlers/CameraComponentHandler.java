package com.quartzy.editor.componentHandlers;

import com.quartzy.engine.ecs.components.CameraComponent;
import com.quartzy.engine.math.Vector3f;
import com.quartzy.engine.world.World;
import imgui.ImGui;
import imgui.type.ImFloat;

import java.util.HashMap;
import java.util.List;

public class CameraComponentHandler extends ComponentHandler<CameraComponent>{
    
    @Override
    public void draw(CameraComponent component){
        if(ImGui.button("Set as main")){
            HashMap<Short, List<CameraComponent>> allEntitiesWithComponent = World.getCurrentWorld().getEcsManager().getAllEntitiesWithComponent(CameraComponent.class);
            for(List<CameraComponent> values : allEntitiesWithComponent.values()){
                for(CameraComponent value : values){
                    value.setMainOnStartup(false);
                }
            }
            component.setMainOnStartup(true);
        }
        ImGui.sameLine();
        ImGui.checkbox("Main", component.isMainOnStartup());
        
        Vector3f cameraPos = component.getCameraPos();
        ImFloat x = new ImFloat(cameraPos.x);
        ImFloat y = new ImFloat(cameraPos.y);
        boolean xb = ImGui.inputFloat("X", x);
        boolean yb = ImGui.inputFloat("Y", y);
        if(xb || yb){
            component.setCameraPos(x.get(), y.get(), 0.0f);
        }
    }
    
    @Override
    public boolean canHaveMultiple(){
        return false;
    }
}
