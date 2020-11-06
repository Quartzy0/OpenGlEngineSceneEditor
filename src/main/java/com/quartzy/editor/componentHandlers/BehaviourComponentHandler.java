package com.quartzy.editor.componentHandlers;

import com.quartzy.editor.componentHandlers.defaults.DefaultBehaviour;
import com.quartzy.engine.ecs.components.Behaviour;
import com.quartzy.engine.ecs.components.BehaviourComponent;
import imgui.ImGui;
import imgui.type.ImString;

public class BehaviourComponentHandler extends ComponentHandler<BehaviourComponent>{
    private ImString text;
    private boolean foundClass = true;
    
    @Override
    public void draw(BehaviourComponent component){
        ImGui.text("Behaviour class (include full package path)");
    
        if(text==null){
            Behaviour behaviour = component.getBehaviour();
            if(DefaultBehaviour.class.isAssignableFrom(behaviour.getClass())){
                behaviour = null;
            }
            text = new ImString(behaviour==null ? "No class" : behaviour.getClass().getName());
            if(text.getData().length<128){
                text.resize(128);
            }
        }
        ImGui.inputText("Class", text);
        if(ImGui.button("Set as behaviour")){
            try{
                Class<?> aClass = Class.forName(text.get());
                if(Behaviour.class.isAssignableFrom(aClass)){
                    foundClass = true;
                    component.setBehaviour((Class<? extends Behaviour>) aClass);
                }else {
                    foundClass = false;
                }
            } catch(ClassNotFoundException e){
                foundClass = false;
            }
        }
        if(!foundClass){
            ImGui.textColored(0xFF6666FF, "Class not found or doesn't extend Behaviour class!");
        }
    }
    
    @Override
    public boolean canHaveMultiple(){
        return true;
    }
}
