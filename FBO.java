package net.graphics;


import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.ovr.OVRSizei;

public class FBO {

    private int[] id;
    private OVRSizei size;
    private int[] textureId;
    private int[] depthId;

    public FBO(OVRSizei size, int textureId2) {

        this.size = size;

        id = new int[1];
        id[0]= GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id[0]);
        {
            textureId = new int[1];
            textureId[0] = textureId2;

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId[0]);
            {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
                
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, size.w(), size.h(), 0, GL11.GL_RGBA, GL11.GL_FLOAT, 0);

                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureId[0], 0);
            }
            depthId = new int[1];
            depthId[0] = GL11.glGenTextures();

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthId[0]);
            {
            	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

            	GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, size.w(), size.h(), 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, 0);

                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureId[0], 0);
            }
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,  GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthId[0], 0);

            if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {

                System.out.println("FrameBuffer incomplete!");
            }
        }
        
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

    }

    public int getTextureId() {
        return textureId[0];
    }

    public int getId() {
        return id[0];
    }

    public OVRSizei getSize() {
        return size;
    }
}
