package org.grobid.core.utilities;

import org.grobid.core.data.Unit;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
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
        UNKNOWN("unknown"),
        SI_BASE("SI base"),
        SI_DERIVED("SI derived"),
        IMPERIAL("imperial"),
        US("us"),
        NON_SI("non SI");

        private String name;

        private System_Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    // measurement types
    public enum Unit_Type {
        UNKNOWN("unknown"),
        LENGTH("length"),
        TIME("time"),
        TEMPERATURE("temperature"),
        MASS("mass"),
        LUMINOUS_INTENSITY("luminous intensity"),
        AMOUNT_OF_SUBSTANCE("amount of substance"),
        ELECTRIC_CURRENT("electric current"),
        ANGLE("angle"),
        SOLID_ANGLE("solid angle"),
        FREQUENCY("frequency"),
        FORCE("force"),
        PRESSURE("pressure"),
        ENERGY("energy"),
        POWER("power"),
        ELECTRIC_CHARGE("electric charge"),
        VOLTAGE("voltage"),
        ELECTRIC_CAPACITANCE("electric capacitance"),
        ELECTRIC_RESISTANCE("electric resistance"),
        ELECTRIC_CONDUCTANCE("electric conductance"),
        ELECTRIC_FIELD("electric field"),
        MAGNETIC_FLUX("magnetic flux"),
        MAGNETIC_INDUCTION("magnetic induction"),
        MAGNETIC_FIELD_STRENGTH("magnetic field strength"),
        INDUCTANCE("inductance"),
        LUMINOUS_FLUX("luminous flux"),
        ILLUMINANCE("illuminance"),
        LUMINANCE("luminance"),
        RADIOACTIVITY("radioactivity"),
        ABSORBED_DOSE("absorbed dose"),
        EQUIVALENT_DOSE("equivalent dose"),
        ATTENUATION("attenuation"),
        TORQUE("torque"),
        DYNAMIC_VISCOSITY("dynamic viscosity"),
        KINEMATIC_VISCOSITY("kinematic viscosity"),
        ACOUSTIC_PRESSURE("acoustic pressure"),
        MASS_FLOW_RATE("mass flow-rate"),
        VOLUME_FLOW_RATE("volume flow-rate"),
        AIR_FLOW_RATE("air flow-rate"),
        SPECTRAL_RESPONSITIVY("spectral responsivity"),
        SPECTRAL_TRANSMITTANCE("regular spectral transmittance"),
        SPECTRAL_REFLECTANCE("diffuse spectral reflectance"),
        REFLECTANCE("reflectance"),
        DETECTOR_PASSBAND("detector passband"),
        THERMAL_CONDUCTIVITY("thermal conductivity"),
        THERMAL_DIFFUSIVITY("thermal diffusivity"),
        HEAT_CAPACITY("specific heat capacity"),
        EMISSION_RATE("emission rate"),
        CATALYTIC_ACTIVITY("catalytic activity"),
        RADIANCE("radiance"),
        IRRADIANCE("irradiance"),
        EMISSIVITY("emissivity"),
        HUMIDITY("humidity"),
        VOLUME("volume"),
        VELOCITY("velocity"),
        AREA("area"),
        CONCENTRATION("concentration"),
        DENSITY("density"),
        ACIDITY("acidity"),
        FRACTION("fraction");
//		PROPORTION			("proportion"),	// -> this is fraction

        private String name;

        private Unit_Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    ;

    // measurement type (atomic value, interval of conjuctive/disjunctive list of values/intervals)
    public enum Measurement_Type {
        VALUE("value"),
        INTERVAL("interval"),
        CONJUNCTION("listc"),
        DISJUNCTION("listd");

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

    private UnitUtilities() {
    }

    /*public void initUnitUtilities() {
        File file = null;
        InputStream ist = null;
        InputStreamReader isr = null;
        BufferedReader dis = null;
        try {
            String path = "src/main/resources/en/units.txt";
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
                    String notations = st.nextToken().trim();
                    String[] notationList = notations.split(",");
                    if (st.hasMoreTokens()) {
                        String type = st.nextToken().trim();
                        if ((type == null) || (type.length() == 0)) {
                            continue;
                        }
                        System_Type system = System_Type.UNKNOWN;
                        if (st.hasMoreTokens()) {
                            String sys = st.nextToken().trim();
                            // here "deserialize" the enum type
                            try {
                                system = System_Type.valueOf(sys);
                            }
                            catch(Exception e) {
                                System.out.println("Invalid system type name: " + sys);
                            }
                        }
                        List<String> names = null;
                        if (st.hasMoreTokens()) {
                            // usual name(s), e.g. metre, meter
                            StringTokenizer st2 = new StringTokenizer(st.nextToken(), ",");
                            while (st2.hasMoreTokens()) {
                                if (names == null)
                                    names = new ArrayList<String>();
                                String localName = st2.nextToken().trim();
                                names.add(localName);
                                // some expansions

                            }
                        }

                        Unit unit = new Unit();
                        if ((notationList != null) && (notationList.length > 0)) {
                            for (int j = 0; j < notationList.length; j++)
                                unit.addNotation(notationList[j].trim());
                        }
                        unit.setSystem(system);
                        // here "deserialize" the enum type
                        Unit_Type savedType = Unit_Type.UNKNOWN;
                        try {
                            savedType = Unit_Type.valueOf(type);
                        }
                        catch(Exception e) {
                            System.out.println("Invalid unit type name: " + type);
                        }
                        unit.setType(savedType);
                        unit.setNames(names);

                        // add unit names in the first map
                        if ((names != null) && (names.size() > 0)) {
                            for (int j = 0; j < names.size(); j++) {
                                if (name2unit == null)
                                    name2unit = new HashMap<String, Unit>();
                                name2unit.put(names.get(j).trim().toLowerCase(), unit);
                            }
                        }

                        // add unit notation map
                        if ((notationList != null) && (notationList.length > 0)) {
                            for (int j = 0; j < notationList.length; j++) {
                                if (notation2unit == null)
                                    notation2unit = new HashMap<String, Unit>();
                                notation2unit.put(notationList[j].trim().toLowerCase(), unit);
                            }
                        } else
                            notation2unit.put("no_notation", unit);

                        // add unit in the second map
                        if ((system == System_Type.SI_BASE) || (system == System_Type.SI_DERIVED)) {
                            if (type2SIUnit == null) {
                                type2SIUnit = new HashMap<String, Unit>();
                            }
                            if (type2SIUnit.get(type) == null)
                                type2SIUnit.put(type, unit);
                        }

                    }
                }
            }
        } catch (FileNotFoundException e) {
//	    	e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        } catch (IOException e) {
//	    	e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
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
    }*/


}