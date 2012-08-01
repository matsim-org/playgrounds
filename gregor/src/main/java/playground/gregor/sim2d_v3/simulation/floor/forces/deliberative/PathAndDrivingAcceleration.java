package playground.gregor.sim2d_v3.simulation.floor.forces.deliberative;

import java.util.Collection;
import java.util.HashMap;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.geotools.MGC;

import playground.gregor.sim2d_v3.config.Sim2DConfigGroup;
import playground.gregor.sim2d_v3.scenario.MyDataContainer;
import playground.gregor.sim2d_v3.simulation.floor.Agent2D;
import playground.gregor.sim2d_v3.simulation.floor.PhysicalAgentRepresentation;
import playground.gregor.sim2d_v3.simulation.floor.PhysicalFloor;
import playground.gregor.sim2d_v3.simulation.floor.forces.deliberative.velocityobstacle.Algorithms;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class PathAndDrivingAcceleration {

	private final PhysicalFloor floor;
	private HashMap<Id, LinkInfo> linkGeos;
	private HashMap<Id, Coordinate> drivingDirections;
	private final double tau;
	private final Scenario sc;

	GeometryFactory geofac = new GeometryFactory();


	private static final double COS_RIGHT = Math.cos(-Math.PI / 2);
	private static final double SIN_RIGHT = Math.sin(-Math.PI / 2);

	// Mauron constant
	private static final double Apath =50;



	public PathAndDrivingAcceleration(PhysicalFloor floor, Scenario sc) {
		this.floor = floor;
		this.tau = ((Sim2DConfigGroup)sc.getConfig().getModule("sim2d")).getTau();
		this.sc = sc;

		init();
	}

	public void init() {
		this.linkGeos = new HashMap<Id, LinkInfo>();

		for (Link link : this.floor.getLinks()) {
			Coordinate from = MGC.coord2Coordinate(link.getFromNode().getCoord());
			Coordinate to = MGC.coord2Coordinate(link.getToNode().getCoord());
			Coordinate c = new Coordinate(to.x - from.x, to.y - from.y);
			double length = Math.sqrt(Math.pow(c.x, 2) + Math.pow(c.y, 2));
			c.x /= length;
			c.y /= length;
			Coordinate perpendicularVec = new Coordinate(COS_RIGHT * c.x + SIN_RIGHT * c.y, -SIN_RIGHT * c.x + COS_RIGHT * c.y);

			double minWidth = getMinWidth(this.geofac.createLineString(new Coordinate[]{from,to}),link.getCoord());
			LinkInfo li = new LinkInfo();
//			li.pathWidth = minWidth;
			li.pathWidth = link.getCapacity()/1.33;
			li.c0 = from;
			li.c1 = to;
			li.perpendicularVector = perpendicularVec;
			li.length = from.distance(to);
			this.linkGeos.put(link.getId(), li);
		}
		this.drivingDirections = new HashMap<Id, Coordinate>();
		for (Link link : this.floor.getLinks()) {
			Coordinate c = new Coordinate(link.getToNode().getCoord().getX() - link.getFromNode().getCoord().getX(), link.getToNode().getCoord().getY() - link.getFromNode().getCoord().getY());
			double length = Math.sqrt(Math.pow(c.x, 2) + Math.pow(c.y, 2));
			c.x /= length;
			c.y /= length;
			this.drivingDirections.put(link.getId(), c);
		}
	}

	private double getMinWidth(LineString link, Coord coord) {
		QuadTree<Coordinate> q = this.sc.getScenarioElement(MyDataContainer.class).getDenseCoordsQuadTree();
		Collection<Coordinate> coll = q.get(coord.getX(), coord.getY(), link.getLength());
		double minDist = Double.POSITIVE_INFINITY;
		for (Coordinate c : coll) {
			Point p = this.geofac.createPoint(c);
			double dist = p.distance(link);
			if (dist < minDist) {
				minDist = dist;
			}
		}
		return minDist;
	}

	public double [] getDesiredVelocity(Agent2D agent) {


		Id mentalLinkId =  agent.getMentalLink();
		Coordinate d = this.drivingDirections.get(mentalLinkId);
		double driveX = d.x  * agent.getDesiredVelocity();
		double driveY = d.y * agent.getDesiredVelocity();

		double fdx = PhysicalAgentRepresentation.AGENT_WEIGHT *(driveX)/this.tau;
		double fdy = PhysicalAgentRepresentation.AGENT_WEIGHT *(driveY)/this.tau;

		Coordinate pos = agent.getPosition();
		LinkInfo li = this.linkGeos.get(mentalLinkId);

		double pathDist = Math.abs(((li.c0.y-li.c1.y) * pos.x + (li.c1.x-li.c0.x)*pos.y + (li.c0.x*li.c1.y - li.c1.x*li.c0.y))/li.length);

		double fpx = 0;
		double fpy = 0;
		if (pathDist > 0.1) {
			double bpath = Math.max(1, li.pathWidth-agent.getPhysicalAgentRepresentation().getAgentDiameter());
			double f = Apath * Math.exp(pathDist / bpath);

			boolean rightHandSide = Algorithms.isLeftOfLine(pos, li.c0, li.c1) > 0;
			double dx = rightHandSide == true ? -li.perpendicularVector.x : li.perpendicularVector.x;
			double dy = rightHandSide == true ? -li.perpendicularVector.y : li.perpendicularVector.y;
			fpx  = dx * f;
			fpy = dy * f;
		}
		double fx = fdx + fpx;
		double fy = fdy + fpy;


		double dvx = (this.tau *fx)/PhysicalAgentRepresentation.AGENT_WEIGHT; //desired velocity
		double dvy =(this.tau *fy)/PhysicalAgentRepresentation.AGENT_WEIGHT;

		//		double dvx = agent.getVx()+(this.tau*fx)/Agent2D.AGENT_WEIGHT; //desired velocity
		//		double dvy = agent.getVy()+(this.tau*fy)/Agent2D.AGENT_WEIGHT;
		double denominator = Math.sqrt(Math.pow(dvx, 2)+Math.pow(dvy, 2)) / agent.getDesiredVelocity();
		dvx /= denominator;
		dvy /= denominator;

		return new double []{dvx,dvy};
	}

	private static final class LinkInfo {
		double pathWidth;
		double length;
		Coordinate perpendicularVector;
		Coordinate c0;
		Coordinate c1;
	}

}
