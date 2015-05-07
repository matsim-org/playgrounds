/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package playground.ikaddoura.minibusWelfare;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.minibus.PConfigGroup;
import org.matsim.contrib.minibus.hook.PModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * @author ikaddoura, dhosse
 *
 */
public class MinibusControler {

	private static String configFile = "/Users/ihab/Documents/workspace/shared-svn/projects/paratransit/paratransitWelfareExample/config.xml";

	public static void main(String[] args) {
		
		Config config = ConfigUtils.loadConfig( configFile, new PConfigGroup() ) ;

		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		Controler controler = new Controler(scenario);
		controler.getConfig().controler().setCreateGraphs(false);
		controler.setOverwriteFiles(true);

		PModule builder = new PModule() ;
		builder.configureControler(controler);

		controler.run();
	}

}
