/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.gda.pg.eti.dbe.dsp;

import java.util.Arrays;

/**
 *
 * @author jwr
 */
public class TimeDomainFilters {
    
    public final static int MINMAX=100;
    public final static int MIN=110;
    public final static int MAX=120;
    public final static int AVG=400;
    public final static int DER=800;
    public final static int DER2=850;
    public final static int BASELINE=1000;
    public final static int BASELINE_REMOVE=1100;
    public final static int SLOPE_DETECTOR=2000;
    
    public final static int EDGE_NONE=0;
    public final static int EDGE_FILL_0=10;
    public final static int EDGE_SYMETRY_0=20;
    
    int type;
    int size;
    
    
    private static int edgeCondition=EDGE_SYMETRY_0;
    
    public TimeDomainFilters(int type, int size){
        this.type=type;
        this.size=size;
    }
    
    public double [] filter(double in[], double [] out){
        switch(type){
          
            case MINMAX:
                filterMinMax(in,out,size);
                break; 
            case MIN:
                filterMin(in,out,size);
                break;     
            case AVG:
                filterAvg(in,out,size);
                break; 
            case DER:
                filterDer(in,out);
                break;  
            case DER2:
                filterDer2(in,out);
                break;    
            case BASELINE:
                baseLineRemove(in,out);
                break;  
            case BASELINE_REMOVE:
                baseLineRemove(in,out);
                break; 
            case SLOPE_DETECTOR:
                slopeDetector(in,out,0.33);
                break;        
            default:
                System.arraycopy(in, 0, out, 0, in.length);
                break;
                
        };
        return out;
    }
    
    private double [] filterMin(double in[], double [] out, int size){
       
        double max=-Double.MAX_VALUE;
        double min=Double.MAX_VALUE;
        int center=size/2;
        
        for(int i=center;i<in.length-center; i++){
            min=Double.MAX_VALUE;
            for(int k=0,j=i-center;k<size;j++,k++){
                if(in[j]<min) min=in[j];
                
            }
            
            out[i]=min;
        }
        
        for(int i=0; i<=center;i++){
            out[i]=in[i];
        }
        
        for(int i=in.length-center; i<in.length;i++){
            out[i]=in[i];
        }
            
       
        return out;
    }
    
    private double [] filterMinMax(double in[], double [] out, int size){
       
        double max=-Double.MAX_VALUE;
        double min=Double.MAX_VALUE;
        int center=size/2;
        
        for(int i=center;i<in.length-center; i++){
            min=Double.MAX_VALUE;
            for(int k=0,j=i-center;k<size;j++,k++){
                if(in[j]<min) min=in[j];
                
            }
            
            out[i]=min;
        }
        
        double [] tmp=in.clone();
        
        for(int i=center;i<in.length-center; i++){
            max=-Double.MAX_VALUE;
            for(int k=0,j=i-center;k<size;j++,k++){
                if(out[j]>max) max=out[j];
                
            }
            
            tmp[i]=max;
        }
            
        out=tmp;
        for(int i=0; i<=center;i++){
            out[i]=in[i];
        }
        
        for(int i=in.length-center; i<in.length;i++){
            out[i]=in[i];
        }
        return out;
    }
    
    private double [] filterAvg(double in[], double [] out, int size){
       
        double avg=0;
     
        double [] tmp;
        int center=size/2;
        System.out.println("CENTER:"+center+", SIZE:"+size);
        
        if(edgeCondition==EDGE_SYMETRY_0){
            tmp=new double[out.length+2*center];
            double []inExt=new double[out.length+2*center];
            for(int i=0;i<in.length; i++){
                inExt[i+center]=in[i];
            }
            for(int i=0;i<center;i++){
                inExt[i]=in[center-i];
            }
            for(int i=in.length;i<inExt.length;i++){
                inExt[i]=in[in.length-(i-in.length)-1];
            }
            for(int i=center;i<inExt.length-center; i++){
                avg=0;
                for(int k=0,j=i-center;k<size;j++,k++){
                    avg+=inExt[j];

                }
                tmp[i]=avg/(double)size;
            }
            System.arraycopy(tmp, center, out, 0, out.length);
            
        }else //if(edgeCondition==EDGE_FILL_0)
        {
            tmp=new double[out.length];
            for(int i=center;i<in.length-center; i++){
                avg=0;
                for(int k=0,j=i-center;k<size;j++,k++){
                    avg+=in[j];

                }
                tmp[i]=avg/(double)size;


            }
            System.arraycopy(tmp, 0, out, 0, tmp.length);
        }
        /*
        for(int i=0; i<=center;i++){
            out[i]=(in[i]+tmp[center])/2;
        }
        
        for(int i=in.length-center; i<in.length;i++){
            out[i]=(in[i]+tmp[in.length-center-1])/2;
        }
        */
        return out;
    }
    
    
    private double [] filterDer(double in[], double [] out){
       
        double [] tmp=new double[out.length];
        
        
        
        for(int i=1;i<in.length-1; i++){
            
            tmp[i]=(in[i+1]-in[i-1])/2;
            
        }
        
        System.arraycopy(tmp, 0, out, 0, tmp.length);
        
        out[0]=out[1];
        out[in.length-1]=out[in.length-2];
        
        return out;
    }
    
    private double [] filterDer2(double in[], double [] out){
       
        
     
        double [] tmp=new double[out.length];
        
        
        for(int i=1;i<in.length-1; i++){
            
            tmp[i]=(in[i+1]-2*in[i]+in[i-1]);
            
        }
        System.arraycopy(tmp, 0, out, 0, tmp.length);
        return out;
    }
    
    
    private double [] slopeDetector(double in[], double [] out, double scale ){
       
        
     
        double [] tmp=new double[out.length];
        double acc=0;
        int start=-1;
        int stop=1;
        
        double min=Double.MAX_VALUE;
        double max=-Double.MIN_VALUE;
        for(int i=0;i<in.length; i++){
            if(in[i]>max) max=in[i];
            if(in[i]<min) min=in[i];
            
        }
        System.out.println("MAX= "+max+", MIN= "+min);
        double threshold=scale*(max-min);
        double diff=0;
        
        for(int i=1;i<in.length-1; i++){
            if(in[i]>in[i-1]){
                //acc++;
                if(start<0) {
                    start=i-1;
                }
                stop=i;
            }else{
                if(start<=0) continue;
                diff=(in[stop]-in[start]);
                if(diff>threshold){
                    //tmp[start]=in[start];
                    //tmp[stop]=in[stop];
                    tmp[start]=10;
                    tmp[stop]=20;
                    
                }
                start=-1;
                stop=i;
            }
            
            
        }
        System.arraycopy(tmp, 0, out, 0, tmp.length);
        return out;
    }
    
    
    public static double simpleSlopeDetector3(double sig[], double max, double min , double scale){
        
        double threshold=scale*(max-min);
	double diff=0;
    int start=0;
    int stop=0;  
    int prevstop=0;
   
    int kk=0;
    double sum1=0;
    double res=0;
    double [] maxPeaksPeriods= new double [40];
    
    double [] tmp=new double [sig.length];
    
    for(int i = 1; i<sig.length; i++){
    	if(sig[i]>sig[i-1]){
                if(start<=0) 
                    start=i-1;
                
                stop=i;
        }else {
                if(start<=0) 
                	continue;
                	
                diff=(sig[stop]-sig[start]);
                if(diff>threshold){
                    tmp[start]=min;
                    tmp[stop]=max;
                    if(prevstop>0){
                    	maxPeaksPeriods[kk]=stop-prevstop;
                	//System.out.printf("MaxPeaksPeriod: %f, for i= %d\n",maxPeaksPeriods[kk],i); 
                	//sum1+=maxPeaksPeriods[kk];
                	kk=kk+1;
                	//prevstop=stop;
                    }
                    prevstop=stop;
                    
                    
                }
              
                
                start=0;
                stop=i;
                
        }
    }   
	
        for(int i=0; i<tmp.length; i++){
            if(tmp[i]!=0){
                //System.out.println("i="+i+", value="+tmp[i]);
            }
        }
	
        kk=0;
        for(int i=0; i<maxPeaksPeriods.length;i++){
            if(maxPeaksPeriods[i]>0){
                sum1+=maxPeaksPeriods[i];
                ++kk;
            }else break;
        }
        System.out.println("SUM1= "+sum1+", kk="+kk);
        res=sum1/(kk);
        return res;
        
    }//simpleSlopeDetector3
    
    private double [] baseLineRemove(double in[], double [] out, int avgWindow, double tVar){
       double [] tmp=new double[out.length];
       double cMean=0;
       double cVar=Double.MAX_VALUE; 
       boolean firstPASS=true;
       double TVar=0.01;
       edgeCondition=EDGE_FILL_0;
       System.arraycopy(in, 0, tmp, 0, tmp.length);
       do{
           Arrays.fill(out, 0);
           filterAvg(tmp, out, avgWindow);
           System.arraycopy(out, 0, tmp, 0, tmp.length);
           Arrays.fill(out, 0);
           filterDer(tmp, out);
           //System.arraycopy(out, 0, tmp, 0, tmp.length);
           cMean=SignalParameters.timeAverage(out);
           cVar=SignalParameters.timeVariance(out, cMean);
           if(firstPASS){
               firstPASS=false;
               TVar=tVar*cVar;
           }
           //System.arraycopy(tmp, 0, in, 0, tmp.length);
           System.out.println("cVAR= "+cVar+ ", tVar="+TVar);
       }while(cVar>TVar); 
        
        System.arraycopy(tmp, 0, out, 0, tmp.length);
        edgeCondition=EDGE_SYMETRY_0;
        return out;
    }
    
    private double [] baseLineRemove(double in[], double [] out){
       return (baseLineRemove(in,out,3,0.02));
    }
    
    
    
    /**
     * Computes the cross correlation between sequences a and b.
     * maxlag is the maximum lag to
     */
    public static double[] xcorr(double[] a, double[] b, int maxlag)
    {
        double[] y = new double[2*maxlag+1];
        Arrays.fill(y, 0);
        
        for(int lag = b.length-1, idx = maxlag-b.length+1; 
            lag > -a.length; lag--, idx++)
        {
            if(idx < 0)
                continue;
            
            if(idx >= y.length)
                break;

            // where do the two signals overlap?
            int start = 0;
            // we can't start past the left end of b
            if(lag < 0) 
            {
                //System.out.println("b");
                start = -lag;
            }

            int end = a.length-1;
            // we can't go past the right end of b
            if(end > b.length-lag-1)
            {
                end = b.length-lag-1;
                //System.out.println("a "+end);
            }

            //System.out.println("lag = " + lag +": "+ start+" to " + end+"   idx = "+idx);
            for(int n = start; n <= end; n++)
            {
                //System.out.println("  bi = " + (lag+n) + ", ai = " + n); 
                y[idx] += a[n]*b[lag+n];
            }
            //System.out.println(y[idx]);
        }

        return(y);
    }
    
    
    
    public static double [] calculateSpectralMoments(double [] sig){
       
        double [] moments= new double[3];
        //0 - mobility
        //1 - complexity
        //2 - purity
      
        double [] dxV = diff(sig);
        double [] ddxV = diff(dxV);

      
        double m0=mean(sqr(sig));
        double m2 = mean(sqr(dxV));
        double m4= mean(sqr(ddxV));


        double mob=m2/m0;
        double mobility=Math.sqrt(mob);


        double complexity = Math.sqrt((m4 / m2) - mob);

        double purity=(m2*m2)/(m0*m4);
        
        moments[0]=mobility;
        moments[1]=complexity;
        moments[2]=purity;
        System.out.println("MOMENTS: "+Arrays.toString(moments));
        return moments;

    }
    
    // getting the maximum value
    public static double getMaxValue(double[] array){  
          double maxValue = array[0];  
          for(int i=1;i < array.length;i++){  
          if(array[i] > maxValue){  
          maxValue = array[i];  

             }  
         }  
                 return maxValue;  
    }  

    // getting the miniumum value
    public static double getMinValue(double[] array){  
         double minValue = array[0];  
         for(int i=1;i<array.length;i++){  
         if(array[i] < minValue){  
         minValue = array[i];  
            }  
         }  
        return minValue;  
    }  
    
    public static double [] diff(double [] in){
        double [] tmp=new double[in.length-1];
     
        for(int i=0;i<in.length-1; i++){
            
            tmp[i]=(in[i+1]-in[i]);
            
        }
        
        return tmp;
    }
    
    public static double [] sqr(double [] in){
        double [] tmp=new double[in.length];
     
        for(int i=0;i<in.length; i++){
            
            tmp[i]=(in[i]*in[i]);
            
        }
        
        return tmp;
    }
    
    public static double mean(double [] in){
        double [] tmp=new double[in.length];
        double mean=0;
        for(int i=0;i<in.length; i++){
            
            mean+=in[i];
            
        }
        
        return mean/in.length;
    }
    
    
    private double [] filterKalman(double in[], double out[]){
        int N = 10; // Number of measurements

        // measurements with mean = .5, sigma = .1;
        double z[] = 
        {   
            0, // place holder to start indexes at 1
            0.3708435, 0.4985331, 0.4652121, 0.6829262, 0.5011293, 
            0.3867151, 0.6391352, 0.5533676, 0.4013915, 0.5864200
        };

        double Q = .000001, // Process variance
            R = .1*.1;// Estimation variance

        double[] xhat = new double[N+1],// estimated true value (posteri)
        xhat_prime = new double[N+1],   // estimated true value (priori)
        p = new double[N+1],    // estimated error (posteri)
        p_prime = new double[N+1],// estimated error (priori)
        k = new double[N+1];    // kalman gain

        double cur_ave = 0;

        // Initial guesses
        xhat[0] = 0; 
        p[0] = 1;

        for(int i = 1; i <= N; i++) {
            // time update
            xhat_prime[i] = xhat[i-1];
            p_prime[i] = p[i-1] + Q;

            // measurement update
            k[i] = p_prime[i]/(p_prime[i] + R);
            xhat[i] = xhat_prime[i] + k[i]*(z[i] - xhat_prime[i]);
            p[i] = (1-k[i])*p_prime[i];

            // calculate running average
            cur_ave = (cur_ave*(i-1) + z[i])/((double)i);

            System.out.printf("%d\t%04f\t%04f\t%04f\n", i, z[i], xhat[i], cur_ave);
        }
        
        return out;
    }
    
}
