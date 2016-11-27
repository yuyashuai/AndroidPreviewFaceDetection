package com.yuyashuai.PreviewFaceDetectionDemo;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by 于亚帅 on 2016/10/26 0026.
 */

public class MyGLSurfaceView extends GLSurfaceView {
    private final MyRenderer mRenderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyRenderer(context);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
    @Override
    public void onPause() {
        super.onPause();
        mRenderer.onPause();
        mRenderer.stopCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRenderer.onResume();

    }



}
