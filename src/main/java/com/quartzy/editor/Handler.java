package com.quartzy.editor;

import com.quartzy.engine.ecs.components.CameraComponent;
import com.quartzy.engine.graphics.Texture;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@CustomLog
public class Handler{
    
    private static Handler INSTANCE;
    
    public File currentlyOpenedFile;
    public Texture defaultTexture;
    public short editorCameraEntity;
    public CameraComponent editorCamera;
    
    
    public static Handler getInstance(){
        return INSTANCE==null ? (INSTANCE = new Handler()) : INSTANCE;
    }
}
