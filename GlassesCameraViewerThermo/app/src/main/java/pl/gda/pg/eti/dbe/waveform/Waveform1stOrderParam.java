/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.gda.pg.eti.dbe.waveform;

/**
 *
 * @author jwr
 */
public class Waveform1stOrderParam {
    
    
    
    
    public static double skewness(double[] a, int from, int to, boolean isSampleSkewness) { 
        long n = 0; 
        double mean = 0.0; 
        double m2 = 0.0; 
        double m3 = 0.0; 

        for (int i = from; i < to; i++) { 
         n++; 
         double delta = a[i] - mean; 
         double deltaOverN = delta / n; 
         double term1 = delta * deltaOverN * (n-1); 
         mean += deltaOverN; 
         m3 += term1 * deltaOverN * (n - 2) - 3.0 * deltaOverN * m2; 
         m2 += term1; 
        } 

        double output = (m3/n)/Math.pow(m2/n, 1.5); 
        if (!isSampleSkewness) { 
         output *= Math.sqrt(n*(n-1))/(n-2); 
        } 

        return output; 
    }//skewness
    
    
    public static double skewness(int[] a, int from, int to, boolean isSampleSkewness) { 
        long n = 0; 
        double mean = 0.0; 
        double m2 = 0.0; 
        double m3 = 0.0; 

        for (int i = from; i < to; i++) { 
         n++; 
         double delta = a[i] - mean; 
         double deltaOverN = delta / n; 
         double term1 = delta * deltaOverN * (n-1); 
         mean += deltaOverN; 
         m3 += term1 * deltaOverN * (n - 2) - 3.0 * deltaOverN * m2; 
         m2 += term1; 
        } 

        double output = (m3/n)/Math.pow(m2/n, 1.5); 
        if (!isSampleSkewness) { 
         output *= Math.sqrt(n*(n-1))/(n-2); 
        } 

        return output; 
    }//skewness
    
    public static double kurtosis(double[] a, int from, int to) { 
        long n = 0; 
        double mean = 0.0; 
        double m2 = 0.0; 
        double m3 = 0.0; 
        double m4 = 0.0; 

        for (int i = from; i < to; i++) { 
         n++; 
         double delta = a[i] - mean; 
         double deltaOverN = delta / n; 
         double deltaOverNSquared = deltaOverN * deltaOverN; 
         double term1 = delta * deltaOverN * (n-1); 
         mean += deltaOverN; 
         m4 += term1 * deltaOverNSquared * (n*n - 3*n + 3.0) + 6.0 * deltaOverNSquared * m2 - 4.0 * deltaOverN * m3; 
         m3 += term1 * deltaOverN * (n - 2) - 3.0 * deltaOverN * m2; 
         m2 += term1; 
        } 

        return (((n*m4) / (m2*m2)) - 3.0); 
    }//kurtosis
    
    public static double kurtosis(int[] a, int from, int to) { 
        long n = 0; 
        double mean = 0.0; 
        double m2 = 0.0; 
        double m3 = 0.0; 
        double m4 = 0.0; 

        for (int i = from; i < to; i++) { 
         n++; 
         double delta = a[i] - mean; 
         double deltaOverN = delta / n; 
         double deltaOverNSquared = deltaOverN * deltaOverN; 
         double term1 = delta * deltaOverN * (n-1); 
         mean += deltaOverN; 
         m4 += term1 * deltaOverNSquared * (n*n - 3*n + 3.0) + 6.0 * deltaOverNSquared * m2 - 4.0 * deltaOverN * m3; 
         m3 += term1 * deltaOverN * (n - 2) - 3.0 * deltaOverN * m2; 
         m2 += term1; 
        } 

        return (((n*m4) / (m2*m2)) - 3.0); 
    }//kurtosis
    
    public static double var(double[] a, int from, int to) { 
        long n = 0; 
        double mean = 0.0; 
        double m2 = 0.0; 
        

        for (int i = from; i < to; i++) { 
         n++; 
         double delta = a[i] - mean; 
         double deltaOverN = delta / n; 
         double deltaOverNSquared = deltaOverN * deltaOverN; 
         double term1 = delta * deltaOverN * (n-1); 
         mean += deltaOverN; 
       
         m2 += term1; 
        } 

        return m2/n; 
    }//variance
    
    
    public static double var(int[] a, int from, int to) { 
        long n = 0; 
        double mean = 0.0; 
        double m2 = 0.0; 
        

        for (int i = from; i < to; i++) { 
         n++; 
         double delta = a[i] - mean; 
         double deltaOverN = delta / n; 
         double deltaOverNSquared = deltaOverN * deltaOverN; 
         double term1 = delta * deltaOverN * (n-1); 
         mean += deltaOverN; 
       
         m2 += term1; 
        } 

        return m2/n; 
    }//var
    
    public static double min(int[] a, int from, int to) { 
        
        int min=Integer.MAX_VALUE;
        for(int v:a){
            if(v<min) min=v;
        }

        return min; 
    }//min
    
    public static double min(double[] a, int from, int to) { 
        
        double min=Double.MAX_VALUE;
        for(double v:a){
            if(v<min) min=v;
        }

        return min; 
    }//min
}
