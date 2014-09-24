/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package playground.johannes.gsv.synPop.invermo.sim;

import playground.johannes.gsv.synPop.ActivityType;
import playground.johannes.gsv.synPop.CommonKeys;
import playground.johannes.gsv.synPop.ProxyObject;
import playground.johannes.gsv.synPop.ProxyPerson;
import playground.johannes.gsv.synPop.ProxyPlan;
import playground.johannes.gsv.synPop.sim.Initializer;

/**
 * @author johannes
 *
 */
public class InitializeStartLocation implements Initializer {

	@Override
	public void init(ProxyPerson person) {
		ProxyPlan plan = person.getPlans().get(0);

		boolean hasHomeAct = false;
		
		for(ProxyObject act : plan.getActivities()) {
			if(ActivityType.HOME.equalsIgnoreCase(act.getAttribute(CommonKeys.ACTIVITY_TYPE))) {
				act.setUserData(MutateStartLocation.START_FACILITY_KEY, true);
				hasHomeAct = true;
			}
		}
		
		if(!hasHomeAct) {
			plan.getActivities().get(0).setUserData(MutateStartLocation.START_FACILITY_KEY, true);
		}
		

	}

	
}
