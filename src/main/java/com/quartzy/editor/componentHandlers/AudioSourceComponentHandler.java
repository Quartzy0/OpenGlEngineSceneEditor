package com.quartzy.editor.componentHandlers;

import com.quartzy.engine.ecs.components.AudioSourceComponent;
import imgui.ImGui;

public class AudioSourceComponentHandler extends ComponentHandler<AudioSourceComponent>{
    @Override
    public void draw(AudioSourceComponent component){
        ImGui.text("Audio Source Component. Use component.play(Sound) in a behaviour script to use.");
        ImGui.text("Note: One AudioSourceComponent can play any sound passed in, in the method");
    }
    
    @Override
    public boolean canHaveMultiple(){
        return false;
    }
}
