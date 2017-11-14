precision highp float;
precision highp int;
precision lowp sampler2D;
varying highp vec2 v_TexCoordinateA;
varying highp vec2 v_TexCoordinateB;
uniform sampler2D u_Texture;

void main() {
    	highp vec4 colA = texture2D(u_Texture, v_TexCoordinateA);
    	highp vec4 colB = texture2D(u_Texture, v_TexCoordinateB);
        gl_FragColor = colA ;
     }