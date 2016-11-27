package com.yuyashuai.PreviewFaceDetectionDemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;
import jp.co.cyberagent.android.gpuimage.OpenGlUtils;

/**
 * Created by yuyashuai on 2016/10/26 0026.
 */

public class MyRenderer implements GLSurfaceView.Renderer ,Camera.PreviewCallback{
    // Our matrices
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];
    private float[] standardMatrix=new float[16];
    private float[] transMatrix=new float[16];//移动Matrix
    private float[] scaleMatrix=new float[16];//缩放Matrix
    private float[] rotateMatrix=new float[16];//旋转Matrix
    //private float[] transMatrix=new float[16];
    private float[] transMatrixTest=new float[16];

    // Geometric variables
    public static float vertices[];
    public static short indices[];
    public static float uvs[];
    public FloatBuffer vertexBuffer;
    public ShortBuffer drawListBuffer;
    public FloatBuffer uvBuffer;

    private boolean bmp2IsNull=true;
    private boolean haveFace=false;
    float	mOutputWidth = 1080;
    float	mOutputHeight = 1920;

    Context mContext;
    long mLastTime;
    int mProgram;
    private int[] texturenames;
    private Bitmap bmp2;
    private float[] vertices2;
    private FloatBuffer vertexBuffer2;
    private int vertexShader;
    private int fragmentShader;

    private IntBuffer mRgbBuffer;
    private int mTextureId=-1;
    private final Queue<Runnable> mRunOnDraw;
    private Camera mCamera;
    private int mCamId = Camera.getNumberOfCameras() - 1;
    private float[] uvs1;
    private FloatBuffer uvBuffer1;
    private float testTransX=0.0f;
    private float testTransY=0.0f;

    public MyRenderer(Context c)
    {
        mContext = c;
        mLastTime = System.currentTimeMillis() + 100;
        mRunOnDraw=new LinkedList<Runnable>();
    }

    public void onPause()
    {

    }

    public void onResume()
    {

        mLastTime = System.currentTimeMillis();
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        long now = System.currentTimeMillis();


        if (mLastTime > now) return;


        long elapsed = now - mLastTime;


        runAll(mRunOnDraw);

        Render(mtrxProjectionAndView);


        mLastTime = now;

    }
    double angle=0;
    float testI=0.0f;
    //大概6-8ms
    private void Render(float[] m) {


        GLES20.glUseProgram(riGraphicTools.sp_Image);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        int mPositionHandle = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandle);


        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "a_texCoord" );

        GLES20.glEnableVertexAttribArray ( mTexCoordLoc );

        GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, uvBuffer);

        int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "uMVPMatrix");

        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);

        int mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image, "s_texture" );

        if (mTextureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
            GLES20.glUniform1i(mSamplerLoc, 0);
        }


        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
        if(!haveFace||bmp2IsNull)
        {

            //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            return;
        }

        angle=Math.PI/2;//90°，π/2。
        rotateMatrix[0]= (float) Math.cos(angle);
        rotateMatrix[1]= (float) Math.sin(angle);
        rotateMatrix[4]= -(float) Math.sin(angle);
        rotateMatrix[5]= (float) Math.cos(angle);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE,GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glUseProgram(riGraphicTools.sp_Face);

        int mPositionHandleFace = GLES20.glGetAttribLocation(riGraphicTools.sp_Face, "vPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandleFace);
        GLES20.glVertexAttribPointer(mPositionHandleFace, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        int mTexCoordLocFace = GLES20.glGetAttribLocation(riGraphicTools.sp_Face, "a_texCoord" );


        GLES20.glEnableVertexAttribArray ( mTexCoordLocFace );


        GLES20.glVertexAttribPointer ( mTexCoordLocFace, 2, GLES20.GL_FLOAT,
                false,
                0, uvBuffer);


        int mtrxhandleFace = GLES20.glGetUniformLocation(riGraphicTools.sp_Face, "uMVPMatrix");

        int mTransHandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Face, "transMatrix");

        int mScaleHandle=GLES20.glGetUniformLocation(riGraphicTools.sp_Face,"scaleMatrix");

        int mRotateHandle=GLES20.glGetUniformLocation(riGraphicTools.sp_Face,"rotateMatrix");

        GLES20.glUniformMatrix4fv(mtrxhandleFace, 1, false, m, 0);


        GLES20.glUniformMatrix4fv(mTransHandle,1,false,transMatrix,0);
        GLES20.glUniformMatrix4fv(mScaleHandle,1,false,scaleMatrix,0);
        GLES20.glUniformMatrix4fv(mRotateHandle,1,false,rotateMatrix,0);

        int mSamplerLocFace = GLES20.glGetUniformLocation (riGraphicTools.sp_Face, "s_texture1" );


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texturenames[1]);


        GLES20.glUniform1i ( mSamplerLocFace, 1);


        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandleFace);
        GLES20.glDisableVertexAttribArray(mTexCoordLocFace);


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        // We need to know the current width and height.
        mOutputWidth = width;
        mOutputHeight = height;



        GLES20.glViewport(0, 0, (int)mOutputWidth, (int)mOutputHeight);


        for(int i=0;i<16;i++)
        {
            mtrxProjection[i] = 0.0f;
            mtrxView[i] = 0.0f;
            mtrxProjectionAndView[i] = 0.0f;
            transMatrix[i]=0.0f;
            if(i!=0&&i!=5&&i!=10&&i!=15)
            {
                standardMatrix[i]=0.0f;
                scaleMatrix[i]=0.0f;
                rotateMatrix[i]=0.0f;
            }
            else
            {
                scaleMatrix[i]=1.0f;
                standardMatrix[i]=1.0f;
                rotateMatrix[i]=1.0f;
            }
        }



        Matrix.orthoM(mtrxProjection, 0, 0f, 1f, 0.0f, 1f, 0, 1);



        Matrix.setLookAtM(mtrxView, 0,
                0f, 0f, 0f,
                0f, 0f, -1f,
                0f, 1f, 0f);
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);



    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        startCamera();


        SetupTriangle();

        SetupImage(null);


        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);


        vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_SolidColor);
        fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_SolidColor);

        riGraphicTools.sp_SolidColor = GLES20.glCreateProgram();
        GLES20.glAttachShader(riGraphicTools.sp_SolidColor, vertexShader);
        GLES20.glAttachShader(riGraphicTools.sp_SolidColor, fragmentShader);
        GLES20.glLinkProgram(riGraphicTools.sp_SolidColor);

        vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_Image);
        //修改不同的shader
        fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_Image);

        riGraphicTools.sp_Image = GLES20.glCreateProgram();
        GLES20.glAttachShader(riGraphicTools.sp_Image, vertexShader);
        GLES20.glAttachShader(riGraphicTools.sp_Image, fragmentShader);
        GLES20.glLinkProgram(riGraphicTools.sp_Image);


        int vertexShaderFace = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_Face);
        int fragmentShaderFace = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_Face);
        riGraphicTools.sp_Face=GLES20.glCreateProgram();
        GLES20.glAttachShader(riGraphicTools.sp_Face,vertexShaderFace);
        GLES20.glAttachShader(riGraphicTools.sp_Face,fragmentShaderFace);

        GLES20.glLinkProgram(riGraphicTools.sp_Face);

    }

    /**
     * @param bitmap
     */
    public void SetupImage(Bitmap bitmap)
    {
        uvs=new float[]{
                1.0f, 0.0f,
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        };


        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

        texturenames = new int[2];
        GLES20.glGenTextures(2, texturenames, 0);

        int id2 = mContext.getResources().getIdentifier("drawable/test3", null, mContext.getPackageName());

        long time2=System.currentTimeMillis();

        if(bmp2==null)

        {
            bmp2 = BitmapFactory.decodeResource(mContext.getResources(), id2);
        }


        long time3=System.currentTimeMillis();
        System.out.println("decodeResource用时"+(time3-time2)+"毫秒");
        if(bmp2!=null)
        {
            bmp2IsNull=false;
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);//-------------------------激活纹理2
            //贴图纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[1]);//-----------------------------------------------------------
            // Set filtering，设置第二层纹理属性
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp2, 0);//-------------------------------------------------------------
        }
        else
        {
            bmp2IsNull=true;
        }

    }

    public void SetupTriangle()
    {
        vertices = new float[]
                {0f, 1f, 0.0f,
                        0f, 0f, 0.0f,
                        1f, 0f, 0.0f,
                        1f, 1f, 0.0f,
                };


        indices = new short[] {0, 1, 2, 0, 2, 3};

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 4);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);

    }

    /**
     * @param transMatrix
     * @param scaleMatrix
     * @param rotateMatrix
     */
    public void setFaceData(float[] transMatrix,float[] scaleMatrix,float[] rotateMatrix )
    {
        this.transMatrix=transMatrix;
        this.scaleMatrix=scaleMatrix;
        this.rotateMatrix=rotateMatrix;
    }


    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {

        final Camera.Size previewSize=camera.getParameters().getPreviewSize();
        if (mRgbBuffer == null) {
            mRgbBuffer = IntBuffer.allocate(previewSize.width *previewSize.height);
        }
        if(mRunOnDraw.isEmpty())
        {
            runOnDraw(new Runnable() {
                @Override
                public void run() {
                    GPUImageNativeLibrary.YUVtoRBGA(data,previewSize.width,previewSize.height,mRgbBuffer.array());
                    mTextureId = OpenGlUtils.loadTexture(mRgbBuffer, previewSize, mTextureId);
                    camera.addCallbackBuffer(data);
                }
            });
        }
    }

    public void startCamera()
    {
        if (mCamera != null) {
            return;
        }
        if (mCamId > (Camera.getNumberOfCameras() - 1) || mCamId < 0) {
            return;
        }


        mCamera = Camera.open(mCamId);
        Camera.Parameters parameters=mCamera.getParameters();
        parameters.setPictureSize(1280,720);
        parameters.setPreviewSize(1280,720);
        final int[] range = findClosestFpsRange(15, parameters.getSupportedPreviewFpsRange());
        parameters.setPreviewFpsRange(range[0], range[1]);
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        if (!parameters.getSupportedFocusModes().isEmpty()) {
            parameters.setFocusMode(parameters.getSupportedFocusModes().get(0));
        }
        for (int i=0;i<parameters.getSupportedPreviewSizes().size();i++)
        {
            System.out.println("高："+parameters.getSupportedPreviewSizes().get(i).height+"   宽："+parameters.getSupportedPreviewSizes().get(i).width);
            System.out.println("高："+parameters.getSupportedPictureSizes().get(i).height+"   宽："+parameters.getSupportedPictureSizes().get(i).width);

        }
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewCallback(this);
        mCamera.setPreviewCallbackWithBuffer(this);
        setUpSurfaceTexture(mCamera);

        mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
            @Override
            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                System.out.println("监测到人脸");
                if(faces!=null&&faces.length>=1)
                {
                    Camera.Face face=faces[0];
                    Rect rect=face.rect;
                    int centerXbefore=rect.centerX();
                    int centerYbefore=rect.centerY();
                    int leftBefore=rect.left;
                    int topBefore=rect.top;
                    int rightBefore=rect.right;
                    int bottomBefore=rect.bottom;
                    Point centerPointBefore=new Point(centerXbefore,centerYbefore);//矩形中心点
                    Point leftTopPointBefore=new Point(leftBefore,topBefore);//矩形左上角
                    Point rightBottomPointBefore=new Point(rightBefore,bottomBefore);
                    Point centerPointAfter=transCoordinate(centerPointBefore);
                    Point leftTopPointAfter=transCoordinate(leftTopPointBefore);//左上角坐标
                    Point rightBottomPointAfter=transCoordinate(rightBottomPointBefore);//右下角坐标

                    float transY=centerPointAfter.y/(float)1280;
                    float transX=centerPointAfter.x/(float)720;
                    transMatrix[12]=-transY+0.5f;
                    transMatrix[13]=transX+0.5f;//左边转换
                    float scaleY=Math.abs(leftTopPointAfter.y-rightBottomPointAfter.y)/(float)1280;
                    float scaleX=Math.abs(rightBottomPointAfter.x-leftTopPointAfter.y)/(float)720;
                    scaleMatrix[0]=scaleY;
                    scaleMatrix[5]=scaleY+0.3f;
                    setFaceData(transMatrix,scaleMatrix,rotateMatrix);
                    haveFace=true;
                }
                else
                {
                    haveFace=false;
                }


            }
        });
        mCamera.startFaceDetection();
    }

    /**
     * 转换为标准的笛卡尔坐标系
     * @param srcPoint
     * @return
     */
    private Point transCoordinate(Point srcPoint)
    {

        int x=srcPoint.y;
        int y=srcPoint.x;
        Point point=new Point(x,y);
        return point;
    }

    private static int[] findClosestFpsRange(int expectedFps, List<int[]> fpsRanges) {
        expectedFps *= 1000;
        int[] closestRange = fpsRanges.get(0);
        int measure = Math.abs(closestRange[0] - expectedFps) + Math.abs(closestRange[1] - expectedFps);
        for (int[] range : fpsRanges) {
            if (range[0] <= expectedFps && range[1] >= expectedFps) {
                int curMeasure = Math.abs(range[0] - expectedFps) + Math.abs(range[1] - expectedFps);
                if (curMeasure < measure) {
                    closestRange = range;
                    measure = curMeasure;
                }
            }
        }
        return closestRange;
    }
    SurfaceTexture mSurfaceTexture;
    public void setUpSurfaceTexture(final Camera camera) {

                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                mSurfaceTexture = new SurfaceTexture(textures[0]);
                try {
                    camera.setPreviewTexture(mSurfaceTexture);
                    camera.setPreviewCallback(this);
                    camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(runnable);
        }
    }
    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }


    public void stopCamera()
    {

        mCamera.setPreviewCallbackWithBuffer(null);
        try {
            mCamera.setPreviewTexture(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.stopFaceDetection();
        mCamera.stopPreview();

    }

}
