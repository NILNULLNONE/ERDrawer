import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class ERDrawer extends Application{
	static final int ENTITY_SET = 0;
	static final int RELATIONSHIP_SET = 1;
	static final int RELATIONSHIP_LINE = 2;
	static final int DESCRIPTIVE_ATTRIBUTE = 3;
	static Stage stage = null;
	static SplitPane sp = new SplitPane();
	static BorderPane root = new BorderPane();
	static MenuBar menuBar = new MenuBar();
	static Label statusBar = new Label();
	static Menu fileMenu = new Menu("File");
	static MenuItem newFileItem = new MenuItem("New");
	static MenuItem openItem = new MenuItem("Open");
	static MenuItem saveItem = new MenuItem("Save");
	static MenuItem saveAsItem = new MenuItem("Save as...");
	
	static Button newFileBtn = new Button("New");
	static Button openBtn = new Button("Open");
	static Button saveBtn = new Button("Save");
	static Button saveAsBtn = new Button("Save as...");
	static ToolBar toolbar = new ToolBar(newFileBtn, openBtn, saveBtn, saveAsBtn);
	
	static ToggleButton cutBtn = new ToggleButton("Cut");
	static ToggleButton circleBtn = new ToggleButton("Circle");
	public static ToggleButton deleteBtn = new ToggleButton("Delete");
	public static ToggleButton entityBtn = new ToggleButton("Entity");
	public static ToggleButton relationshipBtn = new ToggleButton("Relationship");
	public static ToggleButton relationshipLineBtn = new ToggleButton("Line");
	public static ToggleButton descAttrBtn = new ToggleButton("Descriptive attribute");
	static VBox itemsBox = new VBox(entityBtn, relationshipBtn, relationshipLineBtn, descAttrBtn, new Separator(), cutBtn, circleBtn, deleteBtn);
	static ToggleGroup itemsGroup = new ToggleGroup();
	
	static TabPane canvases = new TabPane();{
		canvases.getTabs().addListener(new ListChangeListener(){
			@Override
			public void onChanged(Change arg0) {
				while(arg0.next()){
					itemsBox.setVisible(canvases.getTabs().size() != 0);
				}
			}
	
		});
	}
	public static FileChooser chooser = new FileChooser();

	static SplitPane rightPane = new SplitPane();
	static GridPane outlinePane = new GridPane();
	static GridPane propertyPane = new GridPane();
	{
		propertyPane.getStyleClass().add("grid-pane");
	}
	
	public static void main(String[] args){
		Application.launch(args);
	}

	@Override
	public void start(Stage stage){
		// TODO Auto-generated method stub
		this.stage = stage;
		Scene scene = new Scene(root);
		scene.getStylesheets().add("/style.css");
		stage.setScene(scene);
		stage.show();
		root.setTop(new VBox(menuBar, toolbar));
		root.setCenter(sp);
		root.setBottom(statusBar);
		
		rightPane.getItems().addAll(outlinePane, new ScrollPane(propertyPane));
		rightPane.setOrientation(Orientation.VERTICAL);
		rightPane.setDividerPositions(0.3);
		sp.getItems().addAll(itemsBox, canvases, rightPane);
		sp.setDividerPositions(0.2, 0.8);
		
		chooser.getExtensionFilters().add(new ExtensionFilter("ER", "*.er"));

		setUpMenuBar();
		setUpToolBar();
		setUpItemsBox();
	}
		
	public void newFile(){
		CanvasTab canvasTab = new CanvasTab();
		canvases.getTabs().add(canvasTab);
		canvases.getSelectionModel().select(canvasTab);
	}
	
	public void open(){
		File file = chooser.showOpenDialog(stage);
		if(file != null){
			CanvasTab canvasTab = new CanvasTab(file);
			canvases.getTabs().add(canvasTab);
			canvases.getSelectionModel().select(canvasTab);
		}
	}
	
	public void save(){
		List<Tab>ts = canvases.getTabs();
		for(Tab t: ts){
			((CanvasTab)t).save();
		}
		statusBar.setText("Save!");
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Platform.runLater(() -> statusBar.setText(""));
			}
		}, 1000);
	}
	
	public void saveAs(){
		
	}
	
	void setUpMenuBar(){
		newFileItem.setOnAction(e -> newFile());
		openItem.setOnAction(e -> open());
		saveItem.setOnAction(e -> save());
		saveAsItem.setOnAction(e -> saveAs());
		fileMenu.getItems().addAll(newFileItem, openItem, saveItem, saveAsItem);
		menuBar.getMenus().addAll(fileMenu);
	}
	
	void setUpToolBar(){
		newFileBtn.setOnAction(e -> newFile());
		openBtn.setOnAction(e -> open());
		saveBtn.setOnAction(e -> save());
		saveAsBtn.setOnAction(e -> saveAs());
		circleBtn.setSelected(true);
	}
	
	void setUpItemsBox(){
		entityBtn.setToggleGroup(itemsGroup);
		entityBtn.setMaxWidth(Double.MAX_VALUE);
		relationshipBtn.setToggleGroup(itemsGroup);
		relationshipBtn.setMaxWidth(Double.MAX_VALUE);
		relationshipLineBtn.setToggleGroup(itemsGroup);
		relationshipLineBtn.setMaxWidth(Double.MAX_VALUE);
		descAttrBtn.setToggleGroup(itemsGroup);
		descAttrBtn.setMaxWidth(Double.MAX_VALUE);
		cutBtn.setToggleGroup(itemsGroup);
		cutBtn.setMaxWidth(Double.MAX_VALUE);
		deleteBtn.setToggleGroup(itemsGroup);
		deleteBtn.setMaxWidth(Double.MAX_VALUE);
		circleBtn.setMaxWidth(Double.MAX_VALUE);
		itemsBox.setAlignment(Pos.BOTTOM_CENTER);
		itemsBox.setVisible(false);
	}
}
