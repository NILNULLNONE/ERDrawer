
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Circle;

public class CanvasTab extends Tab {
	Pane canvas = new Pane();
	ScrollPane cont = new ScrollPane(canvas);
	ArrayList<Node>allGraphic = new ArrayList<Node>();
	ObservableList<Node> selectedGraphic = FXCollections.observableArrayList();
	ObjectProperty<Node> activeGraphic = new SimpleObjectProperty<Node>();{
		activeGraphic.addListener((prop, o, n) -> {
			if(n == null){
				ERDrawer.propertyPane.getChildren().clear();
			}
		});
	}
	Line tryLine = new Line();
	Node lastNode = null;
	Point2D lastPoint = null;
	EventHandler<MouseEvent> tryLineFilter = null;
	File file = null;
	public static Map<String, Node>readMap = new HashMap<String, Node>();
	public CanvasTab(File file){
		this();
		this.file = file;
		this.readFile();
		this.setText(file.getName());
	}
	
	public CanvasTab() {
		this.setText("Untitled");
		this.setContent(cont);
		tryLine.setId("tryLine");
		setUpCanvas();
	}
	
	public void readFile(){
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			int len = dis.readInt();
			for(int i = 0; i < len; i++){
				String className = dis.readUTF(dis);
				Object obj = Class.forName(className).newInstance();
				((IGraphic)obj).read(dis);
				if(!(obj instanceof RelationshipLineGraphic)){
					this.addNewGraphic((Node)obj);
				}
				else{
					this.addRLG((Node)obj);
				}
			}
			dis.close();
			this.readMap.clear();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void save(){
		if(file == null){
			file = ERDrawer.chooser.showSaveDialog(ERDrawer.stage);
			if(file == null)return;
			else{
				saveHelp();
				this.setText(file.getName());
			}
		}else{
			saveHelp();
		}
	}
	
	private void saveHelp(){
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
			dos.writeInt(allGraphic.size());
			List l = new ArrayList();
			for(Node n : allGraphic){
				if(n instanceof RelationshipLineGraphic){
					l.add(n);
				}else{
					((IGraphic)n).save(dos);
				}
			}
			for(Object n : l){
				((IGraphic)n).save(dos);
			}
			dos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setUpCanvas() {
		canvas.setMinSize(800, 600);
		canvas.getStyleClass().add("my_canvas");

		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			Node node = null;
			if (ERDrawer.entityBtn.isSelected()) {
				node = new EntitySetGraphic();
			} else if (ERDrawer.relationshipBtn.isSelected()) {
				node = new RelationshipSetGraphic();
			} else if (ERDrawer.descAttrBtn.isSelected()) {
				node = new DescAttrGraphic();
			}
			if (node != null) {
				addNewGraphic(node);
				node.setLayoutX(e.getX());
				node.setLayoutY(e.getY());
			}
			else{
				changeActiveGraphicTo(null);
			}
			if (ERDrawer.relationshipLineBtn.isSelected() && lastNode != null) {
				List<Node> chd = canvas.getChildren();
				for (Node n : chd) {
					if (n instanceof DescAttrGraphic || n instanceof EntitySetGraphic
							|| n instanceof RelationshipSetGraphic) {
						if (n != lastNode && n.getBoundsInParent().contains(e.getX(), e.getY())) {
							Point2D nowPoint = new Point2D(e.getX(), e.getY());
							Circle lastCir = ((IGraphic) lastNode).getConnectPoint(lastPoint, nowPoint);
							Circle nowCir = ((IGraphic) n).getConnectPoint(lastPoint, nowPoint);
							if(lastCir != null && nowCir != null){
								RelationshipLineGraphic rlg = new RelationshipLineGraphic(lastNode, lastCir, n,
										nowCir);
								this.addRLG(rlg);
								cancelTryLine();
							}
							break;
						}
					}
				}
			}
			cancelTryLine();
		});

		ERDrawer.stage.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
			if (e.getCode() == KeyCode.ESCAPE){
				Toggle tog = ERDrawer.itemsGroup.getSelectedToggle();
				if(tog != null)
					tog.setSelected(false);
			}
		});
		
		ERDrawer.relationshipLineBtn.selectedProperty().addListener((prop, o, n) -> {
			if(!n)cancelTryLine();
		});
	}
	
	private void addNewGraphic(Node node) {
		canvas.getChildren().add(0, node);
		allGraphic.add(node);
		ERUtil.enableMove(node);
		this.clearSelectedAndActiveGraphics();
		changeActiveGraphicTo(node);
		node.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			if(ERDrawer.deleteBtn.isSelected()){
				((IGraphic)node).delete();
				return;
			}
			
			if (e.isControlDown()) {
				if (node.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active"))) {
					unactive(node);
					select(node);
				} else if (node.getPseudoClassStates().contains(PseudoClass.getPseudoClass("selected"))) {
					unselect(node);
				} else {
					changeActiveGraphicTo(node);
				}
			} else {
				this.clearSelectedAndActiveGraphics();
				this.active(node);
			}

			if (!(node instanceof RelationshipLineGraphic) && ERDrawer.relationshipLineBtn.isSelected()) {
				Point2D localPos = new Point2D(e.getX(), e.getY());
				Point2D screenPos = node.localToScreen(localPos);
				Point2D canvasPos = canvas.screenToLocal(screenPos);
				if (lastNode == null) {
					canvas.getChildren().add(tryLine);
					tryLine.setStartX(canvasPos.getX());
					tryLine.setStartY(canvasPos.getY());
					tryLine.setEndX(canvasPos.getX());
					tryLine.setEndY(canvasPos.getY());
					tryLineFilter = e2 -> {
						Point2D endCanvasPos = new Point2D(e2.getX(), e2.getY());
						tryLine.setEndX(endCanvasPos.getX());
						tryLine.setEndY(endCanvasPos.getY());
					};
					canvas.addEventFilter(MouseEvent.MOUSE_MOVED, tryLineFilter);
					lastNode = node;
					lastPoint = canvasPos;
				}
			}
			e.consume();
		});
		
		
	}

	private void addRLG(Node rlg){
		canvas.getChildren().add(rlg);
		allGraphic.add(rlg);
		this.clearSelectedAndActiveGraphics();
		changeActiveGraphicTo(rlg);
		rlg.addEventHandler(MouseEvent.MOUSE_CLICKED, e2 -> {
			if (e2.isControlDown()) {
				if (rlg.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active"))) {
					unactive(rlg);
					select(rlg);
				} else if (rlg.getPseudoClassStates()
						.contains(PseudoClass.getPseudoClass("selected"))) {
					unselect(rlg);
				} else {
					changeActiveGraphicTo(rlg);
				}
			} else {
				this.clearSelectedAndActiveGraphics();
				this.active(rlg);
			}
			e2.consume();
		});
	}
	
	public void cancelTryLine() {
		if (lastNode != null) {
			canvas.removeEventFilter(MouseEvent.MOUSE_MOVED, tryLineFilter);
			canvas.getChildren().remove(tryLine);
			lastNode = null;
			lastPoint = null;
		}
	}

	private void changeActiveGraphicTo(Node node) {
		if(node == null){
			this.clearSelectedAndActiveGraphics();
			return;
		}
		Node lastActiveGraphic = activeGraphic.get();
		if (lastActiveGraphic != null) {
			unactive(lastActiveGraphic);
			select(lastActiveGraphic);
		}
		active(node);
	}

	private void clearSelectedAndActiveGraphics() {
		for (int i = selectedGraphic.size() - 1; i >= 0; i--) {
			unselect(selectedGraphic.get(i));
		}

		Node n = activeGraphic.get();
		if (n != null) {
			unactive(n);
		}
	}

	private void unselect(Node n) {
		selectedGraphic.remove(n);
		n.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
	}

	private void select(Node n) {
		selectedGraphic.add(0, n);
		n.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
	}

	private void unactive(Node n) {
		activeGraphic.set(null);
		n.pseudoClassStateChanged(PseudoClass.getPseudoClass("active"), false);
	}

	private void active(Node n) {
		activeGraphic.set(n);
		n.pseudoClassStateChanged(PseudoClass.getPseudoClass("active"), true);
		if (n instanceof IGraphic) {
			((IGraphic) n).buildPropertyPane(ERDrawer.propertyPane);
		}
	}
}
