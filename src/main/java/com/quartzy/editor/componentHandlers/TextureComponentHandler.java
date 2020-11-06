package com.quartzy.editor.componentHandlers;

import com.quartzy.engine.Client;
import com.quartzy.engine.ecs.components.TextureComponent;
import com.quartzy.engine.graphics.Color;
import com.quartzy.engine.graphics.Texture;
import com.quartzy.engine.math.Vector2f;
import com.quartzy.engine.utils.Resource;
import com.quartzy.engine.utils.ResourceManager;
import imgui.ImGui;
import imgui.type.ImFloat;
import imgui.type.ImString;

public class TextureComponentHandler extends ComponentHandler<TextureComponent>{
    private ImString text;
    
    @Override
    public void draw(TextureComponent component){
        Texture texture = component.getTexture();
        ImGui.image(texture.getId(), texture.getWidth(), texture.getHeight(), 0, 1, 1, 0);
        if(text==null){
            Resource resource = texture.getResource();
            text = new ImString(resource==null ? "No resource" : resource.getRelativePath());
            if(text.getData().length<128){
                text.resize(128);
            }
        }
        ImGui.inputText("Image path", text);
        if(ImGui.button("Save texture")){
            ResourceManager resourceManager = Client.getInstance().getResourceManager();
            Resource resource = resourceManager.addResource(text.get());
            if(resource!=null){
                Texture texture1 = resourceManager.getTextureManager().getTexture(resource.getName());
                if(texture1!=null)
                    component.setTexture(texture1);
            }
        }
    
        float[] floats1 = {component.getOffset().x};
        float[] floats2 = {component.getOffset().y};
        ImGui.dragFloat("X", floats1);
        ImGui.dragFloat("Y", floats2);
        component.setOffset(new Vector2f(floats1[0], floats2[0]));
        
        float[] colors = new float[]{component.getColor().getRed(), component.getColor().getGreen(), component.getColor().getBlue(), component.getColor().getAlpha()};
        ImGui.colorEdit4("Texture color", colors);
        component.setColor(new Color(colors[0], colors[1], colors[2], colors[3]));
    }
    
    @Override
    public boolean canHaveMultiple(){
        return true;
    }
}
