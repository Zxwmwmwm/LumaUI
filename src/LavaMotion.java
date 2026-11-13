package luma.ui;
/** Smooth, independently evolving lava shapes; coordinates are normalized to the panel. */
final class LavaMotion {
  static final int COUNT=12;
  private static float wave(float t){return (float)Math.sin(t);}
  static void sample(int i,float t,float[] out){
    float phase=i*2.39996f;
    out[0]=.5f+.37f*wave(t*(.15f+.013f*(i%5))+phase)+.055f*wave(t*.43f+phase*1.8f);
    out[1]=.5f+.38f*wave(t*(.12f+.017f*(i%4))+phase*1.31f)+.06f*wave(t*.31f-phase);
    float size=.032f+.013f*(i%4);
    float stretch=1.4f+.55f*wave(t*(.34f+.02f*(i%3))+phase)+.24f*wave(t*.73f-phase*2);
    out[2]=size/(float)Math.sqrt(Math.max(.45f,stretch));
    out[3]=size*(float)Math.sqrt(Math.max(.45f,stretch))*(1.35f+.18f*wave(t*.51f+phase));
    out[4]=.12f*wave(t*.57f+phase)+.06f*wave(t*.93f-phase);
  }
}
