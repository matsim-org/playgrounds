/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package playground.vsp.demandde.pendlermatrix;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


public class PendlerMatrixReader {

	private static final Logger log = Logger.getLogger(PendlerMatrixReader.class);

	private static final String PV_EINPENDLERMATRIX = "../../detailedEval/eingangsdaten/Pendlermatrizen/EinpendlerMUC_843_062004.csv";

	private static final String PV_AUSPENDLERMATRIX = "../../detailedEval/eingangsdaten/Pendlermatrizen/AuspendlerMUC_843_062004.csv";

	//	private static final String NODES = "../../shared-svn/studies/countries/de/prognose_2025/orig/netze/netz-2004/strasse/knoten_wgs84.csv";

	private Map<Integer, Zone> zones = new HashMap<Integer, Zone>();

	private TripFlowSink flowSink;

	private String shapeFile;

	public PendlerMatrixReader(String shapeFile) {
		this.shapeFile = shapeFile;
	}

	public void run() {
		//		readNodes();
		readShape();
		readMatrix(PV_EINPENDLERMATRIX);
		readMatrix(PV_AUSPENDLERMATRIX);
		flowSink.complete();
	}

	private void readShape() {
		Collection<SimpleFeature> landkreise = ShapeFileReader.getAllFeatures(this.shapeFile);
		for (SimpleFeature landkreis : landkreise) {
			Integer gemeindeschluessel = Integer.parseInt((String) landkreis.getAttribute("gemeindesc"));
			Geometry geo = (Geometry) landkreis.getDefaultGeometry();
			Point point = getRandomPointInFeature(new Random(), geo);
			Coordinate coordinate = point.getCoordinate();
			Double xcoordinate = coordinate.x;
			Double ycoordinate = coordinate.y;
			Coord coord = new CoordImpl(xcoordinate.toString(), ycoordinate.toString());
			Zone zone = new Zone(gemeindeschluessel, 1, 1, coord);
			zones.put(gemeindeschluessel, zone);
		}
	}

	private static Point getRandomPointInFeature(Random rnd, Geometry g) {
		Point p = null;
		double x, y;
		do {
			x = g.getEnvelopeInternal().getMinX() + rnd.nextDouble() * (g.getEnvelopeInternal().getMaxX() - g.getEnvelopeInternal().getMinX());
			y = g.getEnvelopeInternal().getMinY() + rnd.nextDouble() * (g.getEnvelopeInternal().getMaxY() - g.getEnvelopeInternal().getMinY());
			p = MGC.xy2Point(x, y);
		} while (!g.contains(p));
		return p;
	}

	//// leaves some of the municipalities empty...
	//
	//	private void readNodes() {
	//		final CoordinateTransformation coordinateTransformation = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);
	//		TabularFileParserConfig tabFileParserConfig = new TabularFileParserConfig();
	//		tabFileParserConfig.setFileName(NODES);
	//		tabFileParserConfig.setDelimiterTags(new String[] {";"});
	//		try {
	//			new TabularFileParser().parse(tabFileParserConfig,
	//					new TabularFileHandler() {
	//				@Override
	//				public void startRow(String[] row) {
	//					if (row[0].startsWith("Knoten")) {
	//						return;
	//					}
	//					int zone = Integer.parseInt(row[5]);
	//					double x = Double.parseDouble(row[2]);
	//					double y = Double.parseDouble(row[3]);
	//					Zone zone1 = new Zone(zone, 1, 1, coordinateTransformation.transform(new CoordImpl(x,y)));
	//					zones.put(zone, zone1);
	//				}
	//
	//			});
	//		} catch (IOException e) {
	//			throw new RuntimeException(e);
	//		}
	//	}

	private void readMatrix(final String filename) {

		Logger.getLogger(this.getClass()).warn("this method may read double entries in the Pendlermatrix (such as Nuernberg) twice. " +
						"If this may be a problem, you need to check.  kai, apr'11" ) ;

		System.out.println("======================" + "\n"
						+ "Start reading " + filename + "\n"
						+ "======================" + "\n");
		TabularFileParserConfig tabFileParserConfig = new TabularFileParserConfig();
		tabFileParserConfig.setFileName(filename);
		tabFileParserConfig.setDelimiterTags(new String[] {","});
		new TabularFileParser().parse(tabFileParserConfig,
						new TabularFileHandler() {

			@Override
			public void startRow(String[] row) {
				if (row[0].startsWith("#")) {
					return;
				}
				Integer quelle = null ;
				Integer ziel = 0;
				// car market share for commuter work/education trips (taken from "Regionaler Nahverkehrsplan-Fortschreibung, MVV 2007)
				double carMarketShare = 0.67;
				// scale factor, since Pendlermatrix only considers "sozialversicherungspflichtige Arbeitnehmer" (taken from GuthEtAl2005)
				double scaleFactor = 1.29;

				if (filename.equals(PV_EINPENDLERMATRIX)){
					try {
						quelle = Integer.parseInt(row[2]);
						ziel = 9162 ;

						int totalTrips = (int) (scaleFactor * Integer.parseInt(row[4]));
						int workPt = (int) ((1 - carMarketShare) * totalTrips) ;
						int educationPt = 0 ;
						int workCar = (int) (carMarketShare * totalTrips);
						int educationCar = 0 ;
						String label = row[3] ;
						if ( !label.contains("brige ") && !quelle.equals(ziel)) {
							process(quelle, ziel, workPt, educationPt, workCar, educationCar);
						} else {
							System.out.println( " uebrige? : " + label ) ;
						}
					} catch ( Exception ee ) {
						System.err.println("we are trying to read quelle: " + quelle ) ;
						//						System.exit(-1) ;
					}
				}
				else if (filename.equals(PV_AUSPENDLERMATRIX)){
					try {
						quelle = 9162;
						ziel = Integer.parseInt(row[2]);

						int totalTrips = (int) (scaleFactor * Integer.parseInt(row[4]));
						int workPt = (int) ((1 - carMarketShare) * totalTrips) ;
						int educationPt = 0 ;
						int workCar = (int) (carMarketShare * totalTrips);
						int educationCar = 0 ;
						String label = row[3] ;
						if ( !label.contains("brige ") && !quelle.equals(ziel)) {
							process(quelle, ziel, workPt, educationPt, workCar, educationCar);
						} else {
							System.out.println( " uebrige? : " + label ) ;
						}
					} catch ( Exception ee ) {
						System.err.println("we are trying to read quelle: " + quelle ) ;
						//						System.exit(-1) ;
					}
				}
				else{
					System.err.println("ATTENTION: check filename!") ;
				}
			}

		});
	}

	private void process(int quelle, int ziel, int workPt, int educationPt, int workCar, int educationCar) {
		Zone source = zones.get(quelle);
		Zone sink = zones.get(ziel);
		if (source == null) {
			log.error("Unknown source: " + quelle);
			return;
		}
		if (sink == null) {
			log.error("Unknown sink: " + ziel);
			return;
		}
		int carQuantity = workCar + educationCar ;
		int ptQuantity = workPt + educationPt;
		int scaledCarQuantity = scale(carQuantity);
		int scaledPtQuantity = scale(ptQuantity);

		if (scaledCarQuantity != 0) {
			log.info(quelle + "->" + ziel + ": " + scaledCarQuantity + " car trips");
			flowSink.process(zones.get(quelle), zones.get(ziel), scaledCarQuantity, TransportMode.car, "pvWork", 0.0);
		}
		if (scaledPtQuantity != 0){
			log.info(quelle + "->" + ziel + ": " + scaledPtQuantity + " pt trips");
			flowSink.process(zones.get(quelle), zones.get(ziel), scaledPtQuantity, TransportMode.pt, "pvWork", 0.0);
		}
	}

	private int scale(int quantityOut) {
		int scaled = (int) (quantityOut * 0.1 );
		return scaled;
	}

	void setFlowSink(TripFlowSink flowSink) {
		this.flowSink = flowSink;
	}

}
