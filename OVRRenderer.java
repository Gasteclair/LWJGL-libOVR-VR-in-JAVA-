package net.graphics;

import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.ovr.OVR;
import org.lwjgl.ovr.OVREyeRenderDesc;
import org.lwjgl.ovr.OVRFovPort;
import org.lwjgl.ovr.OVRGL;
import org.lwjgl.ovr.OVRGraphicsLuid;
import org.lwjgl.ovr.OVRHmdDesc;
import org.lwjgl.ovr.OVRLayerEyeFov;
import org.lwjgl.ovr.OVRPosef;
import org.lwjgl.ovr.OVRQuatf;
import org.lwjgl.ovr.OVRRecti;
import org.lwjgl.ovr.OVRSizei;
import org.lwjgl.ovr.OVRTextureSwapChainDesc;
import org.lwjgl.ovr.OVRVector2i;

import net.input.OVRTracking;

public class OVRRenderer {
	
	private static PointerBuffer session;
	private static OVRGraphicsLuid luid;
	private static OVRFovPort fovPort;
	private static OVRSizei bufferSize;
	private static PointerBuffer textureSwapChain;
	private static FBO vrFBO;
	
	private static OVREyeRenderDesc eyeRenderDesc0;
	private static OVREyeRenderDesc eyeRenderDesc1;
	
	private static OVRPosef hmdToEyeViewPose0;
	private static OVRPosef hmdToEyeViewPose1;
	
	private static OVRHmdDesc hmdDesc;
	
	private static OVRLayerEyeFov layer;

	public static void create() {
		OVR.ovr_Initialize(null);
		session = BufferUtils.createPointerBuffer(1);
		luid = OVRGraphicsLuid.create();
		
		if(OVR.ovr_Create(session, luid)!=0) {
			System.err.println("Couldn't create OVR!");
			System.exit(-1);
		}
		
		
		
		
		//INIT RENDERING
		fovPort = OVRFovPort.malloc();
		fovPort.set(Settings.OVR_FOVS[0], Settings.OVR_FOVS[1], Settings.OVR_FOVS[2], Settings.OVR_FOVS[3]);
		
		OVRSizei recommenedTex0Size = OVRSizei.malloc();
		OVR.ovr_GetFovTextureSize(session.get(0), OVR.ovrEye_Left, fovPort, 1.0f, recommenedTex0Size);
		
		OVRSizei recommenedTex1Size = OVRSizei.malloc();
		OVR.ovr_GetFovTextureSize(session.get(0), OVR.ovrEye_Right,fovPort, 1.0f, recommenedTex1Size);
		
		bufferSize = OVRSizei.malloc();
		int bufferSizeW = recommenedTex0Size.w() + recommenedTex1Size.w();
		int bufferSizeH = Math.max(recommenedTex0Size.h(), recommenedTex1Size.h());
		bufferSize.set(bufferSizeW, bufferSizeH);
		
		textureSwapChain = PointerBuffer.allocateDirect(1);
		
		OVRTextureSwapChainDesc desc = OVRTextureSwapChainDesc.calloc();
		desc.set(OVR.ovrTexture_2D, OVR.OVR_FORMAT_R8G8B8A8_UNORM_SRGB, 1, bufferSize.w(), bufferSize.h(), 1, 1, true, desc.MiscFlags(), desc.BindFlags());
		OVRGL.ovr_CreateTextureSwapChainGL(session.get(0), desc, textureSwapChain);	 
		
		
		
		IntBuffer chainTexId = BufferUtils.createIntBuffer(1); 
		OVRGL.ovr_GetTextureSwapChainBufferGL(session.get(0), textureSwapChain.get(0), 0, chainTexId);
		vrFBO = new FBO(bufferSize, chainTexId.get(0));
		
		// Initialize VR structures, filling out description.
		
		eyeRenderDesc0 = OVREyeRenderDesc.malloc();
		eyeRenderDesc1 = OVREyeRenderDesc.malloc();
		
		hmdToEyeViewPose0 = OVRPosef.malloc();
		hmdToEyeViewPose1 = OVRPosef.malloc();
		
		hmdDesc = OVRHmdDesc.malloc();
		OVR.ovr_GetHmdDesc(session.get(0), hmdDesc);
		
		OVR.ovr_GetRenderDesc(session.get(0), OVR.ovrEye_Left, hmdDesc.DefaultEyeFov(0), eyeRenderDesc0);
		OVR.ovr_GetRenderDesc(session.get(0), OVR.ovrEye_Right, hmdDesc.DefaultEyeFov(1), eyeRenderDesc1);
		
		hmdToEyeViewPose0 = eyeRenderDesc0.HmdToEyePose();
		hmdToEyeViewPose1 = eyeRenderDesc1.HmdToEyePose();

		// Initialize our single full screen Fov layer.
		layer = OVRLayerEyeFov.malloc();
		
		layer.Header().Type(OVR.ovrLayerType_EyeFov);
		layer.Header().Flags(0);
		layer.ColorTexture(0, textureSwapChain.get(0));
		layer.ColorTexture(1, textureSwapChain.get(0));
		layer.Fov(0, eyeRenderDesc0.Fov());
		layer.Fov(1, eyeRenderDesc1.Fov());	
		layer.Viewport(0, createRecti(0, 0, bufferSize.w() / 2, bufferSize.h()));
		layer.Viewport(1, createRecti(bufferSize.w() / 2, 0, bufferSize.w() / 2, bufferSize.h()));
		layer.RenderPose(0, hmdToEyeViewPose0);		
		layer.RenderPose(1, hmdToEyeViewPose1);	
	}
	
	public static void prepareVRRendering() {
		OVR.ovr_WaitToBeginFrame(session.get(0), 0);
		OVR.ovr_BeginFrame(session.get(0), 0);
		updateRenderPose();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, vrFBO.getId());
		
	}
	
	public static void viewPortEye(int eye) {
		if(eye==0||eye==1)GL11.glViewport(layer.Viewport(eye).Pos().x(), layer.Viewport(eye).Pos().y(), layer.Viewport(eye).Size().w(), layer.Viewport(eye).Size().h());
		else GL11.glViewport(0, 0, Settings.DISPLAY_WIDTH, Settings.DISPLAY_HEIGHT);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	    GL11.glLoadIdentity();
		if(eye==0)GL11.glTranslatef(Settings.BETWEEN_EYES_DISTANCE/2f,0.0f,0.0f);
		else if(eye==1)GL11.glTranslatef(-Settings.BETWEEN_EYES_DISTANCE/2f,0.0f,0.0f);
		else GL11.glTranslatef(0.0f,0.0f,0.0f);
	}
	
	public static void finishVRRendering() {
		OVR.ovr_CommitTextureSwapChain(session.get(0), textureSwapChain.get(0));
		
		PointerBuffer layerPtrList = BufferUtils.createPointerBuffer(1);
		layerPtrList.put(layer.address());
		layerPtrList.flip();
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		
		OVR.ovr_EndFrame(session.get(0), 0, null, layerPtrList);
	}
	
	public static void destroy() {
		OVR.ovr_DestroyTextureSwapChain(session.get(0), textureSwapChain.get(0));
		OVR.ovr_Destroy(session.get(0));
		OVR.ovr_Shutdown();
	}
	
	private static void updateRenderPose() {
		OVRQuatf headQuat = OVRTracking.getHeadQuat();
		OVRQuatf quat0 = hmdToEyeViewPose0.Orientation();
    	quat0.w(headQuat.w());
    	quat0.x(headQuat.x());
    	quat0.y(headQuat.y());
    	quat0.z(headQuat.z());
    	hmdToEyeViewPose0.Orientation(quat0);
    	
    	
    	
	    layer.RenderPose(0, hmdToEyeViewPose0);	
	    
	    
	    OVRQuatf quat1 = hmdToEyeViewPose1.Orientation();
    	quat1.w(headQuat.w());
    	quat1.x(headQuat.x());
    	quat1.y(headQuat.y());
    	quat1.z(headQuat.z());
    	hmdToEyeViewPose1.Orientation(quat1);
    	
	    layer.RenderPose(1, hmdToEyeViewPose1);
	}
	
	private static OVRRecti createRecti(int x, int y, int w, int h) {
		OVRVector2i pos = OVRVector2i.malloc();
		pos.set(x, y);
		OVRSizei size = OVRSizei.malloc();
		size.set(w, h);
		
		OVRRecti recti = OVRRecti.malloc();
		recti.set(pos, size);
		return recti;
	}

	public static PointerBuffer getSession() {
		return session;
	}
	
	
	
}
