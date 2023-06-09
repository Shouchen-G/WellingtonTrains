import java.awt.FocusTraversalPolicy;
import java.awt.TextField;
import java.awt.font.TextLayout;
import java.io.File;
import java.net.Inet4Address;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ecs100.UI;

public class TrainSystemUI {
	private Map<String, Station> stations = new HashMap<String, Station>();
	private Map<String, TrainLine> trainLines = new HashMap<String, TrainLine>();
	private Map<Integer, Double> fares = new HashMap<Integer, Double>();

	public TrainSystemUI() {
		try {
			Scanner scanStation = new Scanner(new File(".\\Train network data\\stations.data"));
			while (scanStation.hasNext()) {
				String stationName = scanStation.next();
				int zone = scanStation.nextInt();
				double distance = scanStation.nextDouble();
				Station s = new Station(stationName, zone, distance);
				stations.put(stationName, s);
			}
			scanStation.close();

			Scanner scanTrainLine = new Scanner(new File(".\\Train network data\\train-lines.data"));
			while (scanTrainLine.hasNext()) {
				String t = scanTrainLine.nextLine();
				TrainLine trainLine = new TrainLine(t);
				trainLines.put(t, trainLine);

				Scanner scanTrainLineStations = new Scanner(new File(".\\Train network data\\" + t + "-stations.data"));
				while (scanTrainLineStations.hasNext()) {
					String s = scanTrainLineStations.next();
					Station station = this.stations.get(s);
					trainLine.addStation(station);
					station.addTrainLine(trainLine);
				}
				scanTrainLineStations.close();

				Scanner scanTrainService = new Scanner(new File(".\\Train network data\\" + t + "-services.data"));
				while (scanTrainService.hasNext()) {
					TrainService trainService = new TrainService(trainLine);
					for (int i = 0; i < trainLine.getStations().size(); i++) {
						int time = scanTrainService.nextInt();
						trainService.addTime(time, i == 0);
					}
					trainLine.addTrainService(trainService);
				}
				scanTrainService.close();
			}
			scanTrainLine.close();

			Scanner sf = new Scanner(new File(".\\Train network data\\fares.data"));
			sf.nextLine();
			while (sf.hasNext()) {
				int zone = sf.nextInt();
				Double fare = sf.nextDouble();
				sf.nextLine();
				fares.put(zone, fare);
			}
			sf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void listStations() {
		UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		UI.printf("%25s", "STATION NAME");
		UI.println();
		UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		for (Map.Entry<String, Station> entry : stations.entrySet()) {
			UI.printf("%25s\n", entry.getValue().getName());
		}
		UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

	public void listTrainLine() {
		UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		UI.printf("%25s", "TRAIN LINE");
		UI.println();
		UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		for (Map.Entry<String, TrainLine> entry : trainLines.entrySet()) {
			UI.printf("%28s\n", entry.getValue().getName());
		}
		UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

	public void findTrainLineByStation() {
		UI.clearText();
		String s = this.stationChooser("Station Name");
		if (stations.containsKey(s)) {
			UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			UI.printf("%25s", "TRAIN LINE");
			UI.println();
			UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			for (TrainLine l : this.trainLineByStation(s)) {
				UI.printf("%28s\n", l.getName());
			}
			UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		} else {
			UI.println("The Station doesn't exist. Please check your spell");
		}
	}

	public void findStationByTrainline() {
		UI.clearText();
		String s = this.trainLineChooser("Train Line");
		if (trainLines.containsKey(s)) {
			UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			UI.printf("%25s", "STATION NAME");
			UI.println();
			UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			for (Station st : stationByTrailine(s)) {
				UI.printf("%25s\n", st.getName());
			}
		} else {
			UI.println("The Train Line doesn't exist. Please check your spell");
		}
	}

	public Set<TrainLine> trainLineByStation(String station) {
		return stations.get(station).getTrainLines();
	}

	public List<Station> stationByTrailine(String trainline) {
		return trainLines.get(trainline).getStations();
	}

	public void route() {
		UI.clearText();
		String sta = stationChooser("From");
		String dest = stationChooser("To");
		Station start = this.stations.get(sta);
		Station end = this.stations.get(dest);
		if (stations.containsValue(start) && stations.containsValue(end)) {
			Set<TrainLine> trainLine = this.findTrainLine(start, end);
			if (trainLine != null && (!trainLine.isEmpty())) {
				UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				UI.printf("%25s", "TRAIN LINE");
				UI.println();
				UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				for (TrainLine tl : trainLine) {
					UI.printf("%28s\n", tl.getName());
					UI.println();
				}
				UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			} else {
				UI.println("Sorry, there are no services between these two stations");
			}

		} else {
			UI.println("Invalid station, please check your spell");
		}
	}

	public Set<TrainLine> findTrainLine(Station start, Station end) {
		Set<TrainLine> l = this.trainLineByStation(start.getName());
		Set<TrainLine> trainLine = new HashSet<TrainLine>();
		for (TrainLine tl : l) {
			if (tl.getStations().contains(start) && tl.getStations().contains(end)) {
				if (tl.getStations().indexOf(start) < tl.getStations().indexOf(end)) {
					trainLine.add(tl);
				}
			}
		}
		return trainLine;
	}

	public void trainService() {
		UI.clearText();
		String s = stationChooser("Station Name");
		Station station = stations.get(s);
		if (stations.containsValue(station)) {
			int i = UI.askInt("Please enter the time (e.g.2050 for 8:50pm)");
			UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			UI.printf("%25s %20s", "TRAIN LINE", "NEXT SERVICE");
			UI.println();
			UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			Set<TrainLine> t = this.trainLineByStation(s);
			for (TrainLine tl : t) {
				int index = tl.getStations().indexOf(station);
				for (TrainService ts : tl.getTrainServices()) {
					if (ts.getTimes().get(index) > i) {
						UI.printf("%25s %20s\n", tl.getName(), ts.getTimes().get(index));
						UI.println();
						break;
					}
				}
			}
			UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		} else {
			UI.println("Invalid station, please check your spell");
		}

	}

	public List<TrainService> nextServices(Station start, Station end, Integer i) {
		Set<TrainLine> trainLine = this.findTrainLine(start, end);
		List<TrainService> trainServices = new ArrayList<TrainService>();
		for (TrainLine tl : trainLine) {
			int ss = tl.getStations().indexOf(start);
			int es = tl.getStations().indexOf(end);
			for (TrainService ts : tl.getTrainServices()) {
				if (ts.getTimes().get(ss) > i && ts.getTimes().get(es) != -1) {
					trainServices.add(ts);
					break;
				}
			}
		}
		return trainServices;
	}

	public Set<Station> exchangeStation(String sta, String dest) {
		Set<TrainLine> startTl = trainLineByStation(sta);
		Set<TrainLine> endTl = trainLineByStation(dest);
		Set<Station> startStations = new HashSet<Station>();
		Set<Station> endStations = new HashSet<Station>();
		Set<Station> exchangeStations = new HashSet<Station>();
		for (TrainLine t1 : startTl) {
			for (Station s1 : t1.getStations()) {
				startStations.add(s1);
			}
		}
		for (TrainLine t2 : endTl) {
			for (Station s2 : t2.getStations()) {
				endStations.add(s2);
			}
		}
		for (Station s1 : startStations) {
			for (Station s2 : endStations) {
				if (s1.equals(s2)) {
					exchangeStations.add(s2);
				}
			}
		}
		return exchangeStations;
	}

	public String stationChooser(String string) {
		ArrayList<String> stations = new ArrayList<String>(this.stations.keySet());
		Collections.sort(stations);
		String selectedName = (String) JOptionPane.showInputDialog(null, string, "All the stations in the region",
				JOptionPane.QUESTION_MESSAGE, null, stations.toArray(), stations.get(0));
		return selectedName;
	}

	public String trainLineChooser(String string) {
		ArrayList<String> tainlines = new ArrayList<String>(this.trainLines.keySet());
		Collections.sort(tainlines);
		String selectedName = (String) JOptionPane.showInputDialog(null, string, "All the train lines in the region",
				JOptionPane.QUESTION_MESSAGE, null, tainlines.toArray(), tainlines.get(0));
		return selectedName;
	}

	public void plan() {
		UI.clearText();
		String sta = stationChooser("From");
		String dest = stationChooser("To");
		Station start = this.stations.get(sta);
		Station end = this.stations.get(dest);
		int zone = (Math.abs(start.getZone() - end.getZone())) + 1;
		Set<TrainLine> trainLines = this.findTrainLine(start, end);
		Double fare = fares.get(zone);
		int i = UI.askInt("Please enter the time (e.g.2050 for 8:50pm)");
		if (trainLines != null && !(trainLines.isEmpty())) {
			List<TrainService> trainServices = this.nextServices(start, end, i);
			UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			UI.printf("%-20s %15s %8s %8s\n", "TRAIN SERVICE", "SCHEDULED", "ARRIVAL ", " FARES");
			UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			if (trainServices != null && !(trainServices.isEmpty())) {
				for (TrainService ts : trainServices) {
					int ss = ts.getTrainLine().getStations().indexOf(start);
					int es = ts.getTrainLine().getStations().indexOf(end);
					UI.printf("%-30s %5s %5s %5s\n", ts.getTrainID(), ts.getTimes().get(ss), ts.getTimes().get(es),
							" $" + fare + "(" + zone + " zones" + ")");
					UI.println();
				}
				UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			} else {
				UI.printf("%30s", "NO SERVICE");
				UI.println();
			}

		} else {
			Set<Station> transfer = this.exchangeStation(sta, dest);
			for (Station s : transfer) {
				String key = s.getName();
				if (transfer.size() == 1) {
					Station es = stations.get(key);
					List<TrainService> transferServices = this.nextServices(start, es, i);
					UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					UI.printf("%-20s %15s %8s %8s\n", "TRAIN SERVICE", "SCHEDULED", "ARRIVAL ", " FARES");
					UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					for (TrainService ts : transferServices) {
						int ss = ts.getTrainLine().getStations().indexOf(start);
						int exs = ts.getTrainLine().getStations().indexOf(es);
						UI.printf("%-30s %5s %5s %5s\n", ts.getTrainID(), ts.getTimes().get(ss), ts.getTimes().get(exs),
								" $" + fare + "(" + zone + " zones" + ")");
						UI.println();
						int transit = ts.getTimes().get(exs);
						List<TrainService> toDest = this.nextServices(es, end, transit);
						UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
						UI.printf("%-20s %15s %8s %8s\n", "TRAIN SERVICE", "SCHEDULED", "ARRIVAL ", " FARES");
						UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
						for (TrainService d : toDest) {
							int e1 = d.getTrainLine().getStations().indexOf(es);
							int e2 = d.getTrainLine().getStations().indexOf(end);
							UI.printf("%-30s %5s %5s %5s\n", d.getTrainID(), d.getTimes().get(e1), d.getTimes().get(e2),
									" $" + fare + "(" + zone + " zones" + ")");
							UI.println();
						}
					}
					UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				} else if (transfer != null && !(transfer.isEmpty())) {
					UI.println("Here are the list of exchange stations that you can transfer");
					UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					UI.printf("%25s", "STATION NAME");
					UI.println();
					UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					UI.printf("%25s\n", key);
					UI.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				} else {
					UI.println("Sorry. No services found");
				}
			}
		}
	}

	public void clear() {
		UI.clearText();
	}
}
