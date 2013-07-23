/* *********************************************************************** *
 * project: org.matsim.*
 * DgTaSignalModelFactory
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
package playground.dgrether.signalsystems.laemmer.model;

import org.matsim.api.core.v01.Id;
import org.matsim.signalsystems.builder.DefaultSignalModelFactory;
import org.matsim.signalsystems.builder.SignalModelFactory;
import org.matsim.signalsystems.data.signalcontrol.v20.SignalPlanData;
import org.matsim.signalsystems.model.SignalController;
import org.matsim.signalsystems.model.SignalPlan;
import org.matsim.signalsystems.model.SignalSystem;
import org.matsim.signalsystems.model.SignalSystemsManager;

import playground.dgrether.signalsystems.DgSensorManager;


/**
 * @author dgrether
 *
 */
public class LaemmerSignalModelFactory implements SignalModelFactory {

	private DgSensorManager sensorManager;
	private DefaultSignalModelFactory delegate;

	public LaemmerSignalModelFactory(DefaultSignalModelFactory defaultSignalModelFactory,
			DgSensorManager sensorManager) {
		this.delegate = defaultSignalModelFactory;
		this.sensorManager = sensorManager;
	}

	@Override
	public SignalSystemsManager createSignalSystemsManager() {
		return this.delegate.createSignalSystemsManager();
	}

	@Override
	public SignalSystem createSignalSystem(Id id) {
		return this.delegate.createSignalSystem(id);
	}

	@Override
	public SignalController createSignalSystemController(String controllerIdentifier) {
		if (LaemmerSignalController.IDENTIFIER.equals(controllerIdentifier)){
			return new LaemmerSignalController(this.sensorManager);
		}
		return this.delegate.createSignalSystemController(controllerIdentifier);
	}

	@Override
	public SignalPlan createSignalPlan(SignalPlanData planData) {
		return this.delegate.createSignalPlan(planData);
	}

}
