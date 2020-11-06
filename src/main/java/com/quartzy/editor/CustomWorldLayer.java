package com.quartzy.editor;

import com.quartzy.engine.graphics.Renderer;
import com.quartzy.engine.layers.Layer;
import com.quartzy.engine.network.NetworkManager;
import com.quartzy.engine.network.Side;
import com.quartzy.engine.world.World;

public class CustomWorldLayer extends Layer{
    private boolean first = true;
    
    @Override
    public void update(float v){
        World currentWorld = World.getCurrentWorld();
        if(first){
            currentWorld.getEcsManager().initComponents();
            first = false;
        }
    }
    
    @Override
    public void render(Renderer renderer){
        World currentWorld = World.getCurrentWorld();
        if(currentWorld!=null && NetworkManager.INSTANCE.getSide() == Side.CLIENT){
            currentWorld.render(renderer);
        }
    }
    
    @Override
    public void onDetach(){
    
    }
    
    @Override
    public void onAttach(){
    
    }
}
