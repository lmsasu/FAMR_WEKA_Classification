package ro.unitbv.pythia;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Title:        Modified Fuzzy Artmap
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

/**
* Functions learn, addWeight_a, and addWeight_b are according to:
* R. Andonie: "A Converse H-Theorem For Inductive Processes",
* Computers and Artificial intelligence, 9, 1990, 159-167.
* This is essentialy where this model is different than the original
* Carpenter et al. Fuzzy Artmap paradigm.
*/
public class MapField implements Serializable
{
	private static final long serialVersionUID = 2L;
	
	private int na = 0;
	private int nb = 0;
    private final double rho_ab; // initialized by constructor
	private FuzzyVector w_ab[] = null;
	private FuzzyVector x_ab = null;
    private double Q_t[] = null;
    private final double q_0 = 0.0;

    /**
     * Instantiates MapField for classification
     * @param rho_ab mapfield vigilance value
     * @param nb number of output classes
     */
    public MapField( double rho_ab, int nb )
	{
		// used for classification, when art_b is a FuzzyVector object
        this.rho_ab = rho_ab;
        this.nb = nb;
	}

    /**
     * Instantiates Mapfield for regression
     * @param rho_ab mapfield vigilance value
     */
    public MapField( double rho_ab )
	{
		// used for regression, when art_b is a FuzzyArt object
        this.rho_ab = rho_ab;
	}

    /**
     * Compute the Mapfield activity vector x_ab
     * @param y_b the output vector associated with the current input
     * @param J the input category index
     */
    private void computeActivation( FuzzyVector y_b, int J )
	{
		x_ab = y_b.and( w_ab[J] );
	}

    /**
     * Vigilance test
     * This test is in accordance with Carpenter's notation.
     * However, we can replace it with something more simple, like:
     * public boolean accept(J, K)
     * {
     *      return (w_ab[J,K] * nb) greater than rho_ab
     * }
     * In this case, computeActivation can be deleted.
     * I have preserved the original notation for scientific purposes.
     * @param y_b the output vector, 1 hot encoded
     * @param J the index of the winner input category
     * @return true if the current pattern is accepted (in accordance with mapfield) or not
     */
    public boolean accept( FuzzyVector y_b, int J )
	{
		computeActivation( y_b, J );
        return x_ab.norm() * nb >= rho_ab * y_b.norm();
	}

    /**
     * Learn conditional probabilities w_ab[J]
     * @param J the index of the winning input category
     * @param K the index of the winning output category
     * @param q_t teh relevance factor assigned to the current training pattern
     */
    public void learn( int J, int K, double q_t )
	{
		Q_t[J] += q_t;
        double A_t = q_t / Q_t[J];
        FuzzyVector delta_t = new FuzzyVector( nb );
        delta_t.initValue( 0 );
        delta_t.setValueAtPos( K, 1 );
        FuzzyVector aux = delta_t.diff( w_ab[J] );
        aux = aux.times( A_t );
        w_ab[J] = w_ab[J].sum( aux );
	}

	/**
     * Increment the number of nodes in Mapfield
     */
    public void addWeight_a()
	{
		na++;
        FuzzyVector aux[] = new FuzzyVector[na];
		Util.safeArrayCopy( w_ab, 0, aux, 0, na-1 );
		aux[na-1] = new FuzzyVector( nb );
        aux[na-1].initValue( 1./nb );
		w_ab = aux;
        double auxQ_t[] = new double[na];
        Util.safeArrayCopy( Q_t, 0, auxQ_t, 0, na-1 );
        auxQ_t[na-1] = q_0;
        Q_t = auxQ_t;
	}

     /**
      * Initialize weights after adding a node in art_b
     */
    public void addWeight_b()
	{
        nb++;
		if ( nb == 1 )
        {
            return; // when first node is added, do nothing
        }
        for ( int j=0; j < na; j++ )
        {
            FuzzyVector aux = new FuzzyVector(nb);
            double val = q_0 / (nb * Q_t[j]);
            for ( int k=0; k < nb-1; k++ )
            {
                aux.setValueAtPos( k, w_ab[j].getValueAtPos( k ) - val/(nb-1) );
            }
            aux.setValueAtPos( nb-1, val );
            w_ab[j] = aux;
        }
	}

    /**
     * Return a clone of w_ab; it gives read-only access to w_ab
     * @param J the index of the input category
     * @return a clone of w_ab; it gives read-only access to w_ab
     */
	public FuzzyVector getw_ab( int J )
	{
		return w_ab[J].clone();
	}

    /**
	* Object cloning
	*/
	public MapField clone()
	{
	    MapField result = new MapField(this.rho_ab, this.nb);
	    
	    result.na = this.na;
	    result.Q_t = this.Q_t == null ? null : Arrays.copyOf(this.Q_t, this.Q_t.length);
	    result.w_ab = new FuzzyVector[this.na];
	    Util.safeArrayCopy( this.w_ab, 0, result.w_ab, 0, this.na );//TODO: make deep copy 
	    result.x_ab = this.x_ab == null ? null : this.x_ab.clone();
	    
	    return result;
	}

	/**
	 * String representation of this
	 */
    public String toString()
    {
      StringBuffer result = new StringBuffer();
      for( int i=0; i<na; i++)
      {
        result.append( w_ab[i].toString() + "\n" );
      }
      return result.toString();
    }
    
    /**
     * @return number of lines in w_ab
     */
    public int getLinesNo()
    {
    	return w_ab.length;
    }
}