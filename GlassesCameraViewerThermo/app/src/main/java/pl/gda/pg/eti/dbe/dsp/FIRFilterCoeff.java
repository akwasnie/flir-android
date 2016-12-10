/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.gda.pg.eti.dbe.dsp;

import DSP.fft.RDFT;
import java.util.Arrays;

/**
 *
 * @author jwr
 */
public class FIRFilterCoeff {
    
  
    
    /**
     * bhm = fir1(64,[0.08 0.48],hamming(64+1));
     */
  private static double [] bandPass0_6_to_3_6_hamming64= {-0.001474422541094352,-0.0004509614429506483,-2.089184551898957e-06,-0.001234020844973777,-0.00206608234497564,-0.0003184552162750516,0.001576122632879778,3.88546384891137e-07,-0.001933896152542998,0.001673407034384744,0.007091617222865363,0.005204156592530343,6.105505670960046e-06,0.004676392311218506,0.01509607451031898,0.01219521502629963,-0.001403337071641021,4.082383048818632e-06,0.0160950760903362,0.01220341869155172,-0.01656167781028775,-0.02512099643244326,-4.246888823375497e-06,0.001211883461460646,-0.04879272545905641,-0.07657776249120726,-0.02991191449358062,-3.852617790809881e-06,-0.08428430479212093,-0.1723627625180652,-0.05438238167609718,0.2387282105467999,0.3997561754771806,0.2387282105467999,-0.05438238167609718,-0.1723627625180652,-0.08428430479212093,-3.852617790809907e-06,-0.02991191449358062,-0.07657776249120728,-0.04879272545905642,0.001211883461460646,-4.246888823376618e-06,-0.02512099643244326,-0.01656167781028776,0.01220341869155172,0.01609507609033621,4.082383048819011e-06,-0.001403337071641021,0.01219521502629964,0.01509607451031898,0.004676392311218507,6.105505670959798e-06,0.005204156592530346,0.007091617222865368,0.001673407034384744,-0.001933896152542999,3.88546384890358e-07,0.001576122632879778,-0.0003184552162750518,-0.002066082344975641,-0.001234020844973777,-2.089184551899103e-06,-0.0004509614429506487,-0.001474422541094352};
    
    /** double[] containing the FIR filter coefficients */
  protected double[]            coefficients;      // filter coefficients
  
  /** int containing the number of filter coefficients */
  protected int                Nc;                // number of coefficients
  
  
  public FIRFilterCoeff(double [] coeff){
      coefficients=coeff.clone();
      Nc=coefficients.length;
  }
  
  public FIRFilterCoeff(){
      coefficients=bandPass0_6_to_3_6_hamming64.clone();
      Nc=coefficients.length;
  }
  
  /**
   * Method to filter a fixed-length sequence with this filter.
   *
   * @param x       double[] containing the input sequence.
   * @return        double[] containing the resulting filtered sequence.
   */
  public    double[]    filterZeroPhase( double[] x ) {
    
    int nfft     = 16;
    int log2nfft = 4;
    int n        = x.length + 2*coefficients.length ;
    while ( nfft < n ) {
      nfft *= 2;
      log2nfft++;
    }
    
    RDFT    fft = new RDFT( log2nfft );
    double[] tmp       = new double[ nfft ];
    double[] transform = new double[ nfft ];
    double[] kernel    = new double[ nfft ];
    
    System.out.println("SIZE n: "+n);
    
    System.arraycopy( x, 0, tmp, coefficients.length, x.length );
    for(int i=0;i<coefficients.length;i++){
        tmp[i]=x[coefficients.length-i];
    }
    for(int i=0;i<coefficients.length;i++){
        tmp[(x.length+coefficients.length)+i]=x[x.length-i-1];
    }
    
    
    fft.evaluate( tmp, transform  );
    
    Arrays.fill( tmp, 0.0f );
    System.arraycopy( coefficients, 0, tmp, 0, coefficients.length );
    fft.evaluate( tmp, kernel );
    
    RDFT.dftProduct( kernel, transform, 1.0f );
    fft.evaluateInverse( transform, tmp );
    
    // trim off trailing zeros
    
    kernel = new double[ x.length];
    System.arraycopy( tmp, (int)(1.5*coefficients.length+0.5), kernel, 0, x.length );
    
    return kernel;
  }
  
  
}
