package dev.cube;

import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL10;

public class Frame { 
    private FloatBuffer vertexBuffer;	
	private ShortBuffer indexBuffer;
	private FloatBuffer colorBuffer;
	private float vertices[] = { // 12
        0.0f, 0.0f, 0.0f, // 0
        0.0f, 0.0f, 0.0f, // 1
        0.0f, 0.0f, 0.0f, // 2
        0.0f, 0.0f, 0.0f  // 3
    };
	private short[] indices = { 0, 1, 0, 2, 0, 3 };
	float colors[] = { 0.5f, 0.5f, 0.5f, 0.5f,
                       1f, 0f, 0f, 1f, // red   x
                       1f, 1f, 0f, 1f, // yellow y
                       0f, 0f, 1f, 1f  // blue  z
    };

	public Frame() {		
		vertices[3] = 5.0f;
		vertices[7] = 5.0f;
		vertices[11] = 5.0f;
		init();
	}
	public Frame(int n, int h){		
		vertices[3] = n;
		vertices[7] = n;
		vertices[11] = h;
		init();
	}
	
	public void draw(GL10 gl) {
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
		gl.glLineWidth(2);
		gl.glDrawElements(GL10.GL_LINES, indices.length, GL10.GL_UNSIGNED_SHORT, indexBuffer);

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	}
    
	private void init(){
		vertexBuffer = getDirectBuffer(vertices);
		indexBuffer = getDirectBuffer(indices);
		colorBuffer = getDirectBuffer(colors);
	}

    public FloatBuffer getDirectBuffer(float[] buffer) { 
        ByteBuffer bb = ByteBuffer.allocateDirect(buffer.length * 4); 
        bb.order(ByteOrder.nativeOrder()); 
        FloatBuffer directBuffer = bb.asFloatBuffer(); 
        directBuffer.put(buffer); 
        directBuffer.position(0); 
        return directBuffer; 
    }
    
    public ShortBuffer getDirectBuffer(short[] buffer) { 
        ByteBuffer bb = ByteBuffer.allocateDirect(buffer.length * 2); 
        bb.order(ByteOrder.nativeOrder()); 
        ShortBuffer directBuffer = bb.asShortBuffer(); 
        directBuffer.put(buffer); 
        directBuffer.position(0); 
        return directBuffer; 
    } 
} 
