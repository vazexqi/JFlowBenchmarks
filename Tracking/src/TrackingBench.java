import java.io.IOException;

public class TrackingBench {

    public static void main(String args[]) throws IOException {
        //int nump = 32; //32; // 60;
        int nump= 22;
        TrackDemo tdmo= new TrackDemo(nump);
        tdmo.run();
    }
}
