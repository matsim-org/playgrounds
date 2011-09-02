/* *********************************************************************** *
 * project: org.matsim.*
 * HomeFacilityGenerator.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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
package playground.johannes.studies.coopsim;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.facilities.ActivityFacilitiesImpl;
import org.matsim.core.facilities.ActivityFacilityImpl;
import org.matsim.core.network.NetworkImpl;

import playground.johannes.socialnetworks.graph.social.SocialGraph;
import playground.johannes.socialnetworks.graph.social.SocialVertex;
import playground.johannes.socialnetworks.sim.gis.MatsimCoordUtils;

/**
 * @author illenberger
 *
 */
public class HomeFacilityGenerator {

	public static final String HOME_PREFIX = "home";
	
	public static void generate(ActivityFacilitiesImpl facilities, NetworkImpl network, SocialGraph graph) {
		for(SocialVertex v : graph.getVertices()) {
			Person person = v.getPerson().getPerson();
			
			Id id = new IdImpl(HOME_PREFIX + person.getId().toString());
			ActivityFacilityImpl homeFac = facilities.createFacility(id, MatsimCoordUtils.pointToCoord(v.getPoint()));
			Link link = network.getNearestLink(homeFac.getCoord());
			homeFac.setLinkId(link.getId());
		}
	}
	
}
