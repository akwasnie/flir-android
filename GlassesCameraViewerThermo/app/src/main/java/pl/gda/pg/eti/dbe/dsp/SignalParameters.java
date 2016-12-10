/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.gda.pg.eti.dbe.dsp;

/**
 *
 * @author jwr
 */
public class SignalParameters {
    
    
    public static double timeAverage(double []y){
        double average=0;
        for(double value:y){
            average+=value;
        }
        return (average/y.length);
    }
    
    
    public static double timeVariance(double []y, double average){
        double var=0;
        for(double value:y){
            var+=(value-average)*(value-average);
        }
        return (var/(y.length-1));
    }
    
    /**
     * Power estimator 1/T Sum(v^2) from 0 to T (y.length in practice)
     * @param y
     * @return 
     */
    public static double power(double []y){
        double res=0;
        for(double value:y){
            res+=(value)*(value);
        }
        return (res/(y.length));
    }
    
    /**
     * Energy estimator  Sum(v^2) from 0 to infinity (y.length in practice)
     * @param y
     * @return 
     */
    public static double energy(double []y){
        double res=0;
        for(double value:y){
            res+=(value)*(value);
        }
        return (res);
    }
    
    public static double [] highest2Peaks(double []y){
        double res[]=new double [2];
        double max1=-Double.MAX_VALUE;
        double max2=-Double.MAX_VALUE;
        
        for(double value:y){
            if(value>max1){
                if(max1>=max2)
                    max2=max1;
                max1=value;
            }else if(value>max2){
                max2=value;
            }
        }
        res[0]=max1;
        res[1]=max2;
        return (res);
    }
    
    public static double getPulse(double [] pds,double fs,double N){
        double pulse=0;
        double maxValue=Double.MIN_VALUE;
        int index=0;
        for(int i=0; i<pds.length; i++){
            if(pds[i]>maxValue){
                index=i;
                maxValue=pds[i];
                
            }
        }
        System.out.println("Index= "+index+", max= "+maxValue);
        pulse=(double)index*60*fs/N;
        return pulse;
    }    
    
    public static double peakEnergyToTotalEnergy(double []y){
        double total=energy(y);
        double peak=highest2Peaks(y)[0];
        
        return ((peak*peak)/total);
    }
    
    public static double twoPeakEnergyToTotalEnergyDifference(double []y){
        double total=energy(y);
        double []peaks=highest2Peaks(y);
        
        return (((peaks[0]*peaks[0])-(peaks[1]*peaks[1]))/total);
    }
    
}
