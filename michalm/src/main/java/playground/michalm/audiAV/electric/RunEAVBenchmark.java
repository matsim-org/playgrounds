/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package playground.michalm.audiAV.electric;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dynagent.run.DynQSimModule;
import org.matsim.contrib.taxi.benchmark.*;
import org.matsim.contrib.taxi.data.TaxiData;
import org.matsim.contrib.taxi.run.TaxiConfigGroup;
import org.matsim.core.config.*;
import org.matsim.core.controler.*;

import playground.michalm.ev.*;
import playground.michalm.ev.data.*;
import playground.michalm.ev.data.file.ChargerReader;
import playground.michalm.taxi.data.file.EvrpVehicleReader;
import playground.michalm.taxi.ev.ETaxiTimeProfileCollectorProvider;
import playground.michalm.taxi.run.*;


/**
 * For a fair and consistent benchmarking of taxi dispatching algorithms we assume that link travel
 * times are deterministic. To simulate this property, we remove (1) all other traffic, and (2) link
 * capacity constraints (e.g. by increasing the capacities by 100+ times), as a result all vehicles
 * move with the free-flow speed (which is the effective speed).
 * <p/>
 * To model the impact of traffic, we can use a time-variant network, where we specify different
 * free-flow speeds for each link over time. The default approach is to specify free-flow speeds in
 * each time interval (usually 15 minutes).
 */
public class RunEAVBenchmark
{
    public static void run(String configFile, int runs)
    {
        Config config = ConfigUtils.loadConfig(configFile, new TaxiConfigGroup(),
                new EvConfigGroup());
        createControler(config, runs).run();
    }


    public static Controler createControler(Config config, int runs)
    {
        TaxiConfigGroup taxiCfg = TaxiConfigGroup.get(config);
        EvConfigGroup evCfg = EvConfigGroup.get(config);
        config.addConfigConsistencyChecker(new TaxiBenchmarkConfigConsistencyChecker());
        config.checkConsistency();

        Scenario scenario = RunTaxiBenchmark.loadBenchmarkScenario(config, 15 * 60, 30 * 3600);
        final TaxiData taxiData = new TaxiData();
        new EvrpVehicleReader(scenario.getNetwork(), taxiData).readFile(taxiCfg.getTaxisFile());
        EvData evData = new EvDataImpl();
        new ChargerReader(scenario, evData).readFile(evCfg.getChargerFile());
        EAVUtils.initEvData(taxiData, evData);

        Controler controler = RunTaxiBenchmark.createControler(scenario, taxiData, runs);
        controler.addOverridingModule(new EvModule(evData));
        controler.addOverridingModule(new DynQSimModule<>(ETaxiQSimProvider.class));

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install()
            {
                addMobsimListenerBinding().toProvider(ETaxiTimeProfileCollectorProvider.class);
                //override the binding in RunTaxiBenchmark
                bind(TaxiBenchmarkStats.class).to(ETaxiBenchmarkStats.class).asEagerSingleton();
            }
        });

        return controler;
    }


    public static void main(String[] args)
    {
        String cfg = "../../../runs-svn/avsim_time_variant_network/" + args[0];
        run(cfg, 1);
    }
}
