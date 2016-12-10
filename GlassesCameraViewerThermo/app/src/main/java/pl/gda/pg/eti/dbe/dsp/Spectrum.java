/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.gda.pg.eti.dbe.dsp;
import DSP.HammingWindow;
import DSP.HanningWindow;
import DSP.Window;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import java.util.Arrays;
/**
 *
 * @author jwr
 */
public class Spectrum{   
     private double signal[];
     private int N;
     private double Fs;
     private double df;
     private double f[]; 
     private double S[]; 
     public double min,max;
     public Spectrum(double signal[], double Fs){       
          this.N = signal.length;       
          this.signal = new double[N];
          System.arraycopy( signal, 0, this.signal,
                             0, signal.length );        
          this.Fs = Fs;       
          this.df = this.Fs/N;     
          f = new double[N/2];
          for(int i=0; i<N/2; i++){
          f[i] = df*i;
          }      
          S = new double[N/2];
          absfft();
     }   
     
     
     public Spectrum(double signal[], double Fs, int windowType, int noOfSamples){       
                 
          this.Fs = Fs;       
          double []xw=new double[signal.length];
          Window hw;
          if(windowType==1){
             hw=new HanningWindow(signal.length);
             hw.window(signal, 0, xw);
          } else if(windowType==2){
             hw=new HammingWindow(signal.length);
             hw.window(signal, 0, xw);
          }else{
              System.arraycopy( signal, 0, xw,
                             0, signal.length );
          }
          
         
          
          this.N = noOfSamples;       
          this.signal = new double[noOfSamples];
          System.arraycopy( xw, 0, this.signal,
                             0, xw.length ); 
          this.df = this.Fs/N;     
          f = new double[N/2];
          for(int i=0; i<N/2; i++){
            f[i] = df*i;
          }      
          S = new double[N/2];
          absfft();
         
     }
     
     private void fft(){                                
         DoubleFFT_1D fft = new DoubleFFT_1D(N);
         fft.realForward(signal);            
         for(int i=0; i<N/2; i++){
         S[i] = Math.sqrt(signal[2*i]*signal[2*i] +
                            signal[2*i+1]*signal[2*i+1]);
         }
         double S2[] = new double[S.length];
         System.arraycopy( S, 0, S2, 0, S.length );       
         Arrays.sort(S2);                                   
         for(int i=0; i<N/2; i++){
         S[i] = 20*Math.log10(S[i]/S2[S2.length-1]);
         }
     }  
     
     private void absfft(){          
         min=Double.MAX_VALUE;
         max=Double.MIN_VALUE;
         DoubleFFT_1D fft = new DoubleFFT_1D(N);
         fft.realForward(signal);            
         for(int i=0; i<N/2; i++){
            S[i] = signal[2*i]*signal[2*i]/N;
            
         }
         
         for(int i=0; i<S.length; i++){
            if(S[i]<min) min=S[i];
            if(S[i]>max) max=S[i];
            
         }
         System.out.println("MAX= "+max);
         System.out.println("MIN= "+min);
         
         
     } 
     
     
     public double[] getFreqResponse(){
          return S;
     }
     public double[] getF(){
          return f;
     }  
     public int getN(){
          return N;
     } 
     
}