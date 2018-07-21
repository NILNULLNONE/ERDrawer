import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class EntitySetGraphic extends SplitPane implements IGraphic {
	Label entityNameLabel = new Label();
	StackPane nameBox = new StackPane(entityNameLabel);
	VBox attrBox = new VBox();
	final double ATTR_BOX_MIN_W = 80;
	final double ATTR_BOX_MIN_H = 30;
	List<RelationshipLineGraphic>relationshipLines = new ArrayList<RelationshipLineGraphic>();
	public EntitySetGraphic() {
		entityNameLabel.setText("Entity");
		this.getItems().addAll(nameBox, attrBox);
		this.setOrientation(Orientation.VERTICAL);
		this.getStyleClass().add("entity");
		attrBox.setMinSize(ATTR_BOX_MIN_W, ATTR_BOX_MIN_H);
	}

	public void addAttribute() {
		addAttribute("Attribute", false, false);
	}

	public void addAttribute(String str, boolean isPri, boolean isPar) {
		AttributeGraphic ag = new AttributeGraphic(str);
		ag.isPrimaryKeyProperty().set(isPri);
		ag.isPartialKeyProperty().set(isPar);
		attrView.getItems().add(ag);
		attrBox.getChildren().add(ag);
		this.setDividerPositions(0);
	}

	Label nameLabel = new Label("Name: ");
	TextField nameTF = new TextField();
	Label attrLabel = new Label("Attributes: ");
	Button addAttrBtn = new Button("+");
	Button delAttrBtn = new Button("-");
	Button upAttrBtn = new Button("<");
	Button downAttrBtn = new Button(">");
	HBox btnHBox = new HBox(5, addAttrBtn, delAttrBtn, upAttrBtn, downAttrBtn);
	TableView attrView = new TableView();
	VBox attrVBox = new VBox(btnHBox, attrView);
	TableColumn attrCol = new TableColumn("Attr.");
	TableColumn priCol = new TableColumn("Pri.");
	TableColumn parCol = new TableColumn("Par.");
	{
		attrCol.setCellValueFactory(new PropertyValueFactory("_text"));
		priCol.setCellValueFactory(new PropertyValueFactory("isPrimaryKey"));
		parCol.setCellValueFactory(new PropertyValueFactory("isPartialKey"));
		attrCol.setCellFactory(TextFieldTableCell.<AttributeGraphic>forTableColumn());
		priCol.setCellFactory(CheckBoxTableCell.<AttributeGraphic>forTableColumn(priCol));
		parCol.setCellFactory(CheckBoxTableCell.<AttributeGraphic>forTableColumn(parCol));
		attrView.getColumns().addAll(attrCol, priCol, parCol);
		attrView.setEditable(true);
		btnHBox.getStyleClass().add("attrBtnBox");
		addAttrBtn.setOnAction(e -> {
			addAttribute();
		});
		delAttrBtn.setOnAction(e -> {
			int selInd = attrView.getSelectionModel().getSelectedIndex();
			if (selInd >= 0) {
				attrView.getItems().remove(selInd);
				attrBox.getChildren().remove(selInd);
				if (selInd < attrView.getItems().size()) {
					attrView.getSelectionModel().select(selInd);
				}
			}
		});
		upAttrBtn.setOnAction(e -> {
			SelectionModel selMod = attrView.getSelectionModel();
			int selInd = selMod.getSelectedIndex();
			if (selInd > 0) {
				Object tmp = attrView.getItems().get(selInd - 1);
				Object tmp2 = attrView.getItems().get(selInd);
				attrView.getItems().set(selInd - 1, tmp2);
				attrView.getItems().set(selInd, tmp);
				selMod.select(selInd - 1);

				attrBox.getChildren().remove(selInd);
				attrBox.getChildren().add(selInd - 1, (Node) tmp2);
			}
		});
		downAttrBtn.setOnAction(e -> {
			SelectionModel selMod = attrView.getSelectionModel();
			int selInd = selMod.getSelectedIndex();
			if ((selInd < attrView.getItems().size() - 1) && selInd >= 0) {
				Object tmp = attrView.getItems().get(selInd + 1);
				Object tmp2 = attrView.getItems().get(selInd);
				attrView.getItems().set(selInd + 1, tmp2);
				attrView.getItems().set(selInd, tmp);
				selMod.select(selInd + 1);

				attrBox.getChildren().remove(selInd + 1);
				attrBox.getChildren().add(selInd, (Node) tmp);
			}
		});
		GridPane.setHgrow(nameLabel, Priority.ALWAYS);
		GridPane.setHgrow(attrLabel, Priority.ALWAYS);
		nameTF.textProperty().bindBidirectional(entityNameLabel.textProperty());
	}

	@Override
	public void buildPropertyPane(GridPane propertyPane) {
		// TODO Auto-generated method stub
		propertyPane.getChildren().clear();
		int index = 0;
		propertyPane.addRow(index++, nameLabel, nameTF);
		propertyPane.addRow(index++, attrLabel, attrVBox);
	}

	@Override
	public Circle getConnectPoint(Point2D p1, Point2D p2) {
		// TODO Auto-generated method stub
		Point2D leftTop = this.localToParent(0, 0);
		Point2D rightTop = this.localToParent(this.getWidth(), 0);
		Point2D rightBottom = this.localToParent(this.getWidth(), this.getHeight());
		Point2D leftBottom = this.localToParent(0, this.getHeight());
		Point2D intersectP = ERUtil.getIntersectPoint(leftTop, rightTop, p1, p2);
		if (intersectP == null) {
			intersectP = ERUtil.getIntersectPoint(rightTop, rightBottom, p1, p2);
			if (intersectP == null) {
				intersectP = ERUtil.getIntersectPoint(rightBottom, leftBottom, p1, p2);
				if (intersectP == null) {
					intersectP = ERUtil.getIntersectPoint(leftBottom, leftTop, p1, p2);
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
		Point2D pos = this.parentToLocal(c.getCenterX(), c.getCenterY());
		double x = newRect.getWidth() * (pos.getX() / oldRect.getWidth());
		double y = newRect.getHeight() * (pos.getY() / oldRect.getHeight());
		pos = this.localToParent(x, y);
		c.setCenterX(pos.getX());
		c.setCenterY(pos.getY());
	}

	@Override
	public void adjust(Circle c) {
		// TODO Auto-generated method stub
		double x = c.getCenterX();
		double y = c.getCenterY();
		Point2D p = this.parentToLocal(x, y);
		double _x = p.getX();
		double _y = p.getY();
		if (_x < 0)
			_x = 0;
		else if (_x > this.getWidth())
			_x = this.getWidth();
		if (_y < 0)
			_y = 0;
		else if (_y > this.getHeight())
			_y = this.getHeight();
		p = this.localToParent(_x, _y);
		ChangeListener cl = (ChangeListener) c.getProperties().get("listener");
		c.centerXProperty().removeListener(cl);
		c.centerYProperty().removeListener(cl);
		c.setCenterX(p.getX());
		c.setCenterY(p.getY());
		c.centerXProperty().addListener(cl);
		c.centerYProperty().addListener(cl);
	}

	@Override
	public void save(DataOutputStream dos) throws Exception {
		// TODO Auto-generated method stub
		dos.writeUTF(this.getClass().getSimpleName());
		dos.writeUTF(this.toString());
		dos.writeUTF(this.entityNameLabel.getText());
		List l = attrView.getItems();
		dos.writeInt(l.size());
		for (Object obj : l) {
			AttributeGraphic ag = (AttributeGraphic) obj;
			dos.writeUTF(ag.getText());
			dos.writeBoolean(ag.isPrimaryKeyProperty().get());
			dos.writeBoolean(ag.isPartialKeyProperty().get());
		}
		dos.writeDouble(this.getLayoutX());
		dos.writeDouble(this.getLayoutY());
		dos.writeDouble(this.getDividerPositions()[0]);
	}

	@Override
	public void read(DataInputStream dis) throws Exception {
		// TODO Auto-generated method stub
		String thisStr = dis.readUTF(dis);
		CanvasTab.readMap.put(thisStr, this);
		String entityName = dis.readUTF(dis);
		this.entityNameLabel.setText(entityName);
		int len = dis.readInt();
		String attr;
		boolean isPri, isPar;
		for (int i = 0; i < len; i++) {
			attr = dis.readUTF(dis);
			isPri = dis.readBoolean();
			isPar = dis.readBoolean();
			this.addAttribute(attr, isPri, isPar);
		}
		double lx = dis.readDouble();
		double ly = dis.readDouble();
		this.setLayoutX(lx);
		this.setLayoutY(ly);
		double pos = dis.readDouble();
		this.setDividerPositions(pos);
	}

	@Override
	public void addConnectCircle(Circle c) {
		// TODO Auto-generated method stub
		this.widthProperty().addListener((prop, o, n) -> {
			if(Math.abs(o.doubleValue()) < ERUtil.ERROR)return;
			this.updateConnectedPoints(new Rectangle2D(0, 0, o.doubleValue(), this.getHeight()),
					new Rectangle2D(0, 0, n.doubleValue(), this.getHeight()), c);
		});
		this.heightProperty().addListener((prop, o, n) -> {
			if(Math.abs(o.doubleValue()) < ERUtil.ERROR)return;
			this.updateConnectedPoints(new Rectangle2D(0, 0, this.getWidth(), o.doubleValue()),
					new Rectangle2D(0, 0, this.getWidth(), n.doubleValue()), c);
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
