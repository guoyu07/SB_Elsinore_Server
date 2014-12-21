package com.sb.elsinore.inputs;

import jGPIO.GPIO.Direction;
import jGPIO.InPin;
import jGPIO.InvalidGPIOException;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.json.simple.JSONObject;

import com.sb.elsinore.BrewServer;
import com.sb.elsinore.LaunchControl;
import com.sb.elsinore.NanoHTTPD.Response;
import com.sb.elsinore.annotations.PhSensorType;
import com.sb.elsinore.annotations.UrlEndpoint;
import com.sb.util.MathUtil;

public class PhSensor {

    private int ainPin = -1;
    private String dsAddress = "";
    private String dsOffset = "";
    private String model = "";
    private String name = "pH Sensor";
    private BigDecimal phReading = new BigDecimal(0);
    private BigDecimal offset = new BigDecimal(0);
    private InPin ainGPIO = null;
    private boolean stopLogging = false;

    /**
     * Create a blank pH Sensor.
     */
    public PhSensor() {
    }

    /**
     * Setup a phSensor using an analog pin.
     * @param newPin The analog pin.
     * @throws InvalidGPIOException If the pin is invalid.
     */
    public PhSensor(final int newPin) throws InvalidGPIOException {
        this.ainPin = newPin;
        ainGPIO = new InPin(this.ainPin, Direction.ANALOGUE);
    }

    /**
     * Setup a pH Sensor using a DS2450 based Analog input.
     * @param address The DS2450 Address
     * @param offset The DS2450 Offset
     */
    public PhSensor(final String address, final String offset) {
        this.dsAddress = address;
        this.dsOffset = offset;
    }

    /**
     * Set the pH Sensor Type.
     * @param type The type of the sensor.
     */
    public final void setType(final String type) {
        this.model = type;
    }

    /**
     * Get the current status in JSON Form.
     * @return The current status.
     */
    public final JSONObject getJsonStatus() {
        JSONObject retVal = new JSONObject();
        retVal.put("phReading", phReading);
        retVal.put("name", name);
        retVal.put("deviceType", model);
        return retVal;
    }

    /**
     * Update the current reading.
     * @return the current Analog Value.
     */
    public final BigDecimal updateReading() {
        BigDecimal pinValue = new BigDecimal(0);

        if (ainGPIO != null) {
            try {
                pinValue = new BigDecimal(ainGPIO.readValue());
                if (this.stopLogging) {
                    BrewServer.LOG.log(Level.SEVERE,
                        "Recovered pH level reading for " + this.name);
                    this.stopLogging = false;
                }
            } catch (Exception e) {
                if (!this.stopLogging) {
                    BrewServer.LOG.log(Level.SEVERE,
                        "Could not update the pH reading from Analogue", e);
                    this.stopLogging = true;
                }
                BrewServer.LOG.info("Reconnecting OWFS");
                LaunchControl.setupOWFS();
            }
        } else if (dsAddress != null && dsAddress.length() > 0
                && dsOffset != null && dsOffset.length() > 0) {
            try {
                pinValue = new BigDecimal(
                    LaunchControl.readOWFSPath(
                        dsAddress + "/volt." + dsOffset));
                if (this.stopLogging) {
                    BrewServer.LOG.log(Level.SEVERE,
                        "Recovered pH level reading for " + this.name);
                    this.stopLogging = false;
                }
            } catch (Exception e) {
                if (!this.stopLogging) {
                    BrewServer.LOG.log(Level.SEVERE,
                        "Could not update the pH reading from OWFS", e);
                    this.stopLogging = true;
                }
                BrewServer.LOG.info("Reconnecting OWFS");
                LaunchControl.setupOWFS();
            }
        }
        
        return pinValue;
    }

    /**
     * Get the current name.
     * @return The name of this Sensor
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Get the current pin.
     * @return "" if no pin set.
     */
    public final String getAIN() {
        if (this.ainPin == -1) {
            return "";
        }
        return Integer.toString(this.ainPin);
    }

    /**
     * Return the current DS2450 Offset.
     * @return DS2450 Offset
     */
    public final String getDsOffset() {
        return this.dsOffset;
    }

    /**
     * Return the current DS2450 Address.
     * @return DS2450 Address
     */
    public final String getDsAddress() {
        return this.dsAddress;
    }

    /**
     * Get the current pH Sensor Model.
     * @return The Model.
     */
    public final String getModel() {
        return this.model;
    }
    /**
     * Calculate the current PH Value based off the current pH Sensor type.
     * @param reading The current Analog read value.
     * @return The value of the pH Probe.
     */
    public final BigDecimal calcPhValue() {
        BigDecimal value = null;

        for (java.lang.reflect.Method m
                : PhSensor.class.getDeclaredMethods()) {
           PhSensorType calcMethod =
                   (PhSensorType) m.getAnnotation(PhSensorType.class);
           if (calcMethod != null) {
               if (calcMethod.model().equalsIgnoreCase(this.model)) {
                   try {
                       value = (BigDecimal) m.invoke(this);
                   } catch (IllegalAccessException e) {
                       //do nothing;
                   } catch (InvocationTargetException o) {
                       // do nothing
                   }
               }
           }
        }

        return value;
    }

    /**
     * Get the current list of available sensor types.
     * @return The List of available sensor types.
     */
    public final List<String> getAvailableTypes() {
        List<String> typeList = new ArrayList<String>();
        for (java.lang.reflect.Method m
                : PhSensor.class.getDeclaredMethods()) {
           PhSensorType calcMethod =
                   (PhSensorType) m.getAnnotation(PhSensorType.class);
           if (calcMethod != null) {
               typeList.add(calcMethod.model());
           }
        }

        return typeList;
    }

    /**
     * Calculate the current pH Value for the SEN0161 pH Sensor.
     * @return The current pH value.
     */
    @PhSensorType(model = "SEN0161")
    public final BigDecimal calcSEN0161() {
        int MAXREAD = 3;
        BigDecimal readValue = new BigDecimal(0);
        BigDecimal t = null;
        for (int i = 0; i <= MAXREAD; i++) {
            t = this.updateReading();
            if (t.compareTo(BigDecimal.ZERO) == 0) {
                i--;
            } else {
                readValue.add(t);
            }
        }
        readValue = readValue.divide(new BigDecimal(MAXREAD));
        t = MathUtil.multiply(readValue, (5.0 / 1024));
        return MathUtil.multiply(t, 3.5).add(offset);
    }
}
