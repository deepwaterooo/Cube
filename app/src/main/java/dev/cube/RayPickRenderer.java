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
    int texture = -1; 
    public float mfAngleX = 0.0f; 
    public float mfAngleY = 0.0f; 
    public float gesDistance = 0.0f;
    
    private Vector3f mvEye = new Vector3f(0, 0, 7f);
    private Vector3f mvCenter = new Vector3f(0, 0, 0);
    private Vector3f mvUp = new Vector3f(0, 1, 0); 

    public RayPickRenderer(Context context) { 
        mContext = context; 
        cube = new Cube(); 
    } 
 
    @Override 
    public void onDrawFrame(GL10 gl) { // 逐帧渲染 
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); // 清除屏幕和深度缓存
        gl.glLoadIdentity(); // 重置当前的模型观察矩阵
        setUpCamera(gl);     // 紧接着设置模型视图矩阵
 
        gl.glPushMatrix();
        drawModel(gl); // 渲染物体
        gl.glPopMatrix(); 
 
        // gl.glPushMatrix(); 
        // PickFactory.getPickRay().draw(gl); // 渲染射线 
        // gl.glPopMatrix(); 
 
        gl.glPushMatrix();
        drawPickedTriangle(gl); // 渲染选中的三角形
        gl.glPopMatrix(); 
 
        updatePick(); 
    } 
 
    private void setUpCamera(GL10 gl) { 
        gl.glMatrixMode(GL10.GL_MODELVIEW); // 设置模型视图矩阵
        gl.glLoadIdentity(); 
        // GLU.gluLookAt(gl, mfEyeX, mfEyeY, mfEyeZ, mfCenterX, mfCenterY, mfCenterZ, 0, 1, 0); //系统提供
        Matrix4f.gluLookAt(mvEye, mvCenter, mvUp, AppConfig.gMatView); // static [4][4]
        gl.glLoadMatrixf(AppConfig.gMatView.asFloatBuffer());  // FloatBuffer [16]
    } 
 
    // private Matrix4f matRotX = new Matrix4f(); 
    // private Matrix4f matRotY = new Matrix4f(); 
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
 
    private void drawModel(GL10 gl) { // 渲染模型
        // 首先对模型进行变换 
        // -------使用系统函数进行变换 
        // gl.glRotatef(mfAngleX, 1, 0, 0);//绕X轴旋转 
        // gl.glRotatef(mfAngleY, 0, 1, 0);//绕Y轴旋转 
        // -------托管方式进行变换 
        // matRotX.setIdentity(); 
        // matRotY.setIdentity(); 
        // matRotX.rotX((float) (mfAngleX * Math.PI / 180)); 
        // matRotY.rotY((float) (mfAngleY * Math.PI / 180)); 
        // AppConfig.gMatModel.set(matRotX); 
        // AppConfig.gMatModel.mul(matRotY); 
 
        /* 以下方式完全按照手势方向旋转 */
        matRot.setIdentity(); 

        point = new Vector3f(mfAngleX, mfAngleY, 0); 
 
        try { // confusing for this part
            // 转换到模型内部的点，先要求逆             
            matInvertModel.set(AppConfig.gMatModel); 
            matInvertModel.invert();
            //System.out.println("matInvertModel in Renderer: ");
            //matInvertModel.printMatrix44(); // I
            matInvertModel.transform(point, point); // point still changes sometimes

            float d = Vector3f.distance(new Vector3f(), point); // (0, 0, 0) ==> Point 
            //System.out.println("d: " + d);

            // 再减少误差
            if (Math.abs(d - gesDistance) <= 1E-4) {
                // 绕这个单位向量旋转（由于误差可能会产生缩放而使得模型消失不见）
                matRot.glRotatef((float) (gesDistance * Math.PI / 180), point.x / d, point.y / d, point.z / d);
                //System.out.println("matRot in Renderer: ");
                //matRot.printMatrix44(); 

                // 旋转后在原基础上再转
                if (0 != gesDistance) 
                    AppConfig.gMatModel.mul(matRot); 
            } 
        } catch (Exception e) { // 由于四舍五入求逆矩阵失败 
        } 

        gesDistance = 0; 
        gl.glMultMatrixf(AppConfig.gMatModel.asFloatBuffer()); // 物体坐标系到世界坐标系的终变换矩阵
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.0f); 
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture); 
        gl.glEnable(GL10.GL_TEXTURE_2D); 
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); 
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
 
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, cube.getCoordinate(Cube.VERTEX_BUFFER)); 
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, cube.getCoordinate(Cube.TEXTURE_BUFFER)); 
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 24, GL10.GL_UNSIGNED_BYTE, cube.getIndices()); 
 
        gl.glDisable(GL10.GL_TEXTURE_2D); 
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); 
        // 渲染坐标系
        drawCoordinateSystem(gl); 
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
        //System.out.println("center in Renderer: ");
        //transformedSphereCenter.printV();
        
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
        } else { 
            AppConfig.gbTrianglePicked = false; 
        } 
    } 

    /** 
     * 渲染选中的三角形 
     */ 
    private void drawPickedTriangle(GL10 gl) { 
        if (!AppConfig.gbTrianglePicked) return; 

        // 由于返回的拾取三角形数据是出于模型坐标系中 
        // 因此需要经过模型变换，将它们变换到世界坐标系中进行渲染 
        // 设置模型变换矩阵 
        gl.glMultMatrixf(AppConfig.gMatModel.asFloatBuffer()); 
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
        // 提交渲染 
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3); 

        // 重置相关属性 
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); 
        gl.glEnable(GL10.GL_DEPTH_TEST); 
        gl.glDisable(GL10.GL_BLEND); 
    } 
 
    /** 
     * 渲染坐标系 
     */ 
    private void drawCoordinateSystem(GL10 gl) { 
        // 暂时禁用深度测试 
        gl.glDisable(GL10.GL_DEPTH_TEST); 
        // 设置点和线的宽度 
        gl.glLineWidth(2.0f); 
        // 仅仅启用顶点数据 
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); 
 
        FloatBuffer fb = IBufferFactory.newFloatBuffer(3 * 2); 
        fb.put(new float[] { 0, 0, 0, 1.4f, 0, 0 }); 
        fb.position(0); 
 
        // 渲染X轴 
        gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);// 设置红色 
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb); 
        gl.glDrawArrays(GL10.GL_LINES, 0, 2); 
 
        fb.clear(); 
        fb.put(new float[] { 0, 0, 0, 0, 1.4f, 0 }); 
        fb.position(0); 
        // 渲染Y轴 
        gl.glColor4f(1.0f, 1.0f, 0.0f, 1.0f);// 设置黄色 
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb); 
        gl.glDrawArrays(GL10.GL_LINES, 0, 2); 
 
        fb.clear(); 
        fb.put(new float[] { 0, 0, 0, 0, 0, 1.4f }); 
        fb.position(0); 
        // 渲染Z轴 
        gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);// 设置蓝色 
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb); 
        gl.glDrawArrays(GL10.GL_LINES, 0, 2); 
 
        // 重置 
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); 
        gl.glLineWidth(1.0f); 
        gl.glEnable(GL10.GL_DEPTH_TEST); 
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
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1); 
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
        float ratio = (float) width / height;// 屏幕宽高比 
        gl.glMatrixMode(GL10.GL_PROJECTION); 
        gl.glLoadIdentity(); 

        // GLU.gluPerspective(gl, 45.0f, ratio, 1, 5000);系统提供 
        Matrix4f.gluPersective(45.0f, ratio, 1, 10, AppConfig.gMatProject);
        //System.out.println("Renderer AppConfig.gMatProject matrix: ");
        //AppConfig.gMatProject.printMatrix44();
        //I/System.out: Renderer AppConfig.gMatProject matrix: 
        //I/System.out: 3.8426232  0.0  0.0  0.0  
        //I/System.out: 0.0  2.4142134  0.0  0.0  
        //I/System.out: 0.0  0.0  -1.2222222  -2.2222223  
        //I/System.out: 0.0  0.0  -1.0  0.0  
        
        gl.glLoadMatrixf(AppConfig.gMatProject.asFloatBuffer()); 
        AppConfig.gMatProject.fillFloatArray(AppConfig.gpMatrixProjectArray); 
        // 每次修改完GL_PROJECTION后，最好将当前矩阵模型设置回GL_MODELVIEW 
        gl.glMatrixMode(GL10.GL_MODELVIEW); 
    } 

    // worry about this function later on
    private void loadTexture(GL10 gl) { 
         // 启用纹理映射 
        gl.glClearDepthf(1.0f); 
        // 允许2D贴图,纹理 
        gl.glEnable(GL10.GL_TEXTURE_2D); 
 
        try { 
            IntBuffer intBuffer = IntBuffer.allocate(1); 
            // 创建纹理 
            gl.glGenTextures(1, intBuffer); 
            texture = intBuffer.get(); 
            // 设置要使用的纹理 
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture); 
 
            // 打开二进制流 
            InputStream is = mContext.getResources().openRawResource(R.drawable.snow_leopard); 
            Bitmap mBitmap = BitmapFactory.decodeStream(is); 
            //Log.i("height|width: ", mBitmap.getWidth() + " | " + mBitmap.getHeight()); // 160 | 173
 
            // 生成纹理 
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0); 
 
            // 线形滤波 
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR); 
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); 
 
            is.close(); 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } 
    } 
  
    public void setOnSurfacePickedListener(OnSurfacePickedListener onSurfacePickedListener) { 
        this.onSurfacePickedListener = onSurfacePickedListener; 
    } 
} 
