package ca.strangebrew.recipe;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class FermentStep implements Comparable<FermentStep> {
	private String type;
	private String tempU;
	private int time;
	private double temp;
	static final public String PRIMARY = "Primary";
	static final public String SECONDARY = "Secondary";
	static final public String CLEARING = "Clearing";
	static final public String AGEING = "Ageing";
	static final public String[] types = {PRIMARY, SECONDARY, CLEARING, AGEING};

    public FermentStep() {
	}
	
	// Misc Utility
	static public int getTypeIndex(String s) {
		for (int i = 0; i < types.length; i++) {
			if (types[i].equalsIgnoreCase(s)) {
				return i;
			}
		}

		return 0; 
	}
	
	public String toXML() {
		String out = "      <ITEM>\n";
		out += "          <TYPE>" + type + "</TYPE>\n";
		out += "          <TIME>" + Integer.toString(time) + "</TIME>\n";
		out += "          <TEMP>" + Double.toString(temp) + "</TEMP>\n";
		out += "          <TEMPU>" + tempU + "</TEMPU>\n";
   		out += "      </ITEM>\n";
		return out;
	}

	
	// Getters and Setters
	public double getTemp() {
		return temp;
	}
	public String getTempU() {
		return tempU;
	}
	public int getTime() {
		return time;
	}
	public String getType() {
		return type;
	}
	public void setTemp(double temp) {
		this.temp = temp;
	}
	public void setTempU(String tempU) {
		this.tempU = tempU;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public int compareTo(@Nonnull FermentStep f) {
		return 0;
// Broken for some reason.. causes weirdness with UI
//				// Sort by type then by time
//				FermentStep fA = (FermentStep)a;
//				FermentStep fB = (FermentStep)b;
//				int result = FermentStep.getTypeIndex(fA.getType()) - FermentStep.getTypeIndex(fB.getType());
//				
//				if (result == 0) {
//					result = fA.getTime() - fB.getTime();
//				}			
	}

    public void convertTo(String newU) {
        if (this.tempU.equalsIgnoreCase(newU)) {
            return;
        }
        if (newU.equals("F")) {
            this.temp = BrewCalcs.cToF(this.temp);
        } else {
            this.temp = BrewCalcs.fToC(this.temp);
        }
        this.tempU = newU;
    }

    public double getTimeMins() {
        return time*24*60;
    }
}
