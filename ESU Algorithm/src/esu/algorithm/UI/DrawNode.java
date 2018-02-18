/*
 * DrawNode
 */
package esu.algorithm.UI;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author BioHazard
 */
public class DrawNode extends Group{
    int r,g,b = (int)(Math.random()*256);
    int raduis = 30;
    final Font font = new Font("ubnutu", 17);
    /**
     * 
     * @param data 
     */
    public DrawNode(int data){
        Circle c = new Circle(raduis,Color.rgb(r, g, b)) ;
	Label nodeDataContent = new Label(data+"");
	nodeDataContent.setTextFill(Color.WHITE);
	nodeDataContent.setFont(font);
	nodeDataContent.setPrefSize(raduis*2, raduis*2);
	nodeDataContent.setTranslateX(-raduis);
	nodeDataContent.setTranslateY(-raduis);
	nodeDataContent.setTextAlignment(TextAlignment.CENTER);
	nodeDataContent.setAlignment(Pos.CENTER);
	getChildren().addAll(c,nodeDataContent);
    }
    
    
}
