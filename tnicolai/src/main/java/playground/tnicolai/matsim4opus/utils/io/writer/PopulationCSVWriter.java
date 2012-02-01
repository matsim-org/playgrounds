package playground.tnicolai.matsim4opus.utils.io.writer;

import java.io.BufferedWriter;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.utils.io.IOUtils;

import playground.tnicolai.matsim4opus.constants.Constants;
import playground.tnicolai.matsim4opus.utils.helperObjects.ClusterObject;
import playground.tnicolai.matsim4opus.utils.helperObjects.PersonAndJobsObject;

public class PopulationCSVWriter {
	
	private static final Logger log = Logger.getLogger(PopulationCSVWriter.class);
	
	/**
	 * writing raw population data to disc
	 * @param file
	 * @param personLocations
	 */
	public static void writePopulationData2CSV(final String file, final Map<Id, PersonAndJobsObject> personLocations){
		
		try{
			log.info("Dumping person information as csv to " + file + " ...");
			BufferedWriter bwPopulation = IOUtils.getBufferedWriter( file );
			
			// create header
			bwPopulation.write(Constants.ERSA_PERSON_ID +","+ 
								 Constants.ERSA_PARCEL_ID +","+ 
								 Constants.ERSA_X_COORDNIATE +","+ 
								 Constants.ERSA_Y_COORDINATE);
			bwPopulation.newLine();
			
			Iterator<PersonAndJobsObject> personIterator = personLocations.values().iterator();

			while(personIterator.hasNext()){
				
				PersonAndJobsObject person = personIterator.next();
				
				bwPopulation.write(person.getObjectID() + "," + 
								   person.getParcelID() + "," +
								   person.getCoord().getX() + "," +
								   person.getCoord().getY());
				bwPopulation.newLine();
			}
			
			bwPopulation.flush();
			bwPopulation.close();
			log.info("... done!");
		}
		catch(Exception e){ 
			e.printStackTrace(); 
		}
	}
	
	/**
	 * writing aggregated population data to disc
	 * @param file
	 * @param personClusterMap
	 */
	public static void writeAggregatedPopulationData2CSV(final String file, final Map<Id, ClusterObject> personClusterMap){
		
		try{
			log.info("Dumping aggregated person information as csv to " + file + " ...");
			BufferedWriter bwAggregatedPopulation = IOUtils.getBufferedWriter( file );
			
			// create header
			bwAggregatedPopulation.write(Constants.ERSA_PARCEL_ID +","+ 
					 		   Constants.ERSA_NEARESTNODE_ID +","+
					 		   Constants.ERSA_NEARESTNODE_X_COORD +","+ 
					 		   Constants.ERSA_NEARESTNODE_Y_COORD +","+
					 		   Constants.ERSA_PERSONS_COUNT);
			bwAggregatedPopulation.newLine();
			
			Iterator<ClusterObject> personIterator = personClusterMap.values().iterator();

			while(personIterator.hasNext()){
				
				ClusterObject person = personIterator.next();
				
				bwAggregatedPopulation.write(person.getParcelID() + "," +
								   person.getNearestNode().getId() + "," +
								   person.getCoordinate().getX() + "," +
								   person.getCoordinate().getY() + "," +
								   person.getNumberOfObjects());
				bwAggregatedPopulation.newLine();
			}
			
			bwAggregatedPopulation.flush();
			bwAggregatedPopulation.close();
			log.info("... done!");
		}
		catch(Exception e){ 
			e.printStackTrace(); 
		}
	}

}
