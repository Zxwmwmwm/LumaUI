package luma.ui;
public class PlayFieldTest {
  static void check(boolean b){if(!b)throw new AssertionError();}
  public static void main(String[] args){
    PlayField p=new PlayField();float x=p.x[0],y=p.y[0];p.pointer(x,y,true,PlayField.POP,1);check(p.burst[0]>0);for(int i=0;i<10;i++)p.step(.05f,PlayField.POP);check(p.burst[0]==0&&p.y[0]>1);
    p.reset();p.pointer(.2f,.3f,true,PlayField.TRAIL,1);check(p.age[0]==0);p.step(.05f,PlayField.TRAIL);check(p.age[0]>0);p.pointer(.5f,.5f,true,PlayField.TRAIL,1);check(p.age[1]==0);
    p.reset();p.x[0]=.2f;p.y[0]=.5f;p.vx[0]=0;p.vy[0]=0;p.pointer(.8f,.5f,true,PlayField.MAGNET,1);for(int i=0;i<10;i++)p.step(.05f,PlayField.MAGNET);check(p.x[0]>.2f);p.reset();p.pointer(.5f,.5f,true,PlayField.MAGNET,1);for(int i=0;i<180;i++)p.step(.05f,PlayField.MAGNET);float gathered=0;for(int i=0;i<PlayField.COUNT;i++)gathered+=Math.abs(p.x[i]-.5f)+Math.abs(p.y[i]-.5f);check(gathered/PlayField.COUNT<.15f);
    p.pointer(.8f,.5f,true,PlayField.MAGNET,1);for(int i=0;i<PlayField.COUNT;i++){p.x[i]=.8f;p.y[i]=.5f;p.vx[i]=0;p.vy[i]=0;}p.pointer(.8f,.5f,false,PlayField.MAGNET,1);check(!p.pressed);float initialX=p.x[0],initialY=p.y[0];p.step(.05f,PlayField.MAGNET);check(Math.abs(p.x[0]-initialX)+Math.abs(p.y[0]-initialY)>.01f);check(Math.abs(p.vx[0]-p.vx[1])+Math.abs(p.vy[0]-p.vy[1])>.05f);
    for(int i=0;i<1000;i++)p.step(.05f,PlayField.MAGNET);for(int i=0;i<PlayField.COUNT;i++)check(Float.isFinite(p.x[i])&&p.x[i]>=0&&p.x[i]<=1&&p.y[i]>=0&&p.y[i]<=1);
    System.out.println("Interactive background simulation tests passed");
  }
}
