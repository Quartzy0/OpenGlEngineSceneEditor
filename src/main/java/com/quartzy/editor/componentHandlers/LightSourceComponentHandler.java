package com.quartzy.editor.componentHandlers;

import com.quartzy.engine.ecs.components.LightSourceComponent;
import com.quartzy.engine.graphics.Color;
import com.quartzy.engine.math.Vector2f;
import imgui.ImGui;

public class LightSourceComponentHandler extends ComponentHandler<LightSourceComponent>{
    @Override
    public void draw(LightSourceComponent component){
        float[] colors = new float[]{component.getColor().getRed(), component.getColor().getGreen(), component.getColor().getBlue()};
        ImGui.colorEdit3("Light color", colors);
        component.setColor(new Color(colors[0], colors[1], colors[2]));
    
        float[] floats = {component.getZ()};
        ImGui.dragFloat("Z", floats);
        component.setZ(floats[0]);
    
        ImGui.text("Offset");
        float[] floats1 = {component.getOffset().x};
        float[] floats2 = {component.getOffset().y};
        ImGui.dragFloat("X", floats1);
        ImGui.dragFloat("Y", floats2);
        component.setOffset(new Vector2f(floats1[0], floats2[0]));
    }
    
    @Override
    public boolean canHaveMultiple(){
        return true;
    }
}
