
package de.lmu.ifi.dbs.utilities.optionhandling.constraints;

import java.util.List;

import de.lmu.ifi.dbs.utilities.optionhandling.IntParameter;
import de.lmu.ifi.dbs.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.utilities.optionhandling.VectorListParameter;
import de.lmu.ifi.dbs.utilities.optionhandling.WrongParameterValueException;

/**
 * Global parameter constraint for testing if the dimensions of each vector in a
 * list of vectors corresponds to the value of a integer parameter given.
 * 
 * @author Steffi Wanka
 * 
 */
public class GlobalVectorListElementSizeConstraint implements GlobalParameterConstraint {

	/**
	 * vector list parameter
	 */
	private VectorListParameter vector;

	/**
	 * integer parameter providing the size constraint
	 */
	private IntParameter size;

	/**
	 * Constructs a global vector size constraint for testing if the dimensions
	 * of each vector of the vector list parameter given corresponds to the
	 * value of the integer parameter given.
	 * 
	 * @param vector
	 *            the vector list parameter
	 * @param sizeConstraint
	 *            the integer parameter providing the size constraint
	 */
	public GlobalVectorListElementSizeConstraint(VectorListParameter vector, IntParameter sizeConstraint) {
		this.vector = vector;
		this.size = sizeConstraint;
	}

	/**
	 * Checks if the dimensions of each vector of the vector list parameter have
	 * the appropriate size specified by the integer parameter. If not, a
	 * parameter exception will be thrown.
	 * 
	 * @see de.lmu.ifi.dbs.utilities.optionhandling.constraints.GlobalParameterConstraint#test()
	 */
	public void test() throws ParameterException {
		for (List<Double> vec : vector.getValue()) {
			if (vec.size() != size.getValue()) {
				throw new WrongParameterValueException("Global Parameter Constraint Error\n" + "The vectors of vector list parameter "
						+ vector.getName() + " have not the required dimension of " + size.getValue() + " given by integer parameter "
						+ size.getName() + "!");
			}
		}

	}

}
