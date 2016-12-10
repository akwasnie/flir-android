/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.gda.pg.eti.dbe.waveform;

import java.text.DecimalFormat;

/**
 *
 * @author jwr
 */
public class HistogramDescriptors {
    
    public static int[] getHistogram(int[] roiData, int c){
        int[] rgb = new int[3];
        
        int value;
        int [] hist=new int[256];
        int offset=c*8; //R->c=2->offset=16; G->c=1, B-> c=0
        double[] res = new double[3];
        for (int i = 0; i < roiData.length; i++) {
            
            value=(roiData[i] >> offset) & 0xff;
            hist[value]++;
        }
       
        return hist;
    }
    
    
    /**
	 * (double []) d_stat_1st_i() - metod oblicza deskryptory statystyczne
	 * pierwszego stopnia na podstawie histogramu liczb ca�kowitych. 
	 * Deskryptory s� przechowywane w tablicy.
	*/
	public static double[] stat_1st(int [] hist){

		double mean=0.0;
		double var=0.0;
		double skew=0.0;
		double kurtosis =0.0;
		double energy=0.0;
		double entropy=0.0;
		double elim_DC=0.0;
                double min=Double.MAX_VALUE;
		double des []= new double[7];
                double []hist_i = new double[hist.length];
                
                double N=0;
                
                for(int i=0; i<hist.length; i++){
                    N+=hist[i];
                    if(hist[i]>0 && i<min) min=i;
                }
                
                for(int i=0; i<hist.length; i++){
                    hist_i[i]=(double)hist[i]/N;
                    
                }
                
	
		for (int i = 0; i < hist.length; i++){
			
			mean += hist_i[i] * i;
			energy += hist_i[i] *hist_i[i];
			if (hist_i[i] != 0)
				entropy += -1*hist_i[i] * log2(hist_i[i]);
		}
		//System.out.println("Srednia= "+srednia);

		double elim_DC_2=0.0;
		for (int i = 0; i < hist.length; i++){
			elim_DC = (double)i - mean;
			elim_DC_2=elim_DC*elim_DC;
			var += elim_DC_2 * hist_i[i];
			skew += elim_DC_2 * elim_DC * hist_i[i];
			kurtosis += elim_DC_2 * elim_DC_2 * hist_i[i];
		}

                //skewness = [n / (n -1) (n - 2)] sum[(x_i - mean)^3] / std^3
                
		skew /= var * Math.sqrt(var);
		kurtosis = (kurtosis/(var*var)) - 3.0;
		
		des[0]=mean;
		des[1]=var;
		des[2]=skew;
		des[3]=kurtosis;
		des[4]=energy;
		des[5]=entropy;	
                des[6]=min;
		for (int o=0; o<des.length; o++)
			System.out.println("DES ["+o+"]="+new DecimalFormat("#0.0000").format(des[o]));
	
		return des;
	}// koniec public double[] d_stat_1st_i(){
        
        
        public static double  log2(double d) { 
            return Math.log(d)/Math.log(2.0); 
        }
    
}
