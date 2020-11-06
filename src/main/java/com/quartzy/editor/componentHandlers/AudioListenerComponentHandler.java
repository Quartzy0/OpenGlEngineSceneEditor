package com.quartzy.editor.componentHandlers;

import com.quartzy.engine.ecs.components.AudioListenerComponent;
import com.quartzy.engine.world.World;
import imgui.ImGui;

import java.util.HashMap;
import java.util.List;

public class AudioListenerComponentHandler extends ComponentHandler<AudioListenerComponent>{
    @Override
    public void draw(AudioListenerComponent component){
        if(ImGui.button("Set as main")){
            HashMap<Short, List<AudioListenerComponent>> allEntitiesWithComponent = World.getCurrentWorld().getEcsManager().getAllEntitiesWithComponent(AudioListenerComponent.class);
            for(List<AudioListenerComponent> value : allEntitiesWithComponent.values()){
                for(AudioListenerComponent comp : value){
                    comp.setMainAtStartup(false);
                }
            }
            component.setAsMain();
            component.setMainAtStartup(true);
        }
        ImGui.sameLine();
        ImGui.checkbox("", component.isMainAtStartup());
    }
    
    @Override
    public boolean canHaveMultiple(){
        return false;
    }
}
