package de.lmu.ifi.dbs.utilities.optionhandling.constraints;

import java.util.List;

import de.lmu.ifi.dbs.utilities.optionhandling.Parameter;
import de.lmu.ifi.dbs.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.utilities.optionhandling.WrongParameterValueException;

/**
 * Global parameter constraint specifying that either all elements of a list of parameters
 * must be set or none of them. 
 * 
 * @author Steffi Wanka
 *
 */
public class AllOrNoneMustBeSetGlobalConstraint implements GlobalParameterConstraint {

	
	private List<Parameter> parameterList;
	
	 /* Constructs a global parameter constraint for testing if either all elements of a list
	 * of parameters are set or none of them.
	 * 
	 * @param parameters list of parameters to be checked
	 */
	public AllOrNoneMustBeSetGlobalConstraint(List<Parameter> parameters){
		this.parameterList = parameters;
	}
	
	/**
	 * Checks if either all elements of a list of parameters are set, or none of them. If not,
	 * a parameter exception is thrown.
	 * 
	 */
	public void test() throws ParameterException {
		
		int notSet = 0;
		int set = 0;
		for(Parameter p : parameterList){
			if(p.isSet()){
				set++;
			}
			else{
				notSet++;
			}
		}
		if(set != 0 && notSet!=0){
			throw new WrongParameterValueException("Global Constraint Error!\n" +
					"Either all of the parameters "+paramsToString()+" must be set or none of them!");
		}
	}
	
	private String paramsToString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		int counter = 1;
		for(Parameter p : parameterList){
			buffer.append(p.getName());
			if(counter != parameterList.size()){
				buffer.append(",");
			}
			counter++;
		}
		buffer.append("]");
		return buffer.toString();
	}

}
