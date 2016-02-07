package org.grobid.core.utilities;

import org.grobid.core.data.Unit;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.io.*;

/**
 * Utilities for managing SI and non-SI units.
 *
 * @author Patrice Lopez
 */
public class UnitUtilities {
	
	// measurement systems
	public enum System_Type {
		UNKNOWN,
		SI_BASE,
		SI_DERIVED,
		IMPERIAL,
		US,
		NON_SI
	}

	// measurement types
	public enum Unit_Type {
		UNKNOWN				("unknown"),
		LENGTH 				("Length"), 
		TIME 				("Time"), 
		TEMPERATURE			("Temperature"), 
		MASS				("Mass"), 
		LUMINOUS_INTENSITY 	("Luminous Intensity"), 
		AMOUNT_OF_SUBSTANCE	("Amount of Substance"), 
		ELECTRIC_CURRENT	("Electric Current"),
		ANGLE 				("Angle"), 
		SOLID_ANGLE			("Solid Angle"), 
		FREQUENCY			("Frequency"), 
		FORCE				("Force"), 
		PRESSURE			("Pressure"), 
		ENERGY				("Energy"), 
		POWER 				("Power"),
		ELECTRIC_CHARGE		("Electric Charge"), 
		VOLTAGE				("Voltage"), 
		ELECTRIC_CAPACITANCE 	("Electric Capacitance"), 
		ELECTRIC_RESISTANCE		("Electric Resistance"), 
		ELECTRIC_CONDUCTANCE	("Electric Conductance"), 
		ELECTRIC_FIELD			("Electric Field"),
		MAGNETIC_FLUX			("Magnetic Flux"),
		MAGNETIC_INDUCTION		("Magnetic Induction"), 
		MAGNETIC_FIELD_STRENGTH	("Magnetic Field Strength"), 
		INDUCTANCE				("Inductance"), 
		LUMINOUS_FLUX			("Luminous Flux"), 
		ILLUMINANCE				("Illuminance"), 
		LUMINANCE				("Luminance"), 
		RADIOACTIVITY			("Radioactivity"),
	 	ABSORBED_DOSE			("Absorbed Dose"), 
		EQUIVALENT_DOSE			("Equivalent Dose"), 
		ATTENUATION				("Attenuation"), 
		TORQUE					("Torque"),
		DYNAMIC_VISCOSITY 		("Dynamic Viscosity"),
		KINEMATIC_VISCOSITY		("Knematic Viscosity"),
		ACOUSTIC_PRESSURE		("Acoustic Pressure"), 
		MASS_FLOW_RATE			("Mass Flow-rate"), 
		VOLUME_FLOW_RATE		("Volume Flow-rate"), 
		AIR_FLOW_RATE			("Air Flow-rate"), 
		SPECTRAL_RESPONSITIVY	("Spectral Responsivity"),
		SPECTRAL_TRANSMITTANCE	("Regular Spectral Transmittance"),
		SPECTRAL_REFLECTANCE	("Diffuse Spectral Reflectance"),
		REFLECTANCE				("Reflectance"),
		DETECTOR_PASSBAND		("Detector Passband"),
		THERMAL_CONDUCTIVITY	("Thermal Conductivity"),
		THERMAL_DIFFUSIVITY		("Thermal Diffusivity"),
		HEAT_CAPACITY			("Specific Heat Capacity"),
		EMISSION_RATE 			("Emission Rate"),
		CATALYTIC_ACTIVITY		("Catalytic Activity"),
		RADIANCE			("Radiance"), 
		IRRADIANCE			("Irradiance"), 
		EMISSIVITY			("Emissivity"), 
		HUMIDITY			("Humidity"),
		VOLUME				("Volume"), 
		VELOCITY			("Velocity"), 
		AREA				("Area"), 
		CONCENTRATION		("Concentration"),
		PROPORTION			("proportion"),	
		FRACTION			("Fraction");
		
		private String name;

		private Unit_Type(String name) {
          	this.name = name;
		}
		
		public String getName() {
			return name;
		}
	};
	
	// measurement type (atomic value, interval of conjuctive/disjunctive list of values/intervals)
	public enum Measurement_Type {
		VALUE		("value"),
		INTERVAL 	("interval"), 
		CONJUNCTION	("listc"),
		DISJUNCTION	("listd");
		
		private String name;

		private Measurement_Type(String name) {
          	this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	private static volatile UnitUtilities instance;
	
	public static UnitUtilities getInstance() {
        if (instance == null) {
            //double check idiom
            // synchronized (instanceController) {
                if (instance == null)
					getNewInstance();
            // }
        }
        return instance;
    }

    /**
     * Creates a new instance.
     */
	private static synchronized void getNewInstance() {
		//LOGGER.debug("Get new instance of Lexicon");
		//GrobidProperties.getInstance();
		instance = new UnitUtilities();
	}
	
	// full unit information accessible from the unit names and notation
	// this mapping is dependent on the language
	private Map<String, Unit> name2unit = null;

	// mapping between measurement types and the SI units for this type, the type here is represented with 
	// the name() value of the enum 
	private Map<String, Unit> type2SIUnit = null;
	
	private UnitUtilities() {}
	
	public void initUnitUtilities() {
		File file = null;
		InputStream ist = null;
        InputStreamReader isr = null;
        BufferedReader dis = null;
        try {			
			String path = "src/main/resources/units.en.txt";
			file = new File(path);
	        if (!file.exists()) {
	            throw new GrobidResourceException("Cannot init unit utilities, because file '" 
					+ file.getAbsolutePath() + "' does not exists.");
	        }
	        if (!file.canRead()) {
	            throw new GrobidResourceException("Cannot init unit utilities, because cannot read file '" 
					+ file.getAbsolutePath() + "'.");
	        }

            ist = getClass().getResourceAsStream(path);
			if (ist == null) 
				ist = new FileInputStream(file);
            isr = new InputStreamReader(ist, "UTF8");
            dis = new BufferedReader(isr);
			
            String l = null;
            while ((l = dis.readLine()) != null) {
            	if (l.length() == 0) continue;
                StringTokenizer st = new StringTokenizer(l, "\t");
                if (st.hasMoreTokens()) {
                 	String notation = st.nextToken().trim(); 
                    if (st.hasMoreTokens()) { 
						String type = st.nextToken().trim();
	                    if (type.length() == 0) {
							continue;
						}
						if ( (type == null) || (type.length() == 0) ) {
							continue;
						}
						System_Type system = System_Type.UNKNOWN;
						if (st.hasMoreTokens()) { 
							String sys = st.nextToken().trim();
							// here "deserialize" the enum type
							system = System_Type.valueOf(sys);
						}
						List<String> names = null;
						if (st.hasMoreTokens()) { 
							// usual name(s), e.g. metre, meter
							StringTokenizer st2 = new StringTokenizer(st.nextToken(), ",");
							while(st2.hasMoreTokens()) {
								names.add(st2.nextToken().trim());
							}
						}
						
						Unit unit = new Unit();
						unit.setNotation(notation);
						unit.setSystem(system);
						// here "deserialize" the enum type
						Unit_Type savedType = Unit_Type.valueOf(type); 
						unit.setType(savedType);
						unit.setNames(names);
						
						// add unit in the first map
						if ( (notation != null) && (notation.length() > 0) )
							name2unit.put(notation, unit);
						else
							name2unit.put("no_notation", unit);
						
						// add unit in the second map
						if ( (system == System_Type.SI_BASE) || (system == System_Type.SI_DERIVED) ) {
							if (type2SIUnit == null) {
								type2SIUnit = new HashMap<String, Unit>();
							}
							if (type2SIUnit.get(type) == null)
								type2SIUnit.put(type, unit);
						}
						
					}
                }
	        } 
		}	
		catch (FileNotFoundException e) {
//	    	e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        } 
		catch (IOException e) {
//	    	e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        } 
		finally {
            try {
                if (ist != null)
                    ist.close();
                if (isr != null)
                    isr.close();
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("Cannot close all streams.", e);
            }
        }
    }
	
	/**
	 * Return a unit object based on an unit name.
	 */
	public Unit getUnitbyName(String name) {
		return (Unit)name2unit.get(name.toLowerCase());
	}

	/** 
	 * Return the SI unit object from a measure type name
	 */
	public Unit getSIUnitByType(String type) {
		return (Unit)type2SIUnit.get(type);
	}
}