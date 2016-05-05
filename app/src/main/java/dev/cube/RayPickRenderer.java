package dev.cube;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.IOException;
import android.opengl.GLUtils;

public class RayPickRenderer implements Renderer { 
    private OnSurfacePickedListener onSurfacePickedListener; 
    private Context mContext; 
    private Cube cube;
    private Frame frame; // thought about pack into one object, but since the vertex & index using differently, 
    private Grid grid;   // leave it this way then. 
    
    int texture = -1; 
    public float mfAngleX = 0.0f; 
    public float mfAngleY = 0.0f; 
    public float gesDistance = 0.0f;
    
    private Vector3f mvEye = new Vector3f(5.0f, 8.0f, -4.0f);
    //private Vector3f mvEye = new Vector3f(5.0f, 10.0f, -4.0f); 
    //private Vector3f mvCenter = new Vector3f(-2.5f, -5f, -2.5f); // depends on if frame & cube together
    private Vector3f mvCenter = new Vector3f(0, 0, 0);
    private Vector3f mvUp = new Vector3f(0, 1, 0);

    public RayPickRenderer(Context context) { 
        mContext = context; 
        cube = new Cube();
        frame = new Frame(6, 10);
        grid = new Grid(5);
    } 
 
    private void loadTexture(GL10 gl) { 
         // 启用纹理映射 
        gl.glClearDepthf(1.0f); 
        // 允许2D贴图,纹理 
        gl.glEnable(GL10.GL_TEXTURE_2D); 
 
        //try { 
            IntBuffer intBuffer = IntBuffer.allocate(1); 
            // 创建纹理           一个具有足够空间保存纹理名的数组
            gl.glGenTextures(1, intBuffer); // GLuint, 纹理名称, 第一个参数指示了要生成几个纹理
            texture = intBuffer.get(); 
            // 设置要使用的纹理, 纹理绑定 
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture); 
 
            // 打开二进制流 
            //InputStream is = mContext.getResources().openRawResource(R.drawable.horse); 
            //Bitmap mBitmap = BitmapFactory.decodeStream(is); 
            //Log.i("height|width: ", mBitmap.getWidth() + " | " + mBitmap.getHeight()); // 160 | 173
 
            // 生成纹理           制定二维纹理映射,   多极分辩率纹理图像
            //GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GLImage.iBitmap, 0); 

            // 纹理控制： 缩小与放大滤波、纹理重复和缩限
            // 当纹理放大得比原始纹理大GL_TEXTURE_MAG_FILTER, 或
            // 当纹理缩小得比原始纹理小GL_TEXTURE_MIN_FILTER 时， 采用线性滤波方式
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST); 
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
            // 默认状态下OpenGL设置了使用所谓mipmap
            // Mipmap是一个图像不同尺寸的组合，
            //       它允许OpenGL选择最为接近的尺寸版本以避免过多的插值计算
            //       并且在物体远离观察者时通过使用更小的纹理来更好地管理内存
            
            // GL_NEAREST: 采用坐标最靠近象素中心的纹素，这有可能使图像走样，计算少执行速度快
            // GL_LINEAR:  采用最靠近象素中心的四个象素的加权平均值, 提供了比较光滑的效果

            // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            // glTexParameterfv(..., GL_REPEAT) [0f, 1.0f]
            // GL_REPEAT: 重复, 重复映射纹理可以在自己的坐标s、t方向上重复

            // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            // GL_CLAMP:  约减，所有大于1的纹素值重设为1,所有小于0的值重设为0

            // glTexEnv*() 控制纹理映射方式的函数
            // glTexCoord*(), 定义纹理坐标的函数
            
            //is.close(); 
            /*} catch (IOException e) { 
            e.printStackTrace(); 
            } */
    } 
  
    private void drawModel(GL10 gl) { // 渲染模型
        // 以下方式完全按照手势方向旋转 
        matRot.setIdentity(); 
        point = new Vector3f(mfAngleX, mfAngleY, 0); 
 
        try { // confusing for this part
            // 转换到模型内部的点，先要求逆             
            matInvertModel.set(AppConfig.gMatModel); 
            matInvertModel.invert();
            //matInvertModel.printMatrix44(); // happen to be I
            matInvertModel.transform(point, point); // point still changes sometimes

            float d = Vector3f.distance(new Vector3f(), point); // (0, 0, 0) ==> Point 

            // 再减少误差
            if (Math.abs(d - gesDistance) <= 1E-4) {
                // 绕这个单位向量旋转（由于误差可能会产生缩放而使得模型消失不见）
                matRot.glRotatef((float) (gesDistance * Math.PI / 180), point.x / d, point.y / d, point.z / d);
                //matRot.printMatrix44(); 

                // 旋转后在原基础上再转
                if (0 != gesDistance) 
                    AppConfig.gMatModel.mul(matRot); 
            } 
        } catch (Exception e) { // 由于四舍五入求逆矩阵失败 
        } 

        gesDistance = 0; 
        gl.glMultMatrixf(AppConfig.gMatModel.asFloatBuffer()); // 物体坐标系到世界坐标系的变换矩阵
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.0f); 
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture); // 绑定纹理在下面绘制的物体上 
        gl.glEnable(GL10.GL_TEXTURE_2D);               // 启用纹理映射 
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); 
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
 
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, cube.getCoordinate(Cube.VERTEX_BUFFER)); 
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, cube.getCoordinate(Cube.TEXTURE_BUFFER)); 
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 24, GL10.GL_UNSIGNED_BYTE, cube.getIndices()); 
 
        gl.glDisable(GL10.GL_TEXTURE_2D); 
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); 

        drawCoordinateSystem(gl); 
    } 

    private void drawCoordinateSystem(GL10 gl) { 
        gl.glDisable(GL10.GL_DEPTH_TEST); // 暂时禁用深度测试 
        gl.glLineWidth(2.0f);             // 设置点和线的宽度 
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); // 仅仅启用顶点数据 

        gl.glPushMatrix();
        gl.glTranslatef(-2.5f, -2.5f, -5f);
        frame.draw(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(-2.5f, -2.5f, -5f);
        grid.draw(gl);
        gl.glPopMatrix();
        
        // 重置 
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); 
        gl.glLineWidth(1.0f); 
        gl.glEnable(GL10.GL_DEPTH_TEST); 
    } 

    @Override 
    public void onDrawFrame(GL10 gl) { // 逐帧渲染 
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); // 清除屏幕和深度缓存
        gl.glLoadIdentity(); // 重置当前的模型观察矩阵
        setUpCamera(gl);     // 紧接着设置模型视图矩阵

        gl.glPushMatrix();
        drawModel(gl); // 渲染物体
        gl.glPopMatrix();

        gl.glPushMatrix();
        drawPickedTriangle(gl); // 渲染选中的三角形
        gl.glPopMatrix(); 
 
        updatePick(); 
    } 
 
    private void setUpCamera(GL10 gl) { 
        gl.glMatrixMode(GL10.GL_MODELVIEW); // 设置模型视图矩阵
        gl.glLoadIdentity();
        //gl.glTranslatef(0, 0, -4.5f); // look weird, frame supposed to move down to feel better
        Matrix4f.gluLookAt(mvEye, mvCenter, mvUp, AppConfig.gMatView); // static [4][4]
        gl.glLoadMatrixf(AppConfig.gMatView.asFloatBuffer());          // FloatBuffer [16]
    } 
 
    private Matrix4f matRot = new Matrix4f(); 
    private Vector3f point; 
    private Vector3f transformedSphereCenter = new Vector3f(); 
    private Ray transformedRay = new Ray(); 
    private Matrix4f matInvertModel = new Matrix4f(); 
    private Vector3f[] mpTriangle = {
        new Vector3f(),
        new Vector3f(), 
        new Vector3f()
    }; 
    private FloatBuffer mBufPickedTriangle = IBufferFactory.newFloatBuffer(3 * 3); 
 
    /** 
     * 渲染选中的三角形 
     */ 
    private void drawPickedTriangle(GL10 gl) { 
        if (!AppConfig.gbTrianglePicked) return; 
        // 由于返回的拾取三角形数据是出于模型坐标系中 
        // 因此需要经过模型变换，将它们变换到世界坐标系中进行渲染 
        gl.glMultMatrixf(AppConfig.gMatModel.asFloatBuffer());  // 设置模型变换矩阵 
        gl.glColor4f(1.0f, 1.0f, 0.0f, 0.7f);  // set to be yellow
        
        // 开启Blend混合模式 
        gl.glEnable(GL10.GL_BLEND); 
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA); 

        // 禁用无关属性，仅仅使用纯色填充 
        gl.glDisable(GL10.GL_DEPTH_TEST); 
        gl.glDisable(GL10.GL_TEXTURE_2D); 

        // 开始绑定渲染顶点数据 
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); 
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mBufPickedTriangle); // 选中三角形物体坐标系顶点坐标
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3); 

        // 重置相关属性 
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); 
        gl.glEnable(GL10.GL_DEPTH_TEST); 
        gl.glDisable(GL10.GL_BLEND); 
    } 
 
    /** 
     * 创建绘图表面时调用 
     */ 
    @Override 
    public void onSurfaceCreated(GL10 gl, EGLConfig config) { 
        // 全局性设置 
        gl.glEnable(GL10.GL_DITHER); 
 
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
        // 设置清屏背景颜色 
        //gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        gl.glClearColor(0f, 0f, 0f, 1); 
        // 设置着色模型为平滑着色 
        gl.glShadeModel(GL10.GL_SMOOTH); 
 
        // 启用背面剪裁 
        gl.glEnable(GL10.GL_CULL_FACE); 
        gl.glCullFace(GL10.GL_BACK); 
        // 启用深度测试 
        gl.glEnable(GL10.GL_DEPTH_TEST); 
        // 禁用光照和混合 
        gl.glDisable(GL10.GL_LIGHTING); 
        gl.glDisable(GL10.GL_BLEND); 
 
        loadTexture(gl); 
 
        AppConfig.gMatModel.setIdentity(); 
    } 
 
    // 更新拾取事件
    private void updatePick() {
        // 若拾取射线与模型的包围体不相交，就无须进行下一步最为耗时的射线与模型的精确三角形相交检测 
        if (!AppConfig.gbNeedPick) return; 
            
        AppConfig.gbNeedPick = false;
        // 更新最新的拾取射线 
        PickFactory.update(AppConfig.gScreenX, AppConfig.gScreenY); 
        // 获得最新的拾取射线 
        Ray ray = PickFactory.getPickRay();

        // 首先把模型的绑定球通过模型矩阵，由模型局部空间变换到世界空间 
        AppConfig.gMatModel.transform(cube.getSphereCenter(), transformedSphereCenter); // (0, 0, 0)
        
        // 触碰的立方体面的标记为无 
        cube.surface = -1; 

        // 首先检测拾取射线是否与模型绑定球发生相交 
        // 这个检测很快，可以快速排除不必要的精确相交检测 
        if (ray.intersectSphere(transformedSphereCenter, cube.getSphereRadius())) {
            // 如果射线与绑定球发生相交，那么就需要进行精确的三角面级别的相交检测 
            // 由于我们的模型渲染数据，均是在模型局部坐标系中 
            // 而拾取射线是在世界坐标系中 
            // 因此需要把射线转换到模型坐标系中 
            // 这里首先计算模型矩阵的逆矩阵             
            matInvertModel.set(AppConfig.gMatModel); 
            matInvertModel.invert();
            // 把射线变换到模型坐标系中，把结果存储到transformedRay中 
            ray.transform(matInvertModel, transformedRay);

            // 将射线与模型做精确相交检测             
            if (cube.intersect(transformedRay, mpTriangle)) {
                // 如果找到了相交的最近的三角形 
                AppConfig.gbTrianglePicked = true;
                // 触碰了哪一个面 
                Log.i("the surface touched ", "mark: " + cube.surface);
                // 回调 
                if (null != onSurfacePickedListener) 
                    onSurfacePickedListener.onSurfacePicked(cube.surface); 
                // 填充数据到被选取三角形的渲染缓存中 
                mBufPickedTriangle.clear(); 
                for (int i = 0; i < 3; i++) { 
                    IBufferFactory.fillBuffer(mBufPickedTriangle, mpTriangle[i]); // 更选选中三角形物体坐标系顶点坐标 
                    //Log.i("point: " + i, mpTriangle[i].x + "\t" + mpTriangle[i].y + "\t" + mpTriangle[i].z); 
                }
                mBufPickedTriangle.position(0); 
            } 
        } else 
            AppConfig.gbTrianglePicked = false; 
    } 

    /** 
     * 当绘图表面尺寸发生改变时调用 
     */ 
    @Override 
    public void onSurfaceChanged(GL10 gl, int width, int height) { 
        // 设置视口 
        gl.glViewport(0, 0, width, height); 
        AppConfig.gpViewport[0] = 0; 
        AppConfig.gpViewport[1] = 0; 
        AppConfig.gpViewport[2] = width; 
        AppConfig.gpViewport[3] = height; 
 
        // 设置投影矩阵 
        float ratio = (float) width / height; // 屏幕宽高比 0.62827224

        gl.glMatrixMode(GL10.GL_PROJECTION); 
        gl.glLoadIdentity(); 

        // GLU.gluPerspective(gl, 45.0f, ratio, 1, 5000);系统提供 
        Matrix4f.gluPersective(45.0f, ratio, 0.1f, 100.0f, AppConfig.gMatProject);
        gl.glLoadMatrixf(AppConfig.gMatProject.asFloatBuffer()); 
        AppConfig.gMatProject.fillFloatArray(AppConfig.gpMatrixProjectArray); 
        // 每次修改完GL_PROJECTION后，最好将当前矩阵模型设置回GL_MODELVIEW 
        gl.glMatrixMode(GL10.GL_MODELVIEW); 
    } 

    public void setOnSurfacePickedListener(OnSurfacePickedListener onSurfacePickedListener) { 
        this.onSurfacePickedListener = onSurfacePickedListener; 
    } 
} 
