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
public class FilterUsingCoeff {
      /**
     * bhm = fir1(64,[0.08 0.48],hamming(64+1));
     */
  private static double [] bandPass0_6_to_3_6_hamming64= {-0.001474422541094352,-0.0004509614429506483,-2.089184551898957e-06,-0.001234020844973777,-0.00206608234497564,-0.0003184552162750516,0.001576122632879778,3.88546384891137e-07,-0.001933896152542998,0.001673407034384744,0.007091617222865363,0.005204156592530343,6.105505670960046e-06,0.004676392311218506,0.01509607451031898,0.01219521502629963,-0.001403337071641021,4.082383048818632e-06,0.0160950760903362,0.01220341869155172,-0.01656167781028775,-0.02512099643244326,-4.246888823375497e-06,0.001211883461460646,-0.04879272545905641,-0.07657776249120726,-0.02991191449358062,-3.852617790809881e-06,-0.08428430479212093,-0.1723627625180652,-0.05438238167609718,0.2387282105467999,0.3997561754771806,0.2387282105467999,-0.05438238167609718,-0.1723627625180652,-0.08428430479212093,-3.852617790809907e-06,-0.02991191449358062,-0.07657776249120728,-0.04879272545905642,0.001211883461460646,-4.246888823376618e-06,-0.02512099643244326,-0.01656167781028776,0.01220341869155172,0.01609507609033621,4.082383048819011e-06,-0.001403337071641021,0.01219521502629964,0.01509607451031898,0.004676392311218507,6.105505670959798e-06,0.005204156592530346,0.007091617222865368,0.001673407034384744,-0.001933896152542999,3.88546384890358e-07,0.001576122632879778,-0.0003184552162750518,-0.002066082344975641,-0.001234020844973777,-2.089184551899103e-06,-0.0004509614429506487,-0.001474422541094352};
    
    /** double[] containing the FIR filter coefficients */
  protected double[]            coefficients;      // filter coefficients
  
  /** int containing the number of filter coefficients */
  protected int                Nc;                // number of coefficients
  
  
  public FilterUsingCoeff(double [] coeff){
      coefficients=coeff.clone();
      Nc=coefficients.length;
  }
  
  public FilterUsingCoeff(){
      coefficients=bandPass0_6_to_3_6_hamming64.clone();
      Nc=coefficients.length;
  }
  
  
  private static double[] computeFreqDomainCoeffs(double samplingRateHz, int length, double cutoffFreqHz, int numPoles, boolean isLowPass) 
 { 
  double[] coeffs = new double[length]; 
  
  double deltaHz = samplingRateHz / ((double) length); 
 
  double fRatio; 
  coeffs[0] = isLowPass ? 1.0 : 0.0; 
  fRatio = deltaHz * (length / 2) / cutoffFreqHz; 
  if (!isLowPass) { 
   fRatio = 1.0 / fRatio; 
  } 
  coeffs[length / 2] = 1.0 / Math.hypot(1.0, pow(fRatio, 2 * numPoles)); 
  for (int i = 1; i < length / 2; i++) { 
   fRatio = i * deltaHz / cutoffFreqHz; 
   if (!isLowPass) { 
    fRatio = 1.0 / fRatio; 
   } 
   coeffs[i] = 1.0 / Math.hypot(1.0, pow(fRatio, 2 * numPoles)); 
   coeffs[length - i] = coeffs[i]; 
  } 
 
  return coeffs; 
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
  
  
  public static double [] filter(double [] waveform, double samplingRateHz, double cutoffFreqHz, int numPoles, boolean isLowPass){
    
    int nfft     = 16;
    int log2nfft = 4;
    int n        = waveform.length  ;
    
    while ( nfft < n ) {
      nfft *= 2;
      log2nfft++;
    }
    
    int paddedWidth = nfft; 
 
    // compute filter coefficients for padded waveforms 
    double[] filterCoeffs = computeFreqDomainCoeffs(samplingRateHz, paddedWidth, cutoffFreqHz, numPoles, isLowPass); 
 
  
 
 
   // initialize real and imaginary temporary arrays for padded data 
   double[] re = new double[paddedWidth]; 
   double[] im = new double[paddedWidth]; 
 
      // copy original values into temporary padded real array (imaginary portion is already initialized to zero)
   System.arraycopy(waveform, 0, re, 0, waveform.length); 
 
   // filter 
   fftComplexPowerOf2(re, im, true); 
   
   
   for (int j = 0; j < paddedWidth; j++) { 
    re[j] *= filterCoeffs[j]; 
    im[j] *= filterCoeffs[j]; 
   } 
   
   fftComplexPowerOf2(re, im, false); 
 
      // copy data into original waveform array, truncating at original width
    System.arraycopy(re, 0, waveform, 0, waveform.length); 
    
    return waveform;
  
  }
  
  public static final double pow(double a, int b) 
 { 
  if (b < 0.0) { 
   a = 1.0 / a; 
   b *= -1; 
  } 
  double result = 1.0; 
  while (b != 0) { 
   if ((b & 1) == 1) { 
    result *= a; 
   } 
   b >>= 1; 
   a *= a; 
  } 
 
  return result; 
 } 
  
  
  /**
   * Based on http://www.programcreek.com/java-api-examples/index.php?source_dir=Waveform-Analysis-for-ImageJ-master/src/waveformAnalysisForImageJ/WaveformUtils.java
   */
  
  //--------------------FFT Methods-----------------------------------------// 
 /**
  * Computes complex FFT of real and imaginary input arrays (in place). For 
  * efficiency, no error checking is performed on real and imaginary input 
  * array lengths. The length of the input array range <b>must</b> be 
  * identical and equal to a power of 2, otherwise a runtime exception may be 
  * thrown. Normalization is consistent with NI LabVIEW® FFT 
  * implementation. 
  * 
  * @param ar        input array containing the real part of the waveform 
  * @param ai        input array containing the imaginary part of the 
  *                  waveform 
  * @param isForward true for forward FFT, false for inverse FFT 
  */ 
 public static final void fftComplexPowerOf2(double ar[], double ai[], boolean isForward) 
 { 
  fftComplexPowerOf2(ar, ai, 0, ar.length, isForward); 
 } 
 
 /**
  * Computes complex FFT of real and imaginary input arrays (in place) within 
  * the specified range. For efficiency, no error checking is performed on 
  * input data range limits or length of real and imaginary input arrays. The 
  * length of the input array range <b>must</b> be identical and equal to a 
  * power of 2, otherwise a runtime exception may be thrown. Normalization is 
  * consistent with NI LabVIEW® FFT implementation. 
  * 
  * 
  * @param ar        input array containing the real part of the waveform 
  * @param ai        input array containing the imaginary part of the 
  *                  waveform 
  * @param from      initial index of the range to compute the FFT, inclusive 
  * @param to        final index of the range to compute the FFT, exclusive 
  * @param isForward true for forward FFT, false for inverse FFT 
  */ 
 public static final void fftComplexPowerOf2(double ar[], double ai[], int from, int to, boolean isForward) 
 { 
  int n = to - from; 
  double scale = 1.0; 
  double c = -Math.PI; 
  if (!isForward) { 
   scale /= n; 
   c *= -1; 
  } 
  int i, j; 
  for (i = j = 0; i < n; ++i) { 
   if (j >= i) { 
    int ii = i + from; 
    int jj = j + from; 
    double tempr = ar[jj] * scale; 
    double tempi = ai[jj] * scale; 
    ar[jj] = ar[ii] * scale; 
    ai[jj] = ai[ii] * scale; 
    ar[ii] = tempr; 
    ai[ii] = tempi; 
   } 
   int m = n / 2; 
   while (m >= 1 && j >= m) { 
    j -= m; 
    m /= 2; 
   } 
   j += m; 
  } 
 
  int mmax, istep; 
  double delta, alpha, beta, temp, cosMDelta, cosMDeltaPlusOne, sinMDelta, sinMDeltaPlusOne, tr, ti; 
  for (mmax = 1, istep = 2 * mmax; mmax < n; mmax = istep, istep = 2 * mmax) { 
 
   // Set up trig recursions 
   delta = c / (double) mmax; 
   beta = Math.sin(delta); 
   temp = Math.sin(delta / 2.0); 
   alpha = 2.0 * temp * temp; 
   cosMDelta = 1.0; 
   sinMDelta = 0.0; 
 
   for (int m = 0; m < mmax; ++m) { 
    for (i = m; i < n; i += istep) { 
     j = i + mmax; 
     int ii = i + from; 
     int jj = j + from; 
     tr = cosMDelta * ar[jj] - sinMDelta * ai[jj]; 
     ti = cosMDelta * ai[jj] + sinMDelta * ar[jj]; 
     ar[jj] = ar[ii] - tr; 
     ai[jj] = ai[ii] - ti; 
     ar[ii] += tr; 
     ai[ii] += ti; 
    } 
    cosMDeltaPlusOne = cosMDelta - (alpha * cosMDelta + beta * sinMDelta); 
    sinMDeltaPlusOne = sinMDelta - (alpha * sinMDelta - beta * cosMDelta); 
    cosMDelta = cosMDeltaPlusOne; 
    sinMDelta = sinMDeltaPlusOne; 
   } 
  } 
 } 
  
}
