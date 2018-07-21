import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;

public interface IGraphic{
	void buildPropertyPane(GridPane propertyPane);
	Circle getConnectPoint(Point2D p1, Point2D p2);
	void updateConnectedPoints(Rectangle2D oldRect, Rectangle2D newRect, Circle c);
	void adjust(Circle c);
	void save(DataOutputStream dos)throws Exception;
	void read(DataInputStream dis)throws Exception;
	void addConnectCircle(Circle c);
	void delete();
	List<RelationshipLineGraphic> getRelationshipLines();
}