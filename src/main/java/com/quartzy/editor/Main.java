package com.quartzy.editor;

import com.quartzy.engine.ApplicationClient;
import com.quartzy.engine.Client;
import com.quartzy.engine.ecs.ECSManager;
import com.quartzy.engine.ecs.components.CameraComponent;
import com.quartzy.engine.ecs.components.CustomRenderComponent;
import com.quartzy.engine.graphics.Framebuffer;
import com.quartzy.engine.graphics.Texture;
import com.quartzy.engine.graphics.Window;
import com.quartzy.engine.utils.Logger;
import com.quartzy.engine.utils.SystemInfo;
import com.quartzy.engine.world.World;
import lombok.CustomLog;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;

@CustomLog
public class Main implements ApplicationClient{
    
    private Framebuffer framebuffer;
    
    @Override
    public Client preInit(String[] strings, SystemInfo systemInfo){
        this.framebuffer = new Framebuffer((int) systemInfo.getMainScreen().x, (int) systemInfo.getMainScreen().y, false);
        
        Client client = new Client.ClientBuilder()
                .setWindowTitle("Scene editor")
                .setFramebuffer(this.framebuffer)
                .setWindowHeight(1080)
                .setWindowWidth(1920)
                .build(this);
        
        return client;
    }
    
    @Override
    public void init(Client client){
        this.framebuffer.init();
        Handler handler = Handler.getInstance();
        handler.defaultTexture = getDefaultTexture();
        log.setOpenGlLogLevel(GL43.GL_DEBUG_SEVERITY_LOW);
        World world = new World("Tester_man");
//        client.getResourceManager().addResource("textures/tiles/stone.png");
//        client.getResourceManager().addResource("textures/particles/flame.png");
        
        client.getLayerStack().popLayer();
        client.getLayerStack().pushLayer(new CustomWorldLayer());
        client.getLayerStack().pushOverlay(new ImGuiLayer(new ImGuiRenderer()));
    
        ECSManager ecsManager = world.getEcsManager();
        short editorCamera = ecsManager.createObject("EditorCamera");
        CameraComponent component = new CameraComponent(true);
        ecsManager.addComponentToEntity(editorCamera, component);
        handler.editorCameraEntity = editorCamera;
        handler.editorCamera = component;
    
        World.setCurrentWorld(world);
    }
    
    @Override
    public void preStart(Client client){
    
    }
    
    @Override
    public void dispose(Client client){
    
    }
    
    public Texture getDefaultTexture(){
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.MAGENTA);
        g.fillRect(0, 0, 16, 16);
        g.setColor(Color.BLACK);
        g.fillRect(16, 0, 16, 16);
        g.setColor(Color.MAGENTA);
        g.fillRect(16, 16, 16, 16);
        g.setColor(Color.BLACK);
        g.fillRect(0, 16, 16, 16);
        
        /* Flip image Horizontal to get the origin to bottom left */
        AffineTransform transform = AffineTransform.getScaleInstance(1f, -1f);
        transform.translate(0, -image.getHeight());
        AffineTransformOp operation = new AffineTransformOp(transform,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        image = operation.filter(image, null);
    
        /* Get charWidth and charHeight of image */
        int width = image.getWidth();
        int height = image.getHeight();
    
        /* Get pixel data of image */
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
    
        /* Put pixel data into a ByteBuffer */
        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                /* Pixel as RGBA: 0xAARRGGBB */
                int pixel = pixels[i * width + j];
                /* Red component 0xAARRGGBB >> 16 = 0x0000AARR */
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                /* Green component 0xAARRGGBB >> 8 = 0x00AARRGG */
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                /* Blue component 0xAARRGGBB >> 0 = 0xAARRGGBB */
                buffer.put((byte) (pixel & 0xFF));
                /* Alpha component 0xAARRGGBB >> 24 = 0x000000AA */
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        /* Do not forget to flip the buffer! */
        buffer.flip();
    
        Texture texture = new Texture(buffer, width, height);
        MemoryUtil.memFree(buffer);
        
        return texture;
    }
}
