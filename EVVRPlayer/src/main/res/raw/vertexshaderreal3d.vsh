attribute vec4 a_Position;
attribute vec2 a_TexCoordinateA;
attribute vec2 a_TexCoordinateB;

uniform mat4 u_MVPMatrix;
varying vec2 v_TexCoordinateA;
varying vec2 v_TexCoordinateB;

void main() {
    gl_Position = u_MVPMatrix*a_Position;
    v_TexCoordinateA = a_TexCoordinateA ;
    v_TexCoordinateB = a_TexCoordinateB ;
  }
