
import java.util.ArrayList;

import javafx.animation.PathTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import javafx.application.Application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javafx.stage.Stage;

import javafx.util.Duration;


public class Towers extends Application {
	
	private int activeDisk = -1;
	private int activePeg = -1;
	private int stepSpeed = 1000; // in milliseconds
	private double stepSpeedFactor = 1;
	private boolean puzzleDone = false; // is the puzzle solved?
	private Color[] color = new Color[8]; // parallel array for storing disk & text colors 
	private int[] binary = {0,0,0,0,0,0,0,0}; // current binary number
	private ArrayList<ArrayList<Disk>> peg = new ArrayList<ArrayList<Disk>>(); // 2d arraylist holds board state
	
	public void start(Stage primaryStage) {
		String cssBackgroundColor = "-fx-background-color: black;";
		
		AnimationPane animationPane = new AnimationPane();
		
		// Timer code
		EventHandler<ActionEvent> timeHandler = e -> animationPane.stepBinary();
		Timeline mediaAnimation = new Timeline(new KeyFrame(Duration.millis(stepSpeed), timeHandler));
		mediaAnimation.setCycleCount(Timeline.INDEFINITE);
		
		// Feedback label setup
		Label lblUserFeedback = new Label(userMessage());
		lblUserFeedback.setStyle("-fx-font-weight: bold;"
				+ "-fx-font-size: 10;"
				+ "-fx-text-alignment: center;"
				+ "-fx-text-fill: white;"
				+ "-fx-border-color: red;"
				+ "-fx-border-width: 2 2 2 2;");
		
		// Create buttons
		Button btRestart = new Button("   |<<\nRestart");
		btRestart.setOnAction(e -> {
			resetArray();
			animationPane.drawScreen();
		});
		btRestart.setStyle(cssBackgroundColor + buttonStyle());
		
		Button btSlower = new Button("   <<\nSlower");
		btSlower.setOnAction(e -> {
									mediaAnimation.setRate(slowerAnimation());
									lblUserFeedback.setText(userMessage());
								});
		btSlower.setStyle(cssBackgroundColor + buttonStyle());
		
		Button btStop = new Button("   []\nStop");
		btStop.setOnAction(e -> mediaAnimation.pause());
		btStop.setStyle(cssBackgroundColor + buttonStyle());
		
		Button btPlay = new Button("  >\nPlay");
		btPlay.setOnAction(e -> mediaAnimation.play());
		btPlay.setStyle(cssBackgroundColor + buttonStyle());
		
		Button btNextFrame = new Button("  >|\nFrame");
		btNextFrame.setOnAction(e -> animationPane.stepBinary());
		btNextFrame.setStyle(cssBackgroundColor + buttonStyle());
		
		Button btFaster = new Button("   >>\nFaster");
		btFaster.setOnAction(e -> {
									mediaAnimation.setRate(fasterAnimation());
									lblUserFeedback.setText(userMessage());
								});
		btFaster.setStyle(cssBackgroundColor + buttonStyle());
		
		// Group buttons
		HBox userBox = new HBox(10);
		userBox.getChildren().addAll(lblUserFeedback, btRestart, btSlower, btStop, btPlay, btNextFrame, btFaster);
		userBox.setAlignment(Pos.CENTER);
		
		// Place components
		BorderPane pane = new BorderPane();
		pane.setStyle(cssBackgroundColor);
		pane.setTop(userBox);
		pane.setCenter(animationPane);
		
		// Set & display Scene
		Scene scene = new Scene(pane, 500, 300);
		scene.widthProperty().addListener(e -> animationPane.drawScreen());
		scene.heightProperty().addListener(e -> animationPane.drawScreen());
		
		primaryStage.setTitle("Towers of Hanio vs. Binary");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		animationPane.drawScreen();
	}
	
	public String userMessage() {
		String string = "" + stepSpeedFactor + "\nmove";
		
		if (stepSpeedFactor != 1.0)
			string += "s";
		
		string += "\nper second";
		
		return string;
	}
	
	public double fasterAnimation() {
		stepSpeedFactor += 0.5;
		return stepSpeedFactor;
	}
	
	public double slowerAnimation() {
		if (stepSpeedFactor >= 1.0)
			stepSpeedFactor -= 0.5;
		return stepSpeedFactor;
	}
	
	public String buttonStyle() {
		return "-fx-font-weight: bold;"
				+ "-fx-font-size: 12;"
				+ "-fx-text-fill: white;"
				+ "-fx-border-color: white;"
				+ "-fx-border-width: 2 2 2 2;";
	}
	
	class AnimationPane extends Pane {
		
		private double lane1;
		private double lane2;
		private double lane3;
		private double surface;
		
		AnimationPane() {
			//dimensions set upfront for dynamic scaling
			setWidth(500);
			setHeight(300);
			lane1 = getWidth()/4;
			lane2 = getWidth()/2;
			lane3 = getWidth()*3/4;
			surface = getHeight()-50;
			
			resetArray();
			drawScreen();
		}
		
		// this method draws the binary number output and sets the disks on screen
		public void drawScreen() {
			
			lane1 = getWidth()/4;
			lane2 = getWidth()/2;
			lane3 = getWidth()*3/4;
			surface = getHeight()-50;
			
			getChildren().clear(); // clear screen before redrawing
			
			drawBoard(); // board drawn here for proper z-depth
			
			// display binary output
			for (int i=0; i<8; i++) {
				Rectangle binaryBox = new Rectangle(getWidth()/2+90 - (30*i), getHeight()-30, 30, 20);
				binaryBox.setStroke(Color.WHITE);
				
				Text binaryText = new Text(getWidth()/2+99 - (30*i), getHeight()-13, String.valueOf(binary[i]));
				binaryText.setFont(Font.font(null, FontWeight.BOLD, 20));
				binaryText.setFill(color[i]);
				
				if (activeDisk == i)
					binaryText.setFill(Color.YELLOW);
				
				getChildren().addAll(binaryBox, binaryText);
			}
			
			// display disks
			for (int i=0; i<peg.size(); i++)
				for (int j=0; j<peg.get(i).size(); j++) {
					peg.get(i).get(j).setX(centerDiskOnX(peg.get(i).get(j)));
					peg.get(i).get(j).setY(surface - peg.get(i).size()*15 + (j*15));
					peg.get(i).get(j).setWidth(getWidth()/9 + (peg.get(i).get(j).getDiskNumber()*8));
					peg.get(i).get(j).setHeight(peg.get(i).get(j).getHeight());
					peg.get(i).get(j).setFill(color[peg.get(i).get(j).getDiskNumber()]);

					if (peg.get(i).get(j).getDiskNumber() == activeDisk) {
						peg.get(i).get(j).setFill(Color.YELLOW); // override color for active disk
					}
					
					getChildren().add(peg.get(i).get(j));
				}
		}
		
		// this method centers the disk on the peg
		public double centerDiskOnX(Disk disk) {
			return getLane(disk.getPegNumber()) - (disk.getDiskNumber()+1)*4.3 - getWidth()/21 ;
		}

		// this method iterates the binary number, moves the "disk" on the array, and animates the move
		public void stepBinary() {
			
			if (puzzleDone == false) {
				Polyline path = new Polyline();
				PathTransition transition = new PathTransition();
				Point2D point[] = new Point2D[4];
				boolean foundCalculation = false;

				// iterate binary number array
				activeDisk = 0;
				
				while (!foundCalculation) 
					if (binary[activeDisk] == 0) {
						int cascade = activeDisk;
						binary[activeDisk] = 1;
								
						//set all lower order numbers equal to zero
						while (cascade > 0) {
							cascade--;
							binary[cascade] = 0;
						}
						foundCalculation = true;
					}
					else
						activeDisk++;
	
				// match active binary number to peg that needs to move
				for (int i=0; i<peg.size(); i++) 									// for each peg,
					if (!peg.get(i).isEmpty())										// are there any disks?
						if (activeDisk != -1)										// is the activeDisk variable valid?
							if (peg.get(i).get(0).getDiskNumber() == activeDisk) {	// is the top disk the active disk?
								activePeg = i;
								point[0] = new Point2D((peg.get(activePeg).get(0).getX() + peg.get(activePeg).get(0).getWidth()/2), 
										(peg.get(activePeg).get(0).getY() + peg.get(activePeg).get(0).getHeight()/2));
								point[1] = new Point2D((peg.get(activePeg).get(0).getX() + peg.get(activePeg).get(0).getWidth()/2), 
										(getHeight()/2-100));
								transition.setNode(peg.get(activePeg).get(0));
							}
				
				// move disk on array
				Disk newDisk = peg.get(activePeg).get(0); // this variable is strictly for code readability
				
				if (peg.get((activePeg+1)%3).isEmpty()) { 	// is the next peg empty?
					newDisk.setPegNumber((activePeg+1)%3);
					peg.get((activePeg+1)%3).add(0, newDisk);
				} else {									// no?...is this disk bigger than on the next peg?
					if (peg.get((activePeg+1)%3).get(0).getDiskNumber() > peg.get(activePeg).get(0).getDiskNumber()) {
						newDisk.setPegNumber((activePeg+1)%3);
						peg.get((activePeg+1)%3).add(0, newDisk);
					} else {								// no?...try to move it 2 pegs to the right (looping back)
						newDisk.setPegNumber((activePeg+2)%3);
						peg.get((activePeg+2)%3).add(0, newDisk);
					}
				}
				newDisk.setDiskNumber(activeDisk);
				peg.get(activePeg).remove(0);
	
				// 2dpoints used for animation path
				point[2] = new Point2D((getLane(newDisk.getPegNumber())), 
						(getHeight()/2-100));
				point[3] = new Point2D((getLane(newDisk.getPegNumber())), 
						(surface + 7 - peg.get(newDisk.getPegNumber()).size()*newDisk.getHeight()));
				path.getPoints().addAll(new Double[] {
						point[0].getX(), point[0].getY(), 
						point[1].getX(), point[1].getY(), 
						point[2].getX(), point[2].getY(), 
						point[3].getX(), point[3].getY()
						});
				
				// setup and play disk animation
				transition.setDuration(Duration.millis(500 / stepSpeedFactor));
				transition.setPath(path);
				
				transition.play();
				
				drawScreen();
			}
			
			puzzleDone = true;
			for (int i=0; i<binary.length; i++)
				if (binary[i] == 0)
					puzzleDone = false;
		}
		
		// this method gets the peg location from the peg ID number
		public double getLane(int peg) {
			switch (peg) {
				case 0: 
					return lane1;
				case 1:
					return lane2;
				default:
					return lane3;
			}
		}
		
		// this method draws the peg board
		public void drawBoard() {
			Rectangle base = new Rectangle(5 ,surface ,getWidth()-10 ,10);
			base.setFill(Color.RED);
			
			Rectangle pegA = new Rectangle(lane1-15, getHeight()/2-50, 30, getHeight()/2);
			pegA.setFill(Color.RED);
			
			Rectangle pegB = new Rectangle(lane2-15, getHeight()/2-50, 30, getHeight()/2);
			pegB.setFill(Color.RED);
			
			Rectangle pegC = new Rectangle(lane3-15, getHeight()/2-50, 30, getHeight()/2);
			pegC.setFill(Color.RED);
			
			this.getChildren().addAll(base, pegA, pegB, pegC);
		}
	}
	
	// resets & reinitializes array
	public void resetArray() {
		int colorOffset = 0;
		activeDisk = -1;
		activePeg = -1;
		
		for (int i=0; i<binary.length; i++)
			binary[i] = 0;
		
		for (int i=0; i<peg.size(); i++) {
			for (int j=0; j<peg.get(i).size(); j++)
				peg.get(i).clear();
			peg.clear();
		}
		
		for (int i=0; i<3; i++)
			peg.add(new ArrayList<Disk>());
		
		for (int i=0; i<8; i++) {
			color[i] = getColor(colorOffset);
			colorOffset += 15;
			peg.get(0).add(i, new Disk(i));
			peg.get(0).get(i).setHeight(15);
			peg.get(0).get(i).setStroke(Color.BLACK);
		}
	}
	
	// sets offset to color to create gradient for disks
	public Color getColor(int offset) {
		double hue = 330 - offset;
		double saturation = .6;
		double brightness = .6;
		
	    return Color.hsb(hue, saturation, brightness);
	}
	
	class Disk extends Rectangle {
		private int diskNumber;
		private int pegNumber;
		
		Disk() {
			
		}
		
		Disk(Disk d) {
			super(d.getX(), d.getY(), d.getWidth(), d.getHeight());
			this.diskNumber = d.diskNumber;
			this.pegNumber = d.pegNumber;
		}
		
		Disk(int num) {
			this.setDiskNumber(num);
			this.setPegNumber(0);
		}
		
		Disk(int num, int peg, double x, double y, double w, double h) {
			super(x, y, w, h);
			this.setDiskNumber(num);
			this.setPegNumber(peg);
		}

		public int getDiskNumber() {
			return diskNumber;
		}

		public void setDiskNumber(int diskNumber) {
			this.diskNumber = diskNumber;
		}

		public int getPegNumber() {
			return pegNumber;
		}

		public void setPegNumber(int pegLocation) {
			this.pegNumber = pegLocation;
		}
	}
	
	// debug dump because the eclipse debugger stinks
	public void debug() {
		int pegOneNum=-1;
		int pegTwoNum=-1;
		int pegThreeNum=-1;
		
		System.out.println("-----");
		
		if (!peg.get(0).isEmpty())
			pegOneNum = peg.get(0).get(0).getDiskNumber();
		System.out.println("Peg1 top = " + pegOneNum);
		
		if (!peg.get(1).isEmpty())
			pegTwoNum = peg.get(1).get(0).getDiskNumber();
		System.out.println("Peg2 top = " + pegTwoNum);
		
		if (!peg.get(2).isEmpty())
			pegThreeNum = peg.get(2).get(0).getDiskNumber();
		System.out.println("Peg3 top = " + pegThreeNum);
		
		System.out.println("-----");
		System.out.println("arr0 size " + peg.get(0).size());
		System.out.println("arr1 size " + peg.get(1).size());
		System.out.println("arr2 size " + peg.get(2).size());
		System.out.println("-----");
		System.out.println("active peg " + activePeg);
		System.out.println("active index " + activeDisk);
		System.out.println("-----");
		
		for (int i=0; i<peg.size(); i++) {
			System.out.print("arr" + i + ": ");
			for (int j=0; j<peg.get(i).size(); j++)
				System.out.print(" " + peg.get(i).get(j).getDiskNumber());
			System.out.println();
		}
		System.out.println("------");
		
		for (int i=0; i<peg.size(); i++) {
			System.out.print("peg" + i + ": ");
			for (int j=0; j<peg.get(i).size(); j++)
				System.out.print(" " + peg.get(i).get(j).getPegNumber());
			System.out.println();
		}
		System.out.println("------");
		System.out.print("Binary array:");
		for (int i=binary.length-1; i>=0; i--)
			System.out.print(" " + binary[i]);
		System.out.println();
			
		System.out.println("-----------------------------");
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}






















