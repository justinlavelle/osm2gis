package ch.hsr.osminabox.db.entities;
/**
 * Based on the referenced Members, this AreaType is needed to create the Geometry-SQL correctly.
 * @author m2huber
 *
 */

	public enum AreaComposition {
	
		/** This Type has only Outer Tags and all Ways are closed rings **/
		ONLY_OUTER,		
		
		/** This Type has one Outer (closed!) and n-inner Ways (all Closed Rings) **/
		ONE_OUTER_N_INNER,
				
		/** This Type has n Inner and n Outer Rings. All are closed **/
		N_INNER_N_OUTER,
		
		/** Could not been detected or Relation is invalid **/
		UNKNOWN,
		
	}

