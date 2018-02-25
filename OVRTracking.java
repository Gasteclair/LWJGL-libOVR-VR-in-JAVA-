package net.input;

import org.lwjgl.ovr.OVR;
import org.lwjgl.ovr.OVRPoseStatef;
import org.lwjgl.ovr.OVRPosef;
import org.lwjgl.ovr.OVRQuatf;
import org.lwjgl.ovr.OVRTrackingState;
import org.lwjgl.ovr.OVRVector3f;

import net.graphics.OVRRenderer;

public class OVRTracking {
	
	private static OVRTrackingState trackingState;
	private static OVRQuatf headQuat;
	private static OVRVector3f headPos;

	public static void create() {
		trackingState = OVRTrackingState.malloc();
	}
	
	public static void update() {
		OVR.ovr_GetTrackingState(OVRRenderer.getSession().get(0), OVR.ovr_GetTimeInSeconds(), true, trackingState);
		OVRPoseStatef poseState = trackingState.HeadPose();
		OVRPosef pose = poseState.ThePose();
		headQuat = pose.Orientation();
		headPos = pose.Position();
	}

	public static OVRQuatf getHeadQuat() {
		return headQuat;
	}

	public static OVRVector3f getHeadPos() {
		return headPos;
	}
	
}
