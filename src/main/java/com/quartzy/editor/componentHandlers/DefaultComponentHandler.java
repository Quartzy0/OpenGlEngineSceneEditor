package com.quartzy.editor.componentHandlers;

import com.quartzy.engine.ecs.Component;
import imgui.ImGui;

public class DefaultComponentHandler extends ComponentHandler{
    @Override
    public void draw(Component component){
        ImGui.text(component.getClass().getSimpleName());
    }
    
    @Override
    public boolean canHaveMultiple(){
        return false;
    }
}
