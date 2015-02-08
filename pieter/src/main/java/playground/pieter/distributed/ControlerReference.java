package playground.pieter.distributed;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.locationchoice.DestinationChoiceConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.facilities.MatsimFacilitiesReader;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV1;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.vehicles.VehicleReaderV1;
import playground.pieter.distributed.listeners.controler.GenomeAnalysis;
import playground.pieter.distributed.plans.PopulationFactoryForPlanGenomes;
import playground.pieter.distributed.plans.PopulationReaderMatsimV5ForPlanGenomes;
import playground.pieter.distributed.plans.PopulationUtilsForPlanGenomes;
import playground.pieter.distributed.plans.router.DefaultTripRouterFactoryForPlanGenomesModule;
import playground.pieter.distributed.replanning.DistributedPlanStrategyTranslationAndRegistration;
import playground.singapore.scoring.CharyparNagelOpenTimesScoringFunctionFactory;
import playground.singapore.transitRouterEventsBased.TransitRouterEventsWSFactory;
import playground.singapore.transitRouterEventsBased.stopStopTimes.StopStopTimeCalculatorSerializable;
import playground.singapore.transitRouterEventsBased.waitTimes.WaitTimeCalculatorSerializable;
import playground.vsp.randomizedtransitrouter.RandomizedTransitRouterModule;

public class ControlerReference {
    private final Controler delegate;
    private Logger logger = Logger.getLogger(this.getClass());

    private ControlerReference(String[] args) throws ParseException {
        System.setProperty("matsim.preferLocalDtds", "true");
        Options options = new Options();
        options.addOption("c", true, "Config file location");
        options.addOption("s", false, "Switch to indicate if this is the Singapore scenario, i.e. special scoring function");
        options.addOption("g", false, "Track plan genomes");
        options.addOption("x", false, "Intelligent routers");
        CommandLineParser parser = new BasicParser();
        CommandLine commandLine = parser.parse(options, args);


        Config config = ConfigUtils.loadConfig(commandLine.getOptionValue("c"), new DestinationChoiceConfigGroup());

        boolean trackGenome = commandLine.hasOption("g");
        boolean singapore = commandLine.hasOption("s");
        if (trackGenome) {

            DistributedPlanStrategyTranslationAndRegistration.TrackGenome = true;
            DistributedPlanStrategyTranslationAndRegistration.substituteStrategies(config, false, 1);
        }
        Scenario scenario = ScenarioUtilsForPlanGenomes.buildAndLoadScenario(config, trackGenome, false);
        this.delegate = new Controler(scenario);
        if (commandLine.hasOption("g")) {
            new DistributedPlanStrategyTranslationAndRegistration(this.delegate, null, false, 1);
        }
        delegate.setOverwriteFiles(true);

        if (singapore) {
            logger.warn("Singapore scoring function");
            delegate.setScoringFunctionFactory(new CharyparNagelOpenTimesScoringFunctionFactory(delegate.getConfig().planCalcScore(), delegate.getScenario()));
            config.controler().setOutputDirectory(config.controler().getOutputDirectory() + "_SG");
//        for (Link link : scenario.getNetwork().getLinks().values()) {
//            Set<String> modes = new HashSet<>(link.getAllowedModes());
//            modes.add("pt");
//            link.setAllowedModes(modes);
//        }
            //this is some more magic hacking to get location choice by car to work, by sergioo
            //sergioo creates a car-only network, then associates each activity and facility with a car link.
//        Set<String> carMode = new HashSet<>();
//        carMode.add("car");
//        NetworkImpl justCarNetwork = NetworkImpl.createNetwork();
//        new TransportModeNetworkFilter(scenario.getNetwork()).filter(justCarNetwork, carMode);
//        for (Person person : scenario.getPopulation().getPersons().values())
//            for (PlanElement planElement : person.getSelectedPlan().getPlanElements())
//                if (planElement instanceof Activity)
//                    ((ActivityImpl) planElement).setLinkId(justCarNetwork.getNearestLinkExactly(((ActivityImpl) planElement).getCoord()).getId());
//        for (ActivityFacility facility : scenario.getActivityFacilities().getFacilities().values())
//            ((ActivityFacilityImpl) facility).setLinkId(justCarNetwork.getNearestLinkExactly(facility.getCoord()).getId());

//        delegate.addPlanStrategyFactory("TransitLocationChoice", new PlanStrategyFactory() {
//            @Override
//            public PlanStrategy createPlanStrategy(Scenario scenario, EventsManager eventsManager) {
//                return new TransitLocationChoiceStrategy(scenario);
//            }
//        });
//        delegate.setMobsimFactory(new PTQSimFactory());
        }
        if (commandLine.hasOption("x")) {
            logger.warn("Smart routing");
            StopStopTimeCalculatorSerializable stopStopTimes = new StopStopTimeCalculatorSerializable(scenario.getTransitSchedule(),
                    config.travelTimeCalculator().getTraveltimeBinSize(), (int) (config
                    .qsim().getEndTime() - config.qsim().getStartTime()));

            WaitTimeCalculatorSerializable waitTimes = new WaitTimeCalculatorSerializable(scenario.getTransitSchedule(),
                    config.travelTimeCalculator().getTraveltimeBinSize(), (int) (config
                    .qsim().getEndTime() - config.qsim().getStartTime()));
            delegate.getEvents().addHandler(waitTimes);
            delegate.getEvents().addHandler(stopStopTimes);
            delegate.setTransitRouterFactory(new TransitRouterEventsWSFactory(delegate.getScenario(), waitTimes.getWaitTimes(), stopStopTimes.getStopStopTimes()));
        } else {

            delegate.addOverridingModule(new RandomizedTransitRouterModule());
        }
        if (trackGenome) {

            delegate.addOverridingModule(new DefaultTripRouterFactoryForPlanGenomesModule());
            delegate.addControlerListener(new GenomeAnalysis(false,false));
        }
        String outputDirectory = config.controler().getOutputDirectory();
        outputDirectory += "_ref" +
                (commandLine.hasOption("g") ? "_g" : "") +
                (commandLine.hasOption("s") ? "_s" : "") +
                (commandLine.hasOption("x") ? "_x" : "");
        config.controler().setOutputDirectory(outputDirectory);
    }

    public static void main(String args[]) throws ParseException {
        new ControlerReference(args).run();
    }

    private void run() {
        delegate.run();

    }

}
