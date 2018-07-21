import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;

public class RelationshipSetGraphic extends StackPane
			implements IGraphic{
	BooleanProperty isIdentifying = new SimpleBooleanProperty(false);
	Label nameLabel = new Label("Relationship");
	Polygon diamond = new Polygon();
	Polyline diamondOutline = new Polyline();
	List<RelationshipLineGraphic>relationshipLines = new ArrayList<RelationshipLineGraphic>();
	public RelationshipSetGraphic(){
		this.getStyleClass().add("relationship");
		diamond.getStyleClass().add("diamond");
		diamondOutline.getStyleClass().add("diamondOutline");
		diamond.setManaged(false);
		diamondOutline.setManaged(false);
		this.nameLabel.setMinWidth(10);
		this.getChildren().addAll(diamond, diamondOutline, nameLabel);
		this.nameLabel.widthProperty().addListener((prop, o, n) -> {
			updateDiamond();
		});
		this.nameLabel.heightProperty().addListener((prop, o, n) -> {
			updateDiamond();
		});
		isIdentifying.addListener((prop, o, n) -> {
			ERUtil.setPseudoState(this, "identifying", n);
		});
		
	}
	public void updateDiamond(){
		double w = this.nameLabel.getWidth();
		double h = this.nameLabel.getHeight();
		Point2D top = new Point2D(w, 0);
		Point2D right = new Point2D(w * 2.0, h);
		Point2D bottom = new Point2D(w, h * 2.0);
		Point2D left = new Point2D(0, h);
		diamond.getPoints().setAll(top.getX(), top.getY(), 
									right.getX(), right.getY(),
									bottom.getX(), bottom.getY(),
									left.getX(), left.getY(),
									top.getX(), top.getY());
		diamondOutline.getPoints().setAll(top.getX(), top.getY(), 
									right.getX(), right.getY(),
									bottom.getX(), bottom.getY(),
									left.getX(), left.getY(),
									top.getX(), top.getY()); 
		diamond.setLayoutX(- w / 2.0);
		diamond.setLayoutY(- h / 2.0);
		diamondOutline.setLayoutX(- w / 2.0);
		diamondOutline.setLayoutY(- h / 2.0);
	}
	
	TextField nameTF = new TextField();
	CheckBox isIdentifyingCB = new CheckBox();
	{
		nameTF.textProperty().bindBidirectional(this.nameLabel.textProperty());
		isIdentifyingCB.selectedProperty().bindBidirectional(this.isIdentifying);
	}
	@Override
	public void buildPropertyPane(GridPane propertyPane) {
		// TODO Auto-generated method stub
		propertyPane.getChildren().clear();
		propertyPane.addRow(0, new Label("Name: "), nameTF);
		propertyPane.addRow(1, new Label("Identifying: "), isIdentifyingCB);		
	}
	@Override
	public Circle getConnectPoint(Point2D p1, Point2D p2) {
		// TODO Auto-generated method stub
		double w = this.getWidth();
		double h = this.getHeight();
		Point2D left = this.localToParent(new Point2D(- w / 2.0, h / 2.0));
		Point2D top = this.localToParent(new Point2D(w / 2.0, - h / 2.0));
		Point2D right = this.localToParent(new Point2D(3.0 * w / 2.0, h / 2.0));
		Point2D bottom = this.localToParent(new Point2D(w / 2.0, 3.0 * h / 2.0));
		Point2D intersectP = ERUtil.getIntersectPoint(left, top, p1, p2);
		if (intersectP == null) {
			intersectP = ERUtil.getIntersectPoint(top, right, p1, p2);
			if (intersectP == null) {
				intersectP = ERUtil.getIntersectPoint(right, bottom, p1, p2);
				if (intersectP == null) {
					intersectP = ERUtil.getIntersectPoint(bottom, left, p1, p2);
				}
			}
		}
		Circle c = null;
		if (intersectP != null) {
			c = new Circle(intersectP.getX(), intersectP.getY(), 3);
		}
		return c;
	}

	@Override
	public void updateConnectedPoints(Rectangle2D oldRect, Rectangle2D newRect, Circle c) {
		// TODO Auto-generated method stub
		Point2D posTmp = this.parentToLocal(c.getCenterX(), c.getCenterY());
		Point2D pos = new Point2D(posTmp.getX() + oldRect.getWidth() / 4.0, posTmp.getY() + oldRect.getHeight() / 4.0);
		double x = newRect.getWidth() * (pos.getX() / oldRect.getWidth());
		double y = newRect.getHeight() * (pos.getY() / oldRect.getHeight());
		pos = this.localToParent(x, y);
		c.setCenterX(pos.getX() - newRect.getWidth() / 4.0);
		c.setCenterY(pos.getY() - newRect.getHeight() / 4.0);
	}
	
	@Override
	public void adjust(Circle c) {
		// TODO Auto-generated method stub
		double x = c.getCenterX();
		double y = c.getCenterY();
		double w = this.getWidth();
		double h = this.getHeight();
		Point2D left = this.localToParent(- w / 2.0, h / 2.0);
		Point2D top = this.localToParent(w / 2.0, - h / 2.0);
		Point2D right = this.localToParent(3.0 * w / 2.0, h / 2.0);
		Point2D bottom = this.localToParent(w / 2.0, 3.0 * h / 2.0);
		double _x = x;
		if(_x < left.getX())_x = left.getX();
		else if(_x > right.getX())_x = right.getX();
		double y1, y2, rate;
		if(_x < top.getX()){
			rate = (_x - left.getX()) / w;
			y1 = left.getY() - rate * (left.getY() - top.getY());
			y2 = left.getY() + rate * (left.getY() - top.getY());
		}
		else{
			rate = (_x - top.getX()) / w;
			y1 = top.getY() + rate * (right.getY() - top.getY());
			y2 = bottom.getY() - rate * (bottom.getY() - right.getY());
		}
		double _y = y;
		double minY = Math.min(y1, y2);
		double maxY = Math.max(y1,  y2);
		if(_y < minY)_y = minY;
		else if(_y > maxY)_y = maxY;
		ChangeListener cl = (ChangeListener)c.getProperties().get("listener");
		c.centerXProperty().removeListener(cl);
		c.centerYProperty().removeListener(cl);
		c.setCenterX(_x);
		c.setCenterY(_y);
		c.centerXProperty().addListener(cl);
		c.centerYProperty().addListener(cl);
	}
	@Override
	public void save(DataOutputStream dos) throws Exception {
		// TODO Auto-generated method stub
		dos.writeUTF(this.getClass().getSimpleName());
		dos.writeUTF(this.toString());
		dos.writeUTF(this.nameLabel.getText());
		dos.writeBoolean(this.isIdentifying.get());
		dos.writeDouble(this.getLayoutX());
		dos.writeDouble(this.getLayoutY());
	}
	
	@Override
	public void read(DataInputStream dis) throws Exception {
		// TODO Auto-generated method stub
		String thisStr = dis.readUTF(dis);
		CanvasTab.readMap.put(thisStr, this);
		String relationshipName = dis.readUTF(dis);
		this.nameLabel.setText(relationshipName);
		this.isIdentifying.set(dis.readBoolean());
		double lx = dis.readDouble();
		double ly = dis.readDouble();
		this.setLayoutX(lx);
		this.setLayoutY(ly);

	}
	@Override
	public void addConnectCircle(Circle c) {
		// TODO Auto-generated method stub
		this.widthProperty().addListener((prop, o, n) -> {
			if(Math.abs(o.doubleValue()) < ERUtil.ERROR)return;
			this.updateConnectedPoints(new Rectangle2D(0, 0, o.doubleValue() * 2.0, this.getHeight() * 2.0), 
										new Rectangle2D(0, 0, n.doubleValue() * 2.0, this.getHeight() * 2.0), c);
		});
		this.heightProperty().addListener((prop, o, n) -> {
			if(Math.abs(o.doubleValue()) < ERUtil.ERROR)return;
			this.updateConnectedPoints(new Rectangle2D(0, 0, this.getWidth() * 2.0, o.doubleValue() * 2.0), 
										new Rectangle2D(0, 0, this.getWidth() * 2.0, n.doubleValue() * 2.0), c);
		});
	}
	@Override
	public void delete() {
		// TODO Auto-generated method stub
		for(int i = relationshipLines.size() - 1; i >= 0; i--){
			relationshipLines.get(i).delete();
		}
		((Pane)this.getParent()).getChildren().remove(this);
	}
	@Override
	public List<RelationshipLineGraphic> getRelationshipLines() {
		// TODO Auto-generated method stub
		return relationshipLines;
	}
	
}
