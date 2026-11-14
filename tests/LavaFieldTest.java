package luma.ui;
public class LavaFieldTest {
 public static void main(String[] args){LavaField l=new LavaField();float x=l.x[0],y=l.y[0],a=l.angle[0];int[] p=new int[96*54];l.render(p,96,54,0xff00ffff);boolean visible=false;for(int c:p)visible|=(c>>>24)>0;if(!visible)throw new AssertionError("no wax");
 LavaField merged=new LavaField();for(int i=0;i<LavaField.COUNT;i++){merged.x[i]=5;merged.y[i]=5;merged.radius[i]=.07f;merged.angle[i]=0;}
 merged.x[0]=.58f;merged.y[0]=.5f;merged.x[1]=.72f;merged.y[1]=.5f;int[] both=new int[101*101],one=new int[101*101];merged.render(both,101,101,0xff00ffff);merged.x[1]=5;merged.y[1]=5;merged.render(one,101,101,0xff00ffff);if((both[50*101+50]>>>24)<=(one[50*101+50]>>>24))throw new AssertionError("metaball fields should combine");
 for(int n=0;n<2000;n++)l.step(.05f,1.4f);if(Math.abs(x-l.x[0])+Math.abs(y-l.y[0])<.01f||Math.abs(a-l.angle[0])<.1f)throw new AssertionError("motion");
 for(int i=0;i<LavaField.COUNT;i++)if(!Float.isFinite(l.x[i])||l.x[i]<0||l.x[i]>1.4f||l.y[i]<0||l.y[i]>1||l.heat[i]<0||l.heat[i]>1)throw new AssertionError("bounds");System.out.println("Lava thermal and metaball tests passed");}
}
