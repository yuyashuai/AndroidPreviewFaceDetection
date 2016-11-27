package com.yuyashuai.PreviewFaceDetectionDemo;

import android.opengl.GLES20;

public class riGraphicTools {

    // Program variables
    public static int sp_SolidColor;
    public static int sp_Image;
    public static int sp_Face;


    /* SHADER Solid
     *
     * This shader is for rendering a colored primitive.
     *
     */
    public static final String vs_SolidColor =
            "uniform 	mat4 		uMVPMatrix;" +
                    "attribute 	vec4 		vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    public static final String fs_SolidColor =
            "precision mediump float;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(0.5,0,0,1);" +
                    "}";

    /* SHADER Image
     *
     * This shader is for rendering 2D images straight from a texture
     * No additional effects.
     *
     */
    public static final String vs_Image =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_texCoord = a_texCoord;" +
                    "}";

    public static final String fs_Image =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture1;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture1, v_texCoord );" +
                    "}";

    //===========================================================后添加，移动相关=======================================================
    //attribute顶点数据，uniform常量数据，由java程序传递给shader,这些数据通常是变换矩阵，
    // 光照参数，颜色等。由 uniform 修饰符修饰的变量属于全局变量，该全局性对顶点着色器与片元着色器均可见，
    //sampler2D一种特殊的 uniform，在vertex shader中是可选的，用于呈现纹理。sampler 可用于顶点着色器和片元着色器。
    //varying 变量用于存储顶点着色器的输出数据，也存储片元着色器的输入数据,在顶点着色器阶段至少应输出位置信息-即内建变量：
    // gl_Position，是每个点固有的Varying，表示点的空间位置。其它两个可选的变量为：gl_FrontFacing 和 gl_PointSize
    //这里是为了测试是否能够实现两套shader去为两层纹理绘制不同的东西
    public static final String vs_Face =
            "uniform mat4 uMVPMatrix;" +
            "uniform mat4 transMatrix;" +
            "uniform mat4 scaleMatrix;" +
            "uniform mat4 rotateMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = (uMVPMatrix + transMatrix)* scaleMatrix  * vPosition  * rotateMatrix;" +
                    //先缩放，再确定位置，再旋转
                    "  v_texCoord = a_texCoord;" +
                    "}";

    public static final String fs_Face =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
                    "}";
    //----------------------------------------------------------------------------------------------------------------------------
    /**public static String vs_Face=
            "attribute vec4 vPosition;" +
            "attribute vec2 textCoordinate;" +
            "varying  vec2 a_texCoord;" +
            "uniform mat4 latetion;" +
            "uniform mat4 facetion;" +
            "uniform mat4 scaletion;" +
            "uniform mat4 secondScale;" +
            "uniform mat4 rotateMatrix;" +
            "void main()" +
            "{" +
            "varyTextCoord = textCoordinate;" +
            "gl_Position = vPosition *latetion *secondScale*rotateMatrix* scaletion  *facetion;" +
            "}";
    public static String fs_Face=
            "varying  vec2 v_texCoord;" +
            "varying  vec2 varyOtherPostion;" +
            "uniform sampler2D myTexture1;" +
            "void main()" +
            " vec2 vary = vec2(1.0-v_texCoord.x,v_texCoord.y);" +
            " vec4 text = texture2D(myTexture1,  vary);" +
            "gl_FragColor = vec4(text.rgb,text.a);" +
            "}";**/
//==============================================================================================================================================
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);

        GLES20.glCompileShader(shader);

        // return the shader
        return shader;
    }

}
