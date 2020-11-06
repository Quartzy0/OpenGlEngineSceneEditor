package com.quartzy.editor.componentHandlers;

import com.quartzy.editor.Handler;
import com.quartzy.editor.Main;
import com.quartzy.editor.componentHandlers.defaults.DefaultBehaviour;
import com.quartzy.engine.ecs.Component;
import com.quartzy.engine.ecs.components.*;
import lombok.CustomLog;
import org.dyn4j.geometry.Transform;

import java.util.ArrayList;
import java.util.HashMap;

@CustomLog
public abstract class ComponentHandler<T extends Component>{
    private static HashMap<Class<? extends Component>, Class<? extends ComponentHandler>> componentHandlers = new HashMap<>();
    private static final DefaultComponentHandler DEFAULT_COMPONENT_HANDLER;
    
    private static HashMap<Integer, ComponentHandler> componentHandlerObjs = new HashMap<>();
    
    static {
        DEFAULT_COMPONENT_HANDLER = new DefaultComponentHandler();
        componentHandlers.put(TransformComponent.class, TransformComponentHandler.class);
        componentHandlers.put(TextureComponent.class, TextureComponentHandler.class);
        componentHandlers.put(CameraComponent.class, CameraComponentHandler.class);
        componentHandlers.put(LightSourceComponent.class, LightSourceComponentHandler.class);
        componentHandlers.put(AudioListenerComponent.class, AudioListenerComponentHandler.class);
        componentHandlers.put(BehaviourComponent.class, BehaviourComponentHandler.class);
    }
    
    public static ComponentHandler getHandler(Component component, int index){
        if(componentHandlerObjs.containsKey(component.hashCode() * 31 + component.getEntityId() + index))return componentHandlerObjs.get(component.hashCode() * 31 + component.getEntityId() + index);
        ComponentHandler componentHandler = null;
        try{
            Class<? extends ComponentHandler> aClass = componentHandlers.get(component.getClass());
            if(aClass==null)return DEFAULT_COMPONENT_HANDLER;
            componentHandler = aClass.newInstance();
            componentHandlerObjs.put(component.hashCode() * 31 + component.getEntityId() + index, componentHandler);
        } catch(InstantiationException | IllegalAccessException e){
            e.printStackTrace();
        }
        return componentHandler;
    }
    
    public static ArrayList<Class<? extends Component>> getSupportedComponents(){
        ArrayList<Class<? extends Component>> componentList = new ArrayList<>();
        for(Class<? extends Component> aClass : componentHandlers.keySet()){
            componentList.add(aClass);
        }
        return componentList;
    }
    
    public static String classToFriendlyName(Class clazz){
        String unfriendlyName = clazz.getSimpleName();
        StringBuilder friendlyName = new StringBuilder();
        for(int i = 0; i < unfriendlyName.length(); i++){
            char ch = unfriendlyName.charAt(i);
            if(i==0){
                friendlyName.append(Character.toUpperCase(ch));
                continue;
            }
            if(Character.isUpperCase(ch)){
                friendlyName.append(" ").append(ch);
                continue;
            }
            friendlyName.append(ch);
        }
        return friendlyName.toString();
    }
    
    public static <T extends Component> T createDefComponent(Class<T> component){
        if(TransformComponent.class.isAssignableFrom(component)){
            return (T) new TransformComponent(new Transform());
        }
        if(TextureComponent.class.isAssignableFrom(component)){
            return (T) new TextureComponent(Handler.getInstance().defaultTexture);
        }
        if(CameraComponent.class.isAssignableFrom(component)){
            return (T) new CameraComponent();
        }
        if(LightSourceComponent.class.isAssignableFrom(component)){
            return (T) new LightSourceComponent();
        }
        if(AudioListenerComponent.class.isAssignableFrom(component)){
            return (T) new AudioListenerComponent();
        }
        if(BehaviourComponent.class.isAssignableFrom(component)){
            return (T) new BehaviourComponent(DefaultBehaviour.class);
        }
        return null;
    }
    
    public static boolean canHaveMultiple(Class<? extends Component> clazz){
        Class<? extends ComponentHandler> aClass = componentHandlers.get(clazz);
        if(aClass!=null){
            try{
                ComponentHandler componentHandler = aClass.newInstance();
                return componentHandler.canHaveMultiple();
            } catch(InstantiationException | IllegalAccessException e){
                log.warning("Couldn't create new instance of class %s", e, aClass.getName());
            }
        }
        return true;
    }
    
    public abstract void draw(T component);
    
    public abstract boolean canHaveMultiple();
}
