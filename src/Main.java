import java.security.PublicKey;

import ecs100.UI;

public class Main {
	public static void main(String[] args) {
		TrainSystemUI ui = new TrainSystemUI();
		UI.initialise();
		UI.setDivider(0.5);
		UI.drawImage(".\\Train network data\\system-map.png", 0, 0, 400, 600);
		UI.addButton("List all Stations", ui::listStations);
		UI.addButton("List all Train Line", ui::listTrainLine);
		UI.addButton("List Train Line by Station", ui::findTrainLineByStation);
		UI.addButton("List Stations by Train Line", ui::findStationByTrainline);
		UI.addButton("Route", ui::route);
		UI.addButton("Train service", ui::trainService);
		UI.addButton("Plan your journey", ui::plan);
		UI.addButton("CLEAR", ui::clear);
	}
}
