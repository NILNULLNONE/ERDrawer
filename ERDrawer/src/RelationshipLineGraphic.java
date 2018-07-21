import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.stage.Stage;

public class RelationshipLineGraphic extends Group implements IGraphic {
	private ObservableList<Circle> circleList = FXCollections.observableArrayList();
	private final String MULTIPLE = "Multiple";
	private final String SINGLE = "Single";
	private final String INHERIT = "Inherit";
	private double cirRadii = 5;
	private final String DOUBLE_LINE = "DOUBLE_LINE";
	private final String DASH_LINE = "DASH_LINE";
	private final String SOLID_LINE = "SOLIDE_LINE";

	private Polyline outerLine = new Polyline();
	{
		outerLine.getStyleClass().add("outerLine");
		outerLine.getPoints().addListener(new ListChangeListener<Double>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Double> chg) {
				// TODO Auto-generated method stub
				while (chg.next()) {
					innerLine.getPoints().setAll(outerLine.getPoints());
				}
			}

		});
		outerLine.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			if (ERDrawer.cutBtn.isSelected()) {
				Point2D pInPar = this.localToParent(e.getX(), e.getY());
				Point2D pInParPar = this.getParent().localToParent(pInPar.getX(), pInPar.getY());
				for (int i = 0; i < circleList.size() - 1; i++) {
					Circle c1 = circleList.get(i);
					Circle c2 = circleList.get(i + 1);
					if (ERUtil.pointInLine(pInParPar, new Point2D(c1.getCenterX(), c1.getCenterY()),
							new Point2D(c2.getCenterX(), c2.getCenterY()))) {
						Circle newCir = new Circle(pInParPar.getX(), pInParPar.getY(), cirRadii);
						this.iniCircle(newCir);
						int index = (i + 1) * 2;
						circleList.add(i + 1, newCir);
						outerLine.getPoints().add(index, newCir.getCenterY());
						outerLine.getPoints().add(index, newCir.getCenterX());
						this.getChildren().add(newCir);
						break;
					}
				}
			}
		});
	}
	private Polyline innerLine = new Polyline();
	{
		innerLine.getStyleClass().add("innerLine");
	}
	private Polygon startSingleArrow = new Polygon();
	private Polygon startInheritArrow = new Polygon();
	{
		startInheritArrow.setFill(Color.WHITE);
		startInheritArrow.setStrokeWidth(1.0);
		startInheritArrow.setStroke(Color.BLACK);
	}
	private Polygon endSingleArrow = new Polygon();
	private Polygon endInheritArrow = new Polygon();
	{
		endInheritArrow.setFill(Color.WHITE);
		endInheritArrow.setStrokeWidth(1.0);
		endInheritArrow.setStroke(Color.BLACK);
	}
	private double singleArrowW = 6;
	private double singleArrowH = 3;
	private double inheritArrowW = 12;
	private double inheritArrowH = 7;
	private Node lastNode = null;
	private Node nowNode = null;
	public RelationshipLineGraphic(){
		this.getStyleClass().add("relationshipLine");		
		this.getChildren().addAll(outerLine, innerLine);
	}
	public RelationshipLineGraphic(Node lastNode, Circle lastCir, Node nowNode, Circle nowCir) {
		this();
		this.lastNode = lastNode;
		this.nowNode = nowNode;
		iniLastOrNowCircle(lastCir, lastNode);
		iniLastOrNowCircle(nowCir, nowNode);
		outerLine.getPoints().setAll(lastCir.getCenterX(), lastCir.getCenterY(), nowCir.getCenterX(),
				nowCir.getCenterY());
		circleList.addAll(lastCir, nowCir);
		this.getChildren().addAll(lastCir, nowCir);	
	}
	
	private void iniLastOrNowCircle(Circle cir, Node node){
		iniCircleHelp(cir);
		((IGraphic)node).addConnectCircle(cir);
		((IGraphic)node).getRelationshipLines().add(this);
		node.layoutXProperty().addListener((prop, o, n) -> {
			cir.setCenterX(cir.getCenterX() + (n.doubleValue() - o.doubleValue()));
		});
		node.layoutYProperty().addListener((prop, o, n) -> {
			cir.setCenterY(cir.getCenterY() + (n.doubleValue() - o.doubleValue()));
		});
		cir.setRadius(cirRadii);
		ChangeListener listener = (prop, o, n) -> {
			((IGraphic) node).adjust(cir);
			circleCenterChange(cir);
		};
		cir.getProperties().put("listener",listener);
		cir.centerXProperty().addListener(listener);
		cir.centerYProperty().addListener(listener);
	}

	private void circleCenterChange(Circle c) {
		int index = getIndex(c);
		outerLine.getPoints().set(index * 2, c.getCenterX());
		outerLine.getPoints().set(index * 2 + 1, c.getCenterY());
		int size = circleList.size();
		if (index == 1 || index == 0)
			this.updateStartType(startTypeBox.getValue());
		if (index == (size - 2) || index == (size - 1))
			this.updateEndType(endTypeBox.getValue());
	}

	private Integer getIndex(Circle c) {
		return circleList.indexOf(c);
	}

	void updateStartType(String str) {
		if (this.getChildren().contains(startInheritArrow)) {
			this.getChildren().remove(startInheritArrow);
		}
		if (this.getChildren().contains(startSingleArrow)) {
			this.getChildren().remove(startSingleArrow);
		}
		List<Double> p = outerLine.getPoints();
		double sx = circleList.get(0).getCenterX();
		double sy = circleList.get(0).getCenterY();
		double ex = circleList.get(1).getCenterX();
		double ey = circleList.get(1).getCenterY();
		p.set(0, sx);
		p.set(1, sy);
		if (!str.equals(MULTIPLE)) {
			double w = Math.abs(sx - ex);
			double h = Math.abs(sy - ey);
			double w2 = w * w;
			double h2 = h * h;
			if (w2 + h2 < 0.00000001)
				return;
			double arrowW, arrowH;
			if (str.equals(SINGLE)) {
				arrowW = singleArrowW;
				arrowH = singleArrowH;
			} else {
				arrowW = inheritArrowW;
				arrowH = inheritArrowH;
			}
			double l2 = arrowH * arrowH;
			double h2pw2 = h2 + w2;
			double rate = arrowW / Math.sqrt(h2pw2);
			double dx = Math.sqrt(l2 * h2 / h2pw2);
			double dy = Math.sqrt(l2 * w2 / h2pw2);
			double pivx = sx + rate * (ex - sx);
			double pivy = sy + rate * (ey - sy);
			Point2D p1 = null;
			Point2D p2 = null;
			Point2D p3 = new Point2D(sx, sy);
			if ((ex - sx) * (ey - sy) < 0) {
				p1 = new Point2D(pivx - dx, pivy - dy);
				p2 = new Point2D(pivx + dx, pivy + dy);
			} else {
				p1 = new Point2D(pivx - dx, pivy + dy);
				p2 = new Point2D(pivx + dx, pivy - dy);
			}
			if (str.equals(SINGLE)) {
				startSingleArrow.getPoints().setAll(p1.getX(), p1.getY(), p3.getX(), p3.getY(), p2.getX(), p2.getY());
				p.set(0, pivx);
				p.set(1, pivy);
				this.getChildren().add(startSingleArrow);
			} else {
				startInheritArrow.getPoints().setAll(p1.getX(), p1.getY(), p3.getX(), p3.getY(), p2.getX(), p2.getY(),
						p1.getX(), p1.getY());
				p.set(0, pivx);
				p.set(1, pivy);
				this.getChildren().add(startInheritArrow);
			}
		}
	}

	void updateEndType(String str) {
		if (this.getChildren().contains(endInheritArrow)) {
			this.getChildren().remove(endInheritArrow);
		}
		if (this.getChildren().contains(endSingleArrow)) {
			this.getChildren().remove(endSingleArrow);
		}
		List<Double> p = outerLine.getPoints();
		int psize = p.size();
		int csize = circleList.size();
		double sx = circleList.get(csize - 1).getCenterX();
		double sy = circleList.get(csize - 1).getCenterY();
		double ex = circleList.get(csize - 2).getCenterX();
		double ey = circleList.get(csize - 2).getCenterY();
		p.set(psize - 2, sx);
		p.set(psize - 1, sy);
		if (!str.equals(MULTIPLE)) {
			double w = Math.abs(sx - ex);
			double h = Math.abs(sy - ey);
			double w2 = w * w;
			double h2 = h * h;
			if (w2 + h2 < 0.00000001)
				return;
			double arrowW, arrowH;
			if (str.equals(SINGLE)) {
				arrowW = singleArrowW;
				arrowH = singleArrowH;
			} else {
				arrowW = inheritArrowW;
				arrowH = inheritArrowH;
			}
			double l2 = arrowH * arrowH;
			double h2pw2 = h2 + w2;
			double rate = arrowW / Math.sqrt(h2pw2);
			double dx = Math.sqrt(l2 * h2 / h2pw2);
			double dy = Math.sqrt(l2 * w2 / h2pw2);
			double pivx = sx + rate * (ex - sx);
			double pivy = sy + rate * (ey - sy);
			Point2D p1 = null;
			Point2D p2 = null;
			Point2D p3 = new Point2D(sx, sy);
			if ((ex - sx) * (ey - sy) < 0) {
				p1 = new Point2D(pivx - dx, pivy - dy);
				p2 = new Point2D(pivx + dx, pivy + dy);
			} else {
				p1 = new Point2D(pivx - dx, pivy + dy);
				p2 = new Point2D(pivx + dx, pivy - dy);
			}
			if (str.equals(SINGLE)) {
				endSingleArrow.getPoints().setAll(p1.getX(), p1.getY(), p3.getX(), p3.getY(), p2.getX(), p2.getY());
				p.set(psize - 2, pivx);
				p.set(psize - 1, pivy);
				this.getChildren().add(endSingleArrow);
			} else {
				endInheritArrow.getPoints().setAll(p1.getX(), p1.getY(), p3.getX(), p3.getY(), p2.getX(), p2.getY(),
						p1.getX(), p1.getY());
				p.set(psize - 2, pivx);
				p.set(psize - 1, pivy);
				this.getChildren().add(endInheritArrow);
			}
		}
	}

	@Override
	public Circle getConnectPoint(Point2D p1, Point2D p2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void adjust(Circle c) {
		// TODO Auto-generated method stub
	}

	
	@Override
	public void updateConnectedPoints(Rectangle2D oldRect, Rectangle2D newRect, Circle c) {
		// TODO Auto-generated method stub

	}

	ComboBox<String> startTypeBox = new ComboBox<String>();
	ComboBox<String> endTypeBox = new ComboBox<String>();
	ComboBox<String> lineTypeBox = new ComboBox<String>();
	{
		startTypeBox.getItems().addAll(MULTIPLE, SINGLE, INHERIT);
		endTypeBox.getItems().addAll(MULTIPLE, SINGLE, INHERIT);
		lineTypeBox.getItems().addAll(SOLID_LINE, DASH_LINE, DOUBLE_LINE);
		startTypeBox.setValue(MULTIPLE);
		endTypeBox.setValue(MULTIPLE);
		lineTypeBox.setValue(SOLID_LINE);
		startTypeBox.valueProperty().addListener((prop, o, n) -> {
			this.updateStartType(n);
		});
		endTypeBox.valueProperty().addListener((prop, o, n) -> {
			this.updateEndType(n);
		});
		lineTypeBox.valueProperty().addListener((prop, o, n) -> {
			ERUtil.setPseudoState(this, o.toLowerCase(), false);
			ERUtil.setPseudoState(this, n.toLowerCase(), true);
		});
	}

	@Override
	public void buildPropertyPane(GridPane propertyPane) {
		// TODO Auto-generated method stub
		propertyPane.getChildren().clear();
		propertyPane.addRow(0, new Label("Start type: "), startTypeBox);
		propertyPane.addRow(1, new Label("End type: "), endTypeBox);
		propertyPane.addRow(2, new Label("Line type: "), lineTypeBox);
	}

	private void iniCircle(Circle c) {
		iniCircleHelp(c);
		c.centerXProperty().addListener((prop, o, n) -> {
			circleCenterChange(c);

		});
		c.centerYProperty().addListener((prop, o, n) -> {
			circleCenterChange(c);
		});
	}
	
	private void iniCircleHelp(Circle c){
		c.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			if(ERDrawer.deleteBtn.isSelected()){
				int index = this.getIndex(c);
				int size = circleList.size();
				if(index == 0 || index == size - 1){
					this.delete();
				}else{
					circleList.remove(c);
					this.getChildren().remove(c);
					outerLine.getPoints().remove(index * 2, index * 2 + 2);
				}
			}
		});
		c.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
			c.getProperties().put("mv_start_pos", new Point2D(e.getScreenX(), e.getScreenY()));
			c.getProperties().put("mv_start_trans", new Point2D(c.getCenterX(), c.getCenterY()));
		});
		c.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
			Object obj = c.getProperties().get("mv_start_pos");
			if (obj != null) {
				Point2D startPos = (Point2D) obj;
				Point2D startTrans = (Point2D) (c.getProperties().get("mv_start_trans"));
				Point2D nowPos = new Point2D(e.getScreenX(), e.getScreenY());
				Point2D dPos = new Point2D(nowPos.getX() - startPos.getX(), nowPos.getY() - startPos.getY());
				double _x = startTrans.getX() + dPos.getX();
				double _y = startTrans.getY() + dPos.getY();
				if (_x < 0)
					_x = 0;
				if (_y < 0)
					_y = 0;
				c.setCenterX(_x);
				c.setCenterY(_y);
			}
		});
		c.visibleProperty().bind(ERDrawer.circleBtn.selectedProperty());
	}


	@Override
	public void save(DataOutputStream dos) throws Exception {
		// TODO Auto-generated method stub
		dos.writeUTF(this.getClass().getSimpleName());
		dos.writeUTF(lastNode.toString());
		dos.writeUTF(nowNode.toString());
		dos.writeInt(circleList.size());
		for(Circle c : circleList){
			dos.writeDouble(c.getCenterX());
			dos.writeDouble(c.getCenterY());
		}
		dos.writeUTF(startTypeBox.getValue());
		dos.writeUTF(endTypeBox.getValue());
		dos.writeUTF(lineTypeBox.getValue());
	}
	
	@Override
	public void read(DataInputStream dis) throws Exception {
		// TODO Auto-generated method stub
		String lastN = dis.readUTF();
		String nowN = dis.readUTF();
		lastNode = (Node)CanvasTab.readMap.get(lastN);
		nowNode = (Node)CanvasTab.readMap.get(nowN);
		int size = dis.readInt();
		List<Circle> l = new ArrayList<Circle>();
		for(int i = 0; i < size; i++){
			double cenX = dis.readDouble();
			double cenY = dis.readDouble();
			Circle c = new Circle(cenX, cenY, cirRadii);
			this.getChildren().add(c);
			circleList.add(c);
			outerLine.getPoints().addAll(cenX, cenY);
			l.add(c);
		}
		for(int i = 0; i < size; i++){
			if(i == 0)iniLastOrNowCircle(l.get(i), lastNode);
			else if(i == size - 1)iniLastOrNowCircle(l.get(i), nowNode);
			else iniCircle(l.get(i));
		}
		startTypeBox.setValue(dis.readUTF());
		endTypeBox.setValue(dis.readUTF());
		lineTypeBox.setValue(dis.readUTF());	
	}
	@Override
	public void addConnectCircle(Circle c) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void delete() {
		// TODO Auto-generated method stub
		((IGraphic)lastNode).getRelationshipLines().remove(this);
		((IGraphic)nowNode).getRelationshipLines().remove(this);
		((Pane)this.getParent()).getChildren().remove(this);
	}
	@Override
	public List<RelationshipLineGraphic> getRelationshipLines() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
