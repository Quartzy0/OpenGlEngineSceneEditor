package com.quartzy.editor.componentHandlers;

import com.quartzy.engine.ecs.components.RigidBodyComponent;

public class RigidBodyComponentHandler extends ComponentHandler<RigidBodyComponent>{
    @Override
    public void draw(RigidBodyComponent component){
    
    }
    
    @Override
    public boolean canHaveMultiple(){
        return false;
    }
}
