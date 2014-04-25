package playground.mzilske.cdr;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.router.ActivityWrapperFacility;
import org.matsim.core.router.PlanRouter;
import org.matsim.core.router.TripRouter;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.population.algorithms.ParallelPersonAlgorithmRunner;
import org.matsim.population.algorithms.ParallelPersonAlgorithmRunner.PersonAlgorithmProvider;
import org.matsim.population.algorithms.PersonAlgorithm;
import playground.mzilske.cdr.ZoneTracker.LinkToZoneResolver;
import playground.mzilske.d4d.NetworkRoutingModule;
import playground.mzilske.d4d.Sighting;
import playground.mzilske.d4d.Sightings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;


public class PopulationFromSightings {


    private static Random rnd = MatsimRandom.getRandom();

    public static void createPopulationWithTwoPlansEach(Scenario scenario, LinkToZoneResolver zones, final Map<Id, List<Sighting>> sightings) {
        for (Entry<Id, List<Sighting>> sightingsPerPerson : sightings.entrySet()) {
            Id personId = sightingsPerPerson.getKey();
            List<Sighting> sightingsForThisPerson = sightingsPerPerson.getValue();
            Person person = scenario.getPopulation().getFactory().createPerson(personId);
            Plan plan1 = createPlanWithEndTimeAtLastSighting(scenario, zones,
                    sightingsForThisPerson);
            person.addPlan(plan1);
            Plan plan2 = createPlanWithEndTimeAtNextSightingElsewhere(scenario, zones,
                    sightingsForThisPerson);
            person.addPlan(plan2);
            scenario.getPopulation().addPerson(person);
        }
    }

    public static void createPopulationWithEndTimesAtLastSightings(Scenario scenario, LinkToZoneResolver zones, final Map<Id, List<Sighting>> sightings) {
        for (Entry<Id, List<Sighting>> sightingsPerPerson : sightings.entrySet()) {
            Id personId = sightingsPerPerson.getKey();
            List<Sighting> sightingsForThisPerson = sightingsPerPerson.getValue();
            Person person = scenario.getPopulation().getFactory().createPerson(personId);
            Plan plan1 = createPlanWithEndTimeAtLastSighting(scenario, zones,
                    sightingsForThisPerson);
            person.addPlan(plan1);

            scenario.getPopulation().addPerson(person);
        }
    }

    public static void createPopulationWithEndTimesAtLastSightingsAndStayAtHomePlan(Scenario scenario, LinkToZoneResolver zones, final Map<Id, List<Sighting>> sightings) {
        for (Entry<Id, List<Sighting>> sightingsPerPerson : sightings.entrySet()) {
            Id personId = sightingsPerPerson.getKey();
            List<Sighting> sightingsForThisPerson = sightingsPerPerson.getValue();
            Person person = scenario.getPopulation().getFactory().createPerson(personId);
            Plan plan1 = createPlanWithEndTimeAtLastSighting(scenario, zones,
                    sightingsForThisPerson);
            person.addPlan(plan1);
            Plan plan2 = scenario.getPopulation().getFactory().createPlan();
            person.addPlan(plan2);
            person.setSelectedPlan(plan1);
            scenario.getPopulation().addPerson(person);
        }
    }

    public static void createPopulationWithEndTimesAtLastSightingsAndAdditionalInflationPopulation(Scenario scenario, LinkToZoneResolver zones, final Map<Id, List<Sighting>> sightings) {
        int count = 1;
        for (Entry<Id, List<Sighting>> sightingsPerPerson : sightings.entrySet()) {
            Id personId = sightingsPerPerson.getKey();
            List<Sighting> sightingsForThisPerson = sightingsPerPerson.getValue();
            Person person = scenario.getPopulation().getFactory().createPerson(personId);
            Plan plan1 = createPlanWithEndTimeAtLastSighting(scenario, zones,
                    sightingsForThisPerson);
            person.addPlan(plan1);
            Plan plan2 = scenario.getPopulation().getFactory().createPlan();
            person.addPlan(plan2);


            scenario.getPopulation().addPerson(person);
        }
        for (int i=0; i<count-1; i++) {
            for (Entry<Id, List<Sighting>> sightingsPerPerson : sightings.entrySet()) {
                Id personId = new IdImpl("I"+i+"_" + sightingsPerPerson.getKey().toString());
                List<Sighting> sightingsForThisPerson = sightingsPerPerson.getValue();
                Person person = scenario.getPopulation().getFactory().createPerson(personId);
                Plan plan1 = createPlanWithEndTimeAtLastSighting(scenario, zones,
                        sightingsForThisPerson);
                person.addPlan(plan1);
                Plan plan2 = scenario.getPopulation().getFactory().createPlan();
                person.addPlan(plan2);

                scenario.getPopulation().addPerson(person);
            }
        }
    }

    public static Plan createPlanWithEndTimeAtLastSighting(Scenario scenario,
                                                           LinkToZoneResolver zones, List<Sighting> sightingsForThisPerson) {
        Plan plan = scenario.getPopulation().getFactory().createPlan();
        boolean first = true;
        Map<Activity, String> cellsOfSightings;
        cellsOfSightings = new HashMap<Activity, String>();
        for (Sighting sighting : sightingsForThisPerson) {
            String zoneId = sighting.getCellTowerId();
            Activity activity = createActivityInZone(scenario, zones,
                    zoneId);
            cellsOfSightings.put(activity, zoneId);
            activity.setEndTime(sighting.getTime());
            if (first) {
                plan.addActivity(activity);
                first = false;
            } else {
                Activity lastActivity = (Activity) plan.getPlanElements().get(plan.getPlanElements().size()-1);
                if ( !(zoneId.equals(cellsOfSightings.get(lastActivity))) ) {
                    Leg leg = scenario.getPopulation().getFactory().createLeg("unknown");
                    plan.addLeg(leg);
                    plan.addActivity(activity);
                } else {
                    lastActivity.setEndTime(sighting.getTime());
                }
            }
        }
        return plan;
    }


    public static Plan createPlanWithEndTimeAtNextSightingElsewhere(Scenario scenario,
                                                                    LinkToZoneResolver zones, List<Sighting> sightingsForThisPerson) {
        Plan plan = scenario.getPopulation().getFactory().createPlan();
        boolean first = true;
        Map<Activity, String> cellsOfSightings;
        cellsOfSightings = new HashMap<Activity, String>();
        for (Sighting sighting : sightingsForThisPerson) {
            String zoneId = sighting.getCellTowerId();
            Activity activity = createActivityInZone(scenario, zones,
                    zoneId);
            cellsOfSightings.put(activity, zoneId);
            activity.setEndTime(sighting.getTime());
            if (first) {
                plan.addActivity(activity);
                first = false;
            } else {
                Activity lastActivity = (Activity) plan.getPlanElements().get(plan.getPlanElements().size()-1);
                if ( !(zoneId.equals(cellsOfSightings.get(lastActivity))) ) {
                    Leg leg = scenario.getPopulation().getFactory().createLeg("unknown");
                    plan.addLeg(leg);
                    plan.addActivity(activity);
                    TripRouter tripRouter = new TripRouter();
                    tripRouter.setRoutingModule("unknown", new NetworkRoutingModule(scenario.getPopulation().getFactory(), (NetworkImpl) scenario.getNetwork(), new FreeSpeedTravelTime()));
                    List<? extends PlanElement> route = tripRouter.calcRoute("unknown", new ActivityWrapperFacility(lastActivity), new ActivityWrapperFacility(activity), sighting.getTime(), null);
                    double travelTime = ((Leg) route.get(0)).getTravelTime();
                    lastActivity.setEndTime(sighting.getTime() - travelTime);
                } else {
                    lastActivity.setEndTime(sighting.getTime());
                }
            }
        }
        return plan;
    }


    public static Activity createActivityInZone(Scenario scenario, LinkToZoneResolver zones, String zoneId) {
        Activity activity = scenario.getPopulation().getFactory().createActivityFromLinkId("sighting", zones.chooseLinkInZone(zoneId));
        return activity;
    }



    public static void preparePopulation(final ScenarioImpl scenario, final LinkToZoneResolver linkToZoneResolver2, final Map<Id, List<Sighting>> allSightings) {
        ParallelPersonAlgorithmRunner.run(scenario.getPopulation(), 8, new org.matsim.population.algorithms.XY2Links(scenario));
        ParallelPersonAlgorithmRunner.run(scenario.getPopulation(), 8, new PersonAlgorithmProvider() {

            @Override
            public PersonAlgorithm getPersonAlgorithm() {
                TripRouter tripRouter = new TripRouter();
                tripRouter.setRoutingModule("unknown", new NetworkRoutingModule(scenario.getPopulation().getFactory(), (NetworkImpl) scenario.getNetwork(), new FreeSpeedTravelTime()));
                return new PlanRouter(tripRouter);
            }

        });

        Population unfeasiblePeople = new PopulationImpl(scenario);

        for (int i=0; i<0; i++) {
            unfeasiblePeople = new PopulationImpl(scenario);
            for (Person person : scenario.getPopulation().getPersons().values()) {
                Plan plan = person.getSelectedPlan();
                if (!isFeasible(plan)) {
                    unfeasiblePeople.addPerson(person);
                }
            }
            System.out.println("Unfeasible plans: " + unfeasiblePeople.getPersons().size() + " of " +scenario.getPopulation().getPersons().size());

            ParallelPersonAlgorithmRunner.run(unfeasiblePeople, 8, new PersonAlgorithm() {

                @Override
                public void run(Person person) {
                    Sightings sightingsForThisAgent = new Sightings(allSightings.get(person.getId()));
                    for (PlanElement planElement : person.getSelectedPlan().getPlanElements()) {
                        if (planElement instanceof Activity) {
                            Sighting sighting = sightingsForThisAgent.sightings.next();
                            ActivityImpl activity = (ActivityImpl) planElement;
                            activity.setLinkId(linkToZoneResolver2.chooseLinkInZone(sighting.getCellTowerId()));
                        }
                    }
                }

            });

            ParallelPersonAlgorithmRunner.run(unfeasiblePeople, 8, new org.matsim.population.algorithms.XY2Links(scenario));


            ParallelPersonAlgorithmRunner.run(unfeasiblePeople, 8, new PersonAlgorithmProvider() {

                @Override
                public PersonAlgorithm getPersonAlgorithm() {
                    TripRouter tripRouter = new TripRouter();
                    tripRouter.setRoutingModule("car", new NetworkRoutingModule(scenario.getPopulation().getFactory(), (NetworkImpl) scenario.getNetwork(), new FreeSpeedTravelTime()));
                    return new PlanRouter(tripRouter);
                }

            });

//			ParallelPersonAlgorithmRunner.run(unfeasiblePeople, 8, new PersonAlgorithmProvider() {
//				
//				@Override
//				public PersonAlgorithm getPersonAlgorithm() {
//					TripRouter tripRouter = new TripRouter();
//					tripRouter.setRoutingModule("unknown", new BushwhackingRoutingModule(scenario.getPopulation().getFactory(), (NetworkImpl) scenario.getNetwork()));
//					return new PlanRouter(tripRouter);
//				}
//	
//			});

        }

        for (Person person : unfeasiblePeople.getPersons().values()) {
            ((PopulationImpl) scenario.getPopulation()).getPersons().remove(person.getId());
        }
    }


    private static Point getRandomPointInFeature(Random rnd, Geometry ft) {
        Point p = null;
        double x, y;
        do {
            x = ft.getEnvelopeInternal().getMinX() + rnd.nextDouble() * (ft.getEnvelopeInternal().getMaxX() - ft.getEnvelopeInternal().getMinX());
            y = ft.getEnvelopeInternal().getMinY() + rnd.nextDouble() * (ft.getEnvelopeInternal().getMaxY() - ft.getEnvelopeInternal().getMinY());
            p = MGC.xy2Point(x, y);
        } while (!ft.contains(p));
        return p;
    }

    private static boolean isFeasible(Plan plan) {
        double currentTime = 0.0;
        for (PlanElement planElement : plan.getPlanElements()) {
            if (planElement instanceof Leg) {
                LegImpl leg = (LegImpl) planElement;
                double arrivalTime = leg.getArrivalTime();
                currentTime = arrivalTime;
            } else if (planElement instanceof Activity) {
                ActivityImpl activity = (ActivityImpl) planElement;
                double sightingTime = activity.getEndTime();
                if (sightingTime < currentTime) {
                    return false;
                }
            }
        }
        return true;
    }

}
