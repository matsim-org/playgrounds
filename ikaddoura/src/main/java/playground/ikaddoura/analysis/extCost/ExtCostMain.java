/* *********************************************************************** *
* project: org.matsim.*
* firstControler
* *
* *********************************************************************** *
* *
* copyright : (C) 2007 by the members listed in the COPYING, *
* LICENSE and WARRANTY file. *
* email : info at matsim dot org *
* *
* *********************************************************************** *
* *
* This program is free software; you can redistribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation; either version 2 of the License, or *
* (at your option) any later version. *
* See also COPYING, LICENSE and WARRANTY file *
* *
* *********************************************************************** */ 

package playground.ikaddoura.analysis.extCost;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;

public class ExtCostMain {
	
	private static final Logger log = Logger.getLogger(ExtCostMain.class);
	
	private String eventsFile = "/Users/ihab/Documents/workspace/runs-svn/berlin_internalizationCar/output/internalization_4/ITERS/it.100/100.eventsCongestionPrices.xml.gz";
	private String configFile = "/Users/ihab/Documents/workspace/runs-svn/berlin_internalizationCar/output/internalization_4/output_config.xml.gz";
	private String netFile = "/Users/ihab/Documents/workspace/runs-svn/berlin_internalizationCar/output/internalization_4/output_network.xml.gz";
	private String outputFolder = "/Users/ihab/Desktop/analysis4";
	
	public static void main(String[] args) {
		ExtCostMain anaMain = new ExtCostMain();
		anaMain.run();
	}

	private void run() {
		
		Config config = ConfigUtils.loadConfig(configFile);
		config.network().setInputFile(netFile);
		ScenarioImpl scenario = (ScenarioImpl) ScenarioUtils.loadScenario(config);

		EventsManager events = EventsUtils.createEventsManager();
		
		ExtCostEventHandler handler = new ExtCostEventHandler(scenario, true);
		events.addHandler(handler);

		log.info("Reading events file...");

		MatsimEventsReader reader = new MatsimEventsReader(events);
		reader.readFile(eventsFile);
		
		log.info("Reading events file... Done.");
		
		log.info("Writing output files...");

		TripInfoWriter writer = new TripInfoWriter(handler, outputFolder);
		writer.writeDetailedResults(TransportMode.car);
		writer.writeAvgTollPerDistance(TransportMode.car);
		writer.writeAvgTollPerTimeBin(TransportMode.car);
		writer.writeDetailedResults(TransportMode.pt);
		writer.writeAvgTollPerDistance(TransportMode.pt);
		writer.writeAvgTollPerTimeBin(TransportMode.pt);
		
		log.info("Writing output files... Done.");

	}
			 
}