package dev.cube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL10;

public class Grid {
	private FloatBuffer vertexBuffer;
	private float vertices[];
	private ShortBuffer[] indexBuffer;
	private short[][] indices;
	int N;
	
	public Grid(int n) {
		N = n+1;
		vertices = new float[3 * N * N];
		indices = new short[N][N];
		indexBuffer = new ShortBuffer[2 * N];

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();

		int x = 0;
		short y = 0;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				vertices[x++] = j;
				vertices[x++] = i;
				vertices[x++] = 0;
				indices[i][j] = y++;
			}
			addIndexToBuffer(i, i);
		}

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				indices[i][j] = (short) (i + N * j);
			}
			addIndexToBuffer(i + N, i);
		}

		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
	}
	
	private void addIndexToBuffer(int bufferPointer, int arrayPointer) {
		ByteBuffer ibb = ByteBuffer.allocateDirect(N * 2);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer[bufferPointer] = ibb.asShortBuffer();
		indexBuffer[bufferPointer].put(indices[arrayPointer]);
		indexBuffer[bufferPointer].position(0);
	}
	
	public void draw(GL10 gl) {
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

		//gl.glPushMatrix();
		//gl.glTranslatef(-2.5f, -4f, 0f);
		for (int i = 0; i < 2 * N; i++) 
			gl.glDrawElements(GL10.GL_LINE_STRIP, N, GL10.GL_UNSIGNED_SHORT, indexBuffer[i]);
        //gl.popMatrix();
        
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
}
