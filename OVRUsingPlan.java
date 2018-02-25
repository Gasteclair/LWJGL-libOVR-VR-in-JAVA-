
    //When starting the game
    public static void onStart() {
      //TODO --> Creating Display
      GL.createCapabilities();

      GL11.glEnable(GL30.GL_FRAMEBUFFER);
      GL11.glEnable(GL11.GL_TEXTURE_2D);
      GL11.glEnable(GL30.GL_RENDERBUFFER);
      GL11.glEnable(GL11.GL_CULL_FACE);
      GL11.glEnable(GL11.GL_DEPTH_TEST);
      GL11.glShadeModel(GL11.GL_SMOOTH);
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

      OVRRenderer.create();
      //TODO --> Loading Scene
    }
    
    //Call it each frames of the game as Rendering Func
    public static void onUpdate() {
      //TODO --> Updating Display
      OVRRenderer.prepareVRRendering();
      clear();
      for(int i=0;i<3;i++) {
        if(i>=2) {
          OVRRenderer.finishVRRendering();
          //put "break;" there if you don't want it to render on screen.
          clear();
        }
        /** OVRRenderer use GL11.glTranslatef() to make 3D **/
        OVRRenderer.viewPortEye(i);
        //TODO --> Rendering Scene
		  }
	  }
    
    //When closing the game
    public static void onDestroy() {
      //TODO --> CleanUp Scene
		  OVRRenderer.destroy();
      //TODO --> Closing Display
	  }
    
    private static void clear() {
		  GL11.glClearColor(Settings.CLEAR_COLOR.getRed(),Settings.CLEAR_COLOR.getGreen(),Settings.CLEAR_COLOR.getBlue(),Settings.CLEAR_COLOR.getAlpha());
	    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
	  }
