import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class AttributeGraphic extends Label{
	StringProperty _text = new SimpleStringProperty(this, "Attr.", "Attribute");
	BooleanProperty isPrimaryKey = new SimpleBooleanProperty(this, "Pri.", false);
	BooleanProperty isPartialKey = new SimpleBooleanProperty(this, "Part.", false);
	public AttributeGraphic(String attr){
		this();
		_text.set(attr);
	}
	public AttributeGraphic(){
		this.getStyleClass().add("attrGraphic");
		textProperty().bindBidirectional(_text);
		isPrimaryKey.addListener((prop, o, n) -> {
			if(n){
				this.pseudoClassStateChanged(PseudoClass.getPseudoClass("primary"), true);
				if(isPartialKey.get()){
					isPartialKey.set(false);
				}
			}else{
				this.pseudoClassStateChanged(PseudoClass.getPseudoClass("primary"), false);
			}
		});
		
		isPartialKey.addListener((prop, o, n) -> {
			if(n){
				this.pseudoClassStateChanged(PseudoClass.getPseudoClass("partial"), true);
				if(isPrimaryKey.get()){
					isPrimaryKey.set(false);
				}
			}else{
				this.pseudoClassStateChanged(PseudoClass.getPseudoClass("partial"), false);
			}
		});
	}

	public StringProperty _textProperty(){
		return _text;
	}
	
	public BooleanProperty isPrimaryKeyProperty(){
		return this.isPrimaryKey;
	}
	
	public BooleanProperty isPartialKeyProperty(){
		return this.isPartialKey;
	}
}
