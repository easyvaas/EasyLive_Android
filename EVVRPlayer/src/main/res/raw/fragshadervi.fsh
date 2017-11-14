#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 v_TexCoordinate;
uniform samplerExternalOES u_Texture;

uniform bool forward;


void main(){
	if(forward)
	{
		if(v_TexCoordinate.y < 0.999 && v_TexCoordinate.y >0.001){
     	   gl_FragColor = texture2D(u_Texture, v_TexCoordinate);
   		 }else {
    	   gl_FragColor  = vec4(0.0,0.0,0.0,1.0);
		}
 	}
   else{	
		 gl_FragColor = texture2D(u_Texture, v_TexCoordinate);
	}

}