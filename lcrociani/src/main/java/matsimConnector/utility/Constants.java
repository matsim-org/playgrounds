package matsimConnector.utility;

public class Constants {

	public static final String CA_MOBSIM_MODE = "MobsimCA";
	public static final String CA_LINK_MODE = "walkCA";
	public static final String TO_Q_LINK_MODE = "CA->Q";
	public static final String TO_CA_LINK_MODE = "Q->CA";
	
	public static final double CA_CELL_SIDE = 0.4;
	public static final double CA_STEP_DURATION = .3;
	public static final Double PEDESTRIAN_SPEED = CA_CELL_SIDE/CA_STEP_DURATION;
	public static final Double CA_TEST_END_TIME = 1200.;
	
	/** name to use to add CAScenario to a matsim scenario as a scenario element **/ 
	public static final String CASCENARIO_NAME = "CAScenario";
	
	public static Double FLOPW_CAP_PER_METER_WIDTH = 1.2;
	
	public static final Double TRANSITION_AREA_LENGTH = CA_CELL_SIDE*5;
	public static final Double FAKE_LINK_WIDTH = 2.5;
	public static final Double TRANSITION_LINK_LENGTH = 5.;//TRANSITION_AREA_LENGTH/2.;
	public static final int TRANSITION_AREA_COLUMNS = (int)(TRANSITION_AREA_LENGTH/CA_CELL_SIDE);
	
	public static final String PATH = "C:/Users/Luca/Documents/uni/Dottorato/Juelich/developing_stuff/Test";
	public static final String DEBUG_TEST_PATH = PATH+"/debug";
	public static final String FD_TEST_PATH = PATH+"/FD/";
	public static final String INPUT_PATH = DEBUG_TEST_PATH+"/input";
	public static final String OUTPUT_PATH = DEBUG_TEST_PATH+"/output";
	public static final String COORDINATE_SYSTEM = "EPSG:3395";
}
