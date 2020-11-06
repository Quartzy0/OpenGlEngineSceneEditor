package com.quartzy.editor.componentHandlers;

import com.quartzy.engine.ecs.components.TransformComponent;
import imgui.ImGui;

public class TransformComponentHandler extends ComponentHandler<TransformComponent>{
    @Override
    public void draw(TransformComponent component){
        float[] float1 = {(float) component.getX()};
        float[] float2 = {(float) component.getY()};
        ImGui.dragFloat("X", float1);
        ImGui.dragFloat("Y", float2);
        component.setPosition(float1[0], float2[0]);
    }
    
    @Override
    public boolean canHaveMultiple(){
        return false;
    }
}
