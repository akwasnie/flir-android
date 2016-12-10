/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.gda.pg.eti.dbe.dsp;

/**
 * N is filter length, h  is kernel coefficients, y is output sample, 
 * x is input data buffer, iWrite and iRead are indexes for x assess. 
 * Method filter() performs filtration
 * based on http://shulgadim.blogspot.com/2012/07/fir-filter-programming-and-testing.html
 * @author jwr
 */
public class FirFilter{
    
     private int N;
     private double h[];
     private double y;
     private double x[];
     private int n;
     private int iWrite = 0;
     private int iRead = 0;
    
     public FirFilter(double h[]){
          this.h = h;
          this.N = h.length;            
          this.x = new double[N];                                      
     }

     public double filter(double newSample){       
          y = 0;           
          x[iWrite] = newSample;      
          iRead = iWrite;
          for(n=0; n<N; n++){
                y += h[n] * x[iRead];
               iRead++;
               if(iRead == x.length){
                    iRead = 0;
               }
          }
          iWrite--;
          if(iWrite < 0){
                iWrite = x.length-1;
          }      
          return y;
     }   
     
     /*
     public static void main(String[] args){                       
         int M = 512;       //signal length
         double Fs = 250.0;  //sample frequency
         double dt = 1.0/Fs; //sample period in time domain           
         double t[] = new double[M];  //time vector
         for(int i=0; i<M; i++){
         t[i] = dt*i;
         }                              
         double x[]= new double[M]; //input signal
         x[0] = 1.0;                         
         new Plot("Input signal", t, x, "t, sec", "x, Volts");                           
         double h[] = {0.2, 0.2, 0.2, 0.2, 0.2};
         FirFilter firFilter = new FirFilter(h);                             
         double y[] = new double[M];  //output signal           
         for(int i=0; i<M; i++){
         y[i] = firFilter.filter(x[i]);
         }                                
         new Plot("Output signal", t, y, "t, sec", "x, Volts");        
         Spectrum spectrum = new Spectrum(y,Fs);        
         new Plot("Spectrum", spectrum.getF(),                     
                   spectrum.getFreqResponse(), "f, Hz", "S, dB");
     }
     
     */
}
