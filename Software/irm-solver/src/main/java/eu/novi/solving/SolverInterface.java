package eu.novi.solving;

/**
 * Solver is an interface to the appropriate solver (e.g. LP solver etc.) 
 * to be used by the algorithms for solving in the inter-domain VNE problem
 * and the splitting resources problem. 
 * 
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 */
public interface SolverInterface {
	
	/**
	 * 
	 */
	public void solve();

	
}
