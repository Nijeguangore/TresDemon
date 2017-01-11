package initZone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
	private int vertexShader,fragmentShader,uniTranslationMatrix,cameraPositionMatrix,perspectivePositionMatrix;
	
	private IntBuffer VBO = IntBuffer.wrap(new int[1]);
	private int[] VAO = new int[1];
	
	public float[] camera;
	public float[] perspective;
	public float[] transform;
	
	private ArrayList<String> vertexSrc;
	private ArrayList<String> fragmentSrc;
	//Beware optimization. Shaders could lose uniforms if not used in main().
	
	public BaseRenderer() throws FileNotFoundException {
		fragmentSrc = new ArrayList<>();
		vertexSrc = new ArrayList<>();
		
		loadShaderSource(vertexSrc,"VertexShaderSrc");
		loadShaderSource(fragmentSrc,"FragmentShaderSrc");
		
		camera = new float[16];
		FloatUtil.makeLookAt(camera, 0, new float[]{0.0f,0.0f,-10.5f}, 0, new float[]{0.0f,0.0f,0.0f},
				0, new float[]{0.0f,1.0f,0.0f}, 0, new float[16]);
		
		perspective = new float[16];
		FloatUtil.makePerspective(perspective, 0, true, 45.0f, .75f ,0.001f, 100.0f);
		
		transform = new float[16];
		FloatUtil.makeTranslation(transform, true, 0, 0, 0);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		
		
		GL3 gl = drawable.getGL().getGL3();
		//Create the shaders, store the ID's generated.
		vertexShader = makeShader(vertexSrc.toArray(new String[0]), gl ,gl.GL_VERTEX_SHADER);
		//vertexShader = gl.glCreateShader(gl.GL_VERTEX_SHADER);
		fragmentShader = makeShader(fragmentSrc.toArray(new String[0]),gl,gl.GL_FRAGMENT_SHADER);
		//Generate, make, and link rasterization program.
		ProgramID = makeProgram(gl,vertexShader,fragmentShader);
		
		if(ProgramID != -1){
			uniTranslationMatrix = gl.glGetUniformLocation(ProgramID, "translationM");
			cameraPositionMatrix = gl.glGetUniformLocation(ProgramID, "cameraM");
			perspectivePositionMatrix = gl.glGetUniformLocation(ProgramID, "perspectiveM");
		}
		
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		
	}

	@Override
	public void display(GLAutoDrawable drawable) {
			GL3 gl = drawable.getGL().getGL3();
			gl.glClearColor(0.0f, .45f, .77f, 1.0f);
			gl.glClear(gl.GL_COLOR_BUFFER_BIT);
			
			float[] triangle = {-0.5f,0.0f,0.0f, 0.5f,0.0f,0.0f, 0.0f,0.5f,0.0f};
			//Get a triangle on screen.
			gl.glGenVertexArrays(1, VAO, 0);

			gl.glGenBuffers(1, VBO);
			
			gl.glBindVertexArray(VAO[0]);

			gl.glBindBuffer(gl.GL_ARRAY_BUFFER,VBO.get(0));
			
			gl.glBufferData(gl.GL_ARRAY_BUFFER, 36, FloatBuffer.wrap(triangle), gl.GL_STATIC_DRAW);
			
			gl.glVertexAttribPointer(0, 3, gl.GL_FLOAT, false, 12,0);
			gl.glEnableVertexAttribArray(0);
			gl.glBindVertexArray(0);
			
			gl.glUseProgram(ProgramID);
			gl.glBindVertexArray(VAO[0]);
			
			gl.glUniformMatrix4fv(uniTranslationMatrix, 1, false, transform,0);
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

	private void loadShaderSource(ArrayList<String> container, String fileSource) throws FileNotFoundException{
		File shaderFile = new File(fileSource);
		Scanner shadeReader = new Scanner(shaderFile);
		while(shadeReader.hasNextLine()){
			StringBuilder formatter = new StringBuilder();
			formatter.append(shadeReader.nextLine());
			formatter.append('\n');
			container.add(formatter.toString());
		}
		shadeReader.close();
	}
	
	private int makeShader(String[] src, GL3 gl, int shaderType){
		//Buffer to determine success of compilation and linking.
		IntBuffer successToken = IntBuffer.wrap(new int[1]);
		//Buffer for returned messages
		ByteBuffer infoLog = ByteBuffer.wrap(new byte[512]);
		
		if(shaderType == gl.GL_VERTEX_SHADER || shaderType == gl.GL_FRAGMENT_SHADER){
			int shaderID = gl.glCreateShader(shaderType);
			//Set source and compile
			gl.glShaderSource(shaderID, src.length, src, null);
			gl.glCompileShader(shaderID);
			//Check compile status of shader...
			gl.glGetShaderiv(shaderID, gl.GL_COMPILE_STATUS, successToken);
			if(successToken.get() == 0){
				gl.glGetShaderInfoLog(shaderID, 512, (IntBuffer)null, infoLog);
				System.out.println(new String(infoLog.array(), Charset.forName("UTF-8")));
				return -1;
			}
			return shaderID;
		}
		return -1;
	}
	
	private int makeProgram(GL3 gl, int vertexShader, int fragmentShader) {
		//Buffer to determine success of compilation and linking.
		IntBuffer successToken = IntBuffer.wrap(new int[1]);
		//Buffer for returned messages
		ByteBuffer infoLog = ByteBuffer.wrap(new byte[512]);
		
		int generatedProgram = gl.glCreateProgram();
		gl.glAttachShader(generatedProgram, vertexShader);
		gl.glAttachShader(generatedProgram, fragmentShader);
		gl.glLinkProgram(generatedProgram);
		
		gl.glGetProgramiv(generatedProgram, gl.GL_LINK_STATUS, successToken);
		
		if(successToken.get() == 0) {
		    gl.glGetProgramInfoLog(generatedProgram, 512, (IntBuffer)null, infoLog);
		    System.out.println(new String(infoLog.array(),Charset.forName("UTF-8")));
		    return -1;
		}
		
		return generatedProgram;
	}
	
}
