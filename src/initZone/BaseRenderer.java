package initZone;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.List;

import com.jogamp.common.util.locks.RecursiveLock;
import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLRunnable;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.FovHVHalves;

public class BaseRenderer implements GLEventListener {
	
	private int ProgramID = 0;
	private int vertexShader,fragmentShader;
	private IntBuffer VBO = IntBuffer.wrap(new int[1]);
	private int[] VAO = new int[1];
	public float[] camera;
	public float[] perspective;
	
	//Beware optimization. Shaders could lose uniforms if not used in main().
	public static String[] vertSrc = {
			"#version 330\n",
			"layout (location = 0) in vec3 position;\n",
			"uniform mat4 translationM;\n",
			"uniform mat4 cameraM;\n",
			"uniform mat4 perspectiveM;\n",
			"void main(){\n",
			"gl_Position = perspectiveM * cameraM *translationM * vec4(position,1.0);\n",
			"}\n"
	};
	
	public static String[] fragSrc = {
		"#version 330\n",
		"out vec4 color;\n",
		"void main(){\n",
		"color = vec4(1.0,0.2,0.15,1.0);\n",
		"}\n"
	};
	
	
	public BaseRenderer() {
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		//Buffer to determine success of compilation and linking.
		IntBuffer successToken = IntBuffer.wrap(new int[1]);
		//Buffer for returned messages
		ByteBuffer infoLog = ByteBuffer.wrap(new byte[512]);
		
		GL3 gl = drawable.getGL().getGL3();
		//Create the shaders, store the ID's generated.
		vertexShader = makeShader(vertSrc, gl ,gl.GL_VERTEX_SHADER);
		vertexShader = gl.glCreateShader(gl.GL_VERTEX_SHADER);
		fragmentShader = gl.glCreateShader(gl.GL_FRAGMENT_SHADER);
		//Set sources and compile
			//v
		gl.glShaderSource(vertexShader, vertSrc.length, vertSrc, null);
		gl.glCompileShader(vertexShader);
			//f
		gl.glShaderSource(fragmentShader, fragSrc.length, fragSrc, null);
		gl.glCompileShader(fragmentShader);
		//Check compile status of vertex shader...
		gl.glGetShaderiv(vertexShader, gl.GL_COMPILE_STATUS, successToken);
		if(successToken.get() == 0){
			gl.glGetShaderInfoLog(vertexShader, 512, (IntBuffer)null, infoLog);
			System.out.println(new String(infoLog.array(), Charset.forName("UTF-8")));
		}
		else{
			System.out.println();
		}
		
		//Reset between compile checks
		successToken.rewind();
		infoLog.rewind();
		//...Check compile status of fragment shader.
		gl.glGetShaderiv(fragmentShader, gl.GL_COMPILE_STATUS, successToken);
		if(successToken.get() == 0){
			gl.glGetShaderInfoLog(fragmentShader, 512, (IntBuffer)null, infoLog);
			System.out.println(new String(infoLog.array(), Charset.forName("UTF-8")));
		}
		else{
			System.out.println();
		}
		//Generate, make, and link rasterization program.
		ProgramID = gl.glCreateProgram();
		
		gl.glAttachShader(ProgramID, vertexShader);
		gl.glAttachShader(ProgramID, fragmentShader);
		
		gl.glLinkProgram(ProgramID);
		//Reset between compile and link check.
		successToken.rewind();
		infoLog.rewind();
		//Check link status.
		gl.glGetProgramiv(ProgramID, gl.GL_LINK_STATUS, successToken);
		
		if(successToken.get() == 0) {
		    gl.glGetProgramInfoLog(ProgramID, 512, (IntBuffer)null, infoLog);
		    System.out.println(new String(infoLog.array(),Charset.forName("UTF-8")));
		}
		else{
			System.out.println();
		}
		
		
		
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void display(GLAutoDrawable drawable) {
			GL3 gl = drawable.getGL().getGL3();
			gl.glClearColor(0.0f, .45f, .77f, 1.0f);
			gl.glClear(gl.GL_COLOR_BUFFER_BIT);
			
			float[] triangle = {-0.5f,0.0f,0.0f, 0.5f,0.0f,0.0f, 0.0f,0.5f,0.0f};
			float[] idMat = new float[16];
			//Get a triangle on screen.
			gl.glGenVertexArrays(1, VAO, 0);
//			gl.glGenVertexArrays(1, VAO);
			gl.glGenBuffers(1, VBO);
			
			gl.glBindVertexArray(VAO[0]);

			gl.glBindBuffer(gl.GL_ARRAY_BUFFER,VBO.get(0));
			
			gl.glBufferData(gl.GL_ARRAY_BUFFER, 36, FloatBuffer.wrap(triangle), gl.GL_STATIC_DRAW);
			
			gl.glVertexAttribPointer(0, 3, gl.GL_FLOAT, false, 12,0);
			gl.glEnableVertexAttribArray(0);
			gl.glBindVertexArray(0);
			
			gl.glUseProgram(ProgramID);
			gl.glBindVertexArray(VAO[0]);
			
			camera = new float[16];
			perspective = new float[16];
			
			FloatUtil.makeLookAt(camera, 0, new float[]{0.0f,0.0f,-10.5f}, 0, new float[]{0.0f,0.0f,0.0f},
					0, new float[]{0.0f,1.0f,0.0f}, 0, new float[16]);
			FloatUtil.makePerspective(perspective, 0, true, 45.0f, .75f ,0.001f, 100.0f);
			
			
			int uniTranslationMatrix = gl.glGetUniformLocation(ProgramID, "translationM");
			int cameraPositionMatrix = gl.glGetUniformLocation(ProgramID, "cameraM");
			int perspectivePositionMatrix = gl.glGetUniformLocation(ProgramID, "perspectiveM");
			//System.out.println(perspectivePositionMatrix);
			
			FloatUtil.makeTranslation(idMat, true, 0, 0, 0);
			
			gl.glUniformMatrix4fv(uniTranslationMatrix, 1, false, idMat,0);
			gl.glUniformMatrix4fv(cameraPositionMatrix, 1, false, camera,0);
			gl.glUniformMatrix4fv(perspectivePositionMatrix, 1, false, perspective,0);
			
			
			
			gl.glDrawArrays(gl.GL_TRIANGLES, 0, 3);
			gl.glUseProgram(0);
			
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL3 gl = drawable.getGL().getGL3();
		
		gl.glViewport(x, y, width, height);
		
	}

	private int makeShader(String[] src, GL3 gl, int shaderType){
		if(shaderType == gl.GL_VERTEX_SHADER){
			
			return 0;
		}
		else if(shaderType == gl.GL_FRAGMENT_SHADER){
			return 1;
		}
		return 0;
	}
	
}
