package AASMAProject.Graphics;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;

import java.util.Collection;

public class MultiAgentFaultDetectorPlacementStrategy implements SmartPlacementStrategy {

    public static final double SPACING = 2;
    public static final double SERVER_FD_SPACING = 2;

    @Override
    public <V, E> void place(double width, double height, Graph<V, E> theGraph, Collection<? extends SmartGraphVertex<V>> vertices) {
        double cX = width / 2;
        double cY = height / 2;
        int N = vertices.size() / 2;

        double r = 0;
        boolean first = true;

        //System.out.println(String.format("width=%f height=%f", cX, cY));

        for (SmartGraphVertex<V> vertex : vertices) {
            if (first){
                r = Math.sqrt((2 * vertex.getRadius() * vertex.getRadius()) / (1 - Math.cos((2 * Math.PI) / (SPACING * N))));
                //System.out.println(String.format("vertex-radius=%f", vertex.getRadius()));
                first = false;
            }

            int k = getTrailingInt(vertex.getUnderlyingVertex().element().toString());
            double angle = -Math.PI / 2 - (2 * Math.PI) / N * k;

            double x = cX + ((vertex.getUnderlyingVertex().element().toString().startsWith("S")) ? SERVER_FD_SPACING : 1) * r * Math.cos(angle);
            double y = cY + ((vertex.getUnderlyingVertex().element().toString().startsWith("S")) ? SERVER_FD_SPACING : 1) * r * Math.sin(angle);

            //System.out.println(String.format("%s: x=%f y=%f r=%f angle=%f", vertex.getUnderlyingVertex().element().toString(), x, y, r, angle));

            vertex.setPosition(x, y);
        }
    }

    private int getTrailingInt(String s){
        int res = 0;
        int power = 1;

        for(int i = s.length() - 1; i >= 0; i--){
            int digit = s.charAt(i) - '0';

            if(digit >= 0 && digit <= 9){
                res += digit * power;
            }
            else{
                break;
            }

            power *= 10;
        }

        return res;
    }
}
