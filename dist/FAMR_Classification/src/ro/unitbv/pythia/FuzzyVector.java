package ro.unitbv.pythia;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Description: Models the fuzzy vector concept
 * Copyright:    Copyright (c) 2002
 * Transilvania University of Brasov
 * @author
 * @version 1.1
 */

public class FuzzyVector implements Serializable, Cloneable
{
	private static final long serialVersionUID = 2L;
	
	final int length;
	double v[];

	/**
	 * Allocate space for this.v
	 * @param n the length of the vector
	 */
	public FuzzyVector( int n )
	{
	    length = n;
		v = new double[ n ];//filled in with 0
	}

    /**
     * @param v the contents vector
     */
	public FuzzyVector( double v[] )
	{
		this( v.length );
		if (Util.min(v) < 0 || Util.max(v) > 1)
		{
			throw new RuntimeException("In classifyInFuzzyVectorputVector: the vector is not between 0 and 1.");
		}
	    System.arraycopy( v, 0, this.v, 0, length );
	}
	
	/**
	 * Returns the fuzzy and of this and b
	 * @param b the RHS for "this and b"
	 * @return the fuzzy and result
	 */
	public FuzzyVector and( FuzzyVector b )
	{
	    if (b == null)
	    {
	    	throw new RuntimeException("RHS is null in 'and' method");
	    }
		if ( length != b.length )
	    {
			throw new RuntimeException("Size mismatch in 'and' method");
		}
		FuzzyVector result = new FuzzyVector( length );
		for( int i=0; i<length; i++)
		{
		    result.v[i] = Math.min( v[i], b.v[i] );
		}
		return result;
	}

	/**
	* fuzzy or of this and b
	*/
	/**
	 * Returns the fuzzy or of this and b
	 * @param b the RHS for "this or b"
	 * @return the fuzzy or result
	 */
	public FuzzyVector or( FuzzyVector b )
	{
		if (b == null)
	    {
	    	throw new RuntimeException("RHS is null in 'or' method");
	    }
		if ( length != b.length )
		{
			throw new RuntimeException("Size mismatch in 'or' method");
		}
		FuzzyVector result = new FuzzyVector( length );
		for( int i=0; i<length; i++ )
		{
		    result.v[i] = Math.max( v[i], b.v[i] );
		}
		return result;
  }

	/**
	 * Returns the sum of this and b
	 * @param b the RHS of this + b
	 * @return this + b
	 */
	public FuzzyVector sum( FuzzyVector b )
	{
		if (b == null)
	    {
	    	throw new RuntimeException("RHS is null in sum");
	    }
		if ( length != b.length )
		{
		    throw new RuntimeException("Size mismatch in 'sum' method");
	    }
	    FuzzyVector result = new FuzzyVector( length );
	    for( int i=0; i<length; i++)
	    {
		    result.v[i] = v[i] + b.v[i];
	    }
	    return result;
	}

    /**
	* scalar product of this and b
	*/
	/**
	 * Computes scalar product of this and b
	 * @param b the FuzzyVector which is multiplied with "this"
	 * @return scalar product of this and b
	 */
	public double prod( FuzzyVector b )
	{
		if (b == null)
	    {
	    	throw new RuntimeException("RHS is null in 'prod' method");
	    }
		if ( length != b.length )
		{
		    throw new RuntimeException("size mismatch in 'prod' method");
	    }
	    double prod = 0;
	    for( int i=0; i<length; i++)
	    {
		    prod += v[i] * b.v[i];
	    }
	    return prod;
	}

	/**
	 * Computes this - b
	 * @param b the RHS of this - b
	 * @return difference of this and b
	 */
	public FuzzyVector diff( FuzzyVector b )
	{
		if (b == null)
	    {
	    	throw new RuntimeException("RHS is null in 'diff' method");
	    }
		if ( length != b.length )
	    {
	        throw new RuntimeException("size mismatch in 'diff' method");
		}
		FuzzyVector result = new FuzzyVector( length );
		for( int i=0; i<length; i++)
		{
		    result.v[i] = v[i] - b.v[i];
		}
		return result;
	}

	/**
	 * Computes product by a scalar value x
	 * @param x the scalar value used to multiply "this"
	 * @return product by a scalar value x
	 */
	public FuzzyVector times( double x )
	{
	    FuzzyVector result = new FuzzyVector( length );
	    for( int i=0; i<length; i++)
	    {
		    result.v[i] = x * v[i];
	    }
		return result;
	}

	/**
	 * Computes the max value from this
	 * @return the max value from this
	 */
	public double max()
	{
	    double result = Double.NEGATIVE_INFINITY;
		for( int i=0; i<length; i++)
		{
		    if ( result < v[i] )
		    {
			    result = v[i];
		    }
	    }
	    return result;
	}

	/**
	 * Computes position of max value from this
	 * @return the position of the max value
	 */
	public int posMax()
	{
	    int index = -1;
	    double max = Double.NEGATIVE_INFINITY;
	    for( int i=0; i<length; i++)
	    {
			if ( max < v[i] )
		    {
		        max = v[ i ];
		        index = i;
		    }
	    }
		return index;
	}

	/**
	 * Performs complement-coding (Carpenter 1992)
	 * @return the complement-coded of this
	 */
	public FuzzyVector normalize()
	{
	    FuzzyVector result = new FuzzyVector( 2 * length );
	    System.arraycopy( v, 0, result.v, 0, length );
		for( int i=length; i<2*length; i++)
	    {
		    result.v[i] = 1 - v[i-length];
		}
		return result;
	}

	/**
	 * Computes L1 norm of this
	 * @return L1 norm of this
	 */
	public double norm()
	{
	    double sum = 0.0;
	    for ( int i=0; i<length; i++)
	    {
			sum += Math.abs( v[i] );
		}
		return sum;
	}

	/**
	 * Performs  object cloning
	 */
	public FuzzyVector clone()
	{
	    return new FuzzyVector(this.v);
	}

	/**
	 * Fills this with value
	 * @param value the value filling the current vector
	 */
	public void initValue( double value )
	{
	    Arrays.fill( v, value );
	}

	/**
	 * 
	 * @param index position in vector
	 * @param value to be set
	 */
	public void setValueAtPos( int index, double value )
	{
	    v[index] = value;
	}

	/**
	 * getter for requested value
	 * @param index position in vector
	 * @return the value found at index
	 */
    public double getValueAtPos( int index )
    {
        return v[index];
    }

    /**
     * Safe copy of this' contents
     * @return a new array with this' contents
     */
    public double[] getValue()
    {
        double vector[] = new double[ length ];
        System.arraycopy( v, 0, this.v, 0, length );
        return vector;
    }

    /**
	* creates a string containing all values separated by space
	*/
    public String toString()
    {
        return Util.toString(this.v, ",");
	}
}