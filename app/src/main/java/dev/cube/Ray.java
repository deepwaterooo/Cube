package dev.cube;

import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

public class Ray {
	private static FloatBuffer gBufPosition = IBufferFactory.newFloatBuffer(2 * 3);
	public Vector3f mvOrigin = new Vector3f();
	public Vector3f mvDirection = new Vector3f();

	public void transform(Matrix4f matrix, Ray out) {
		Vector3f v0 = Vector3f.TEMP;
		Vector3f v1 = Vector3f.TEMP1;
		v0.set(mvOrigin);
		v1.set(mvOrigin);
		v1.add(mvDirection);

		matrix.transform(v0, v0);
		matrix.transform(v1, v1);

		out.mvOrigin.set(v0);
		v1.sub(v0);
		v1.normalize();
		out.mvDirection.set(v1);
	}

	public void draw(GL10 gl) {
		gBufPosition.position(0);
		IBufferFactory.fillBuffer(gBufPosition, mvOrigin);
		Vector3f.TEMP.set(mvDirection);
		float len = 100.0f;
		Vector3f.TEMP.scale(len);
		Vector3f.TEMP.add(mvOrigin);
		IBufferFactory.fillBuffer(gBufPosition, Vector3f.TEMP);
		gBufPosition.position(0);

		if (AppConfig.gbTrianglePicked) {
			gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
		} else {
			gl.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
		}

		gl.glPointSize(4.0f);
		gl.glLineWidth(4.0f);
		gl.glDisable(GL10.GL_DEPTH_TEST);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, gBufPosition);
		gl.glDrawArrays(GL10.GL_POINTS, 0, 2);

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glPointSize(1f);
		gl.glLineWidth(1.0f);
	}

	public boolean intersectTriangle(Vector3f v0, Vector3f v1, Vector3f v2, Vector4f location) {
		return intersect(v0, v1, v2, location);
	}

	private static final float MAX_ABSOLUTE_ERROR = 0.000001f;

	private static Vector3f tmp0 = new Vector3f(), tmp1 = new Vector3f(),
			tmp2 = new Vector3f(), tmp3 = new Vector3f(),
			tmp4 = new Vector3f();

	private boolean intersect(Vector3f v0, Vector3f v1, Vector3f v2, Vector4f loc) {
		Vector3f diff = tmp0;
		Vector3f edge1 = tmp1;
		Vector3f edge2 = tmp2;
		Vector3f norm = tmp3;
		Vector3f tmp = tmp4;
		diff.sub(mvOrigin, v0);
		edge1.sub(v1, v0);
		edge2.sub(v2, v0);
		norm.cross(edge1, edge2);

		float dirDotNorm = mvDirection.dot(norm);
		float sign = 0.0f;

		if (dirDotNorm > MAX_ABSOLUTE_ERROR) {
			sign = 1;
		} else if (dirDotNorm < -MAX_ABSOLUTE_ERROR) {
			sign = -1;
			dirDotNorm = -dirDotNorm;
		} else {
			return false;
		}

		tmp.cross(diff, edge2);
		float dirDotDiffxEdge2 = sign * mvDirection.dot(tmp);
		if (dirDotDiffxEdge2 >= 0.0f) {
			tmp.cross(edge1, diff);
			float dirDotEdge1xDiff = sign * mvDirection.dot(tmp);
			if (dirDotEdge1xDiff >= 0.0f) {
				if (dirDotDiffxEdge2 + dirDotEdge1xDiff <= dirDotNorm) {
					float diffDotNorm = -sign * diff.dot(norm);
					if (diffDotNorm >= 0.0f) {
						if (loc == null) {
							return true;
						}
						float inv = 1f / dirDotNorm;
						float t = diffDotNorm * inv;
						loc.set(mvOrigin);
						loc.add(mvDirection.x * t, mvDirection.y * t, mvDirection.z * t);
						loc.w = t;
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean intersectSphere(Vector3f center, float radius) {
		Vector3f diff = tmp0;
		diff.sub(mvOrigin, center);
		float r2 = radius * radius;
		float a = diff.dot(diff) - r2;
		
		if (a <= 0.0f) { return true; 	}

		float b = mvDirection.dot(diff);
		
		if (b >= 0.0f) { return false; 	}

		return b * b >= a;
	}
}
