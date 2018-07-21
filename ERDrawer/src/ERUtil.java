import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class ERUtil {
	public static final double ERROR = 0.1;
	
	public static void enableMove(Node n){
		n.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
			n.getProperties().put("mv_start_pos", new Point2D(e.getScreenX(), e.getScreenY()));
			n.getProperties().put("mv_start_trans", new Point2D(n.getLayoutX(), n.getLayoutY()));
		});
		n.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
			Object obj = n.getProperties().get("mv_start_pos");
			if(obj != null){
				Point2D startPos = (Point2D)obj;
				Point2D startTrans = (Point2D)(n.getProperties().get("mv_start_trans"));
				Point2D nowPos = new Point2D(e.getScreenX(), e.getScreenY());
				Point2D dPos = new Point2D(nowPos.getX() - startPos.getX(), nowPos.getY() - startPos.getY());
				double _x = startTrans.getX() + dPos.getX();
				double _y = startTrans.getY() + dPos.getY();
				if(_x < 0)_x = 0;
				if(_y < 0)_y = 0;
				n.setLayoutX(_x);
				n.setLayoutY(_y);
			}
		});
	}
	
	public static void setPseudoState(Node n, String pseudo, boolean active){
		n.pseudoClassStateChanged(PseudoClass.getPseudoClass(pseudo), active);
	}

	public static Point2D getIntersectPoint(Point2D p1, Point2D p2, Point2D p3, Point2D p4){
		Point2D ret = null;
		double x1 = p1.getX();
		double x2 = p2.getX();
		double x3 = p3.getX();
		double x4 = p4.getX();
		double y1 = p1.getY();
		double y2 = p2.getY();
		double y3 = p3.getY();
		double y4 = p4.getY();
		double dx1 = x2 - x1;
		double dx2 = x4 - x3;
		double dy1 = y2 - y1;
		double dy2 = y4 - y3;
		double foo = dy1 * dx2 - dy2 * dx1;
		if(Math.abs(foo) < 0.0000001)return ret;
		double t1 = ((x1 * dy2 - y1 * dx2) - (x3 * dy2 - y3 * dx2)) / foo;
		if(t1 < 0 || t1 > 1)return ret;
		double t2 = ((x1 * dy1 - y1 * dx1) - (x3 * dy1 - y3 * dx1)) / foo;
		if(t2 < 0 || t2 > 1)return ret;
		ret = new Point2D(x1 + t1 * dx1, y1 + t1 * dy1);
		return ret;
	}

	public static boolean pointInLine(Point2D p, Point2D p1, Point2D p2){
		double dx1 = p.getX() - p1.getX();
		double dy1 = p.getY() - p1.getY();
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		boolean nox = Math.abs(dx) < ERROR;
		boolean noy = Math.abs(dy) < ERROR;
		if(nox && noy)return false;
		else if(!nox && !noy){
			return dx1 / dx >=0 && dx1 / dx <= 1 
									&& dy1 / dy >=0 && dy1 / dy <= 1
									&& Math.abs(dx1 / dx - dy1 / dy) < ERROR;
		}else if(!nox && noy){
			return Math.abs(dy1) < ERROR && dx1 / dx >=0 && dx1 / dx <=1;
		}else{
			return Math.abs(dx1) < ERROR && dy1 / dy >=0 && dy1 / dy <=1;
		}
	}
}
