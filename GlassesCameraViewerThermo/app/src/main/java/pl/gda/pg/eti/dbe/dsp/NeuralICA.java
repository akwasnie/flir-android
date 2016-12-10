/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * Based on Independent Component Analysis. ICA Neural Network. Java implementation
 * http://shulgadim.blogspot.com/2014/02/independent-component-analysis-ica.html
 */

package pl.gda.pg.eti.dbe.dsp;

/**
 *
 * @author jwr
 */
import java.util.Random;

public class NeuralICA {
    private double [][] W;  
    private double [] y;        
    private double[][]I;            
    private double eta = 0.01;
        
    public NeuralICA(int N){        
        I = new double[N][N];
        for(int i=0; i<I.length; i++){              
            for(int j=0; j<I[0].length; j++){
                if(i==j){
                    I[i][j] = 1.0;
                }
            }
        }                   
        W = new double[N][N];           
        for(int i=0; i<N; i++){             
            for(int j=0; j<N; j++){     
                if(i==j){
                    W[i][j] = 1.0;
                }
            }
        }                       
        y = new double[N];
    }
    
    
    public double[] ica(double [] x){               
        y = mult(W,x);              
        update(W,  mult(dW(F(y),G(y)), W ) );               
        return y;
    }
    
    public  double[] mult (double[][]a, double []b){
        int M = a.length;
        int N = a[0].length;
        double[] y = new double[M];
        for(int i=0; i<M; i++){                 
            for(int j=0; j<N; j++){
                y[i] += a[i][j]*b[j];
            }           
        }
        return y;
    }
 
    
    public  double[][] sum (double[][]a, double [][]b){
        int M = a.length;
        int N = a[0].length;        
        double[][] c = new double[M][N];
        for(int i=0; i<M; i++){                 
            for(int j=0; j<N; j++){
                c[i][j] = a[i][j] + b[i][j];
            }           
        }   
        return c;
    }

    public double[][] mult( double[][] a, double[][] b ){
        int m = a.length;
        int n = a[0].length;
        int o = b[0].length;
        double[][] matres = new double[m][];
        for ( int i = 0; i < m; ++i ){
            matres[i] = new double[o];
            for ( int j = 0; j < o; ++j ){
                matres[i][j] = 0.0f;
                for ( int k = 0; k < n; ++k )
                    matres[i][j] += a[i][k] * b[k][j];
            }
        }
        return(matres);
    }
    
    public  void update (double[][]a, double [][]b){
        int M = a.length;
        int N = a[0].length;        
        for(int i=0; i<M; i++){                 
            for(int j=0; j<N; j++){
                a[i][j] += b[i][j];
            }           
        }   
    }
    
    private double[] F(double[] y){     
        double[] f = new double[y.length];
        for(int i=0; i<y.length; i++){          
            f[i] = y[i];
        }       
        return f;       
    }
    
    private double[] G(double[] y){     
        double[] g = new double[y.length];
        for(int i=0; i<y.length; i++){          
            g[i] = Math.tanh(10*y[i]);          
        }       
        return g;
    }
    
    private double[][] dW(double[]f, double[]g){                
        double[][] A = new double[f.length][f.length];
        for(int i=0; i<f.length; i++){              
            for(int j=0; j<f.length; j++){              
                A[i][j] = eta*(I[i][j] - f[i]*g[j]);                
            }
        }                       
        return A;
    }
    
    public static void main(String args[]){
        int K = 5000;       
        double [][] s = new double[3][K];   
        double dt = 0.001;      
        Random rand = new Random();
        for(int k=0; k< K; k++){
            s[0][k] = rand.nextGaussian();        
            s[1][k] = 
            Math.pow(10, 0)*Math.sin(500*dt*k)*Math.sin(30*dt*k);
            s[2][k] = Math.pow(10, 0)*Math.sin(234*dt*k);
            
        }
        //new MultiPlot("s",s);
        double[][] A = {{0.20, -0.61, 0.62},
                          {0.91, -0.89, -0.33},
                          {0.59,  0.76, 0.60}};             
        double [][] x = new double[3][K];       
        for(int k=0; k<K; k++){
            for(int i=0; i<3; i++){             
                for(int j=0; j<3; j++){                                     
                    x[i][k] = x[i][k] + A[i][j]*s[j][k];                                                                        
                }
            }
            
        }       
       // new MultiPlot("x",x);
        NeuralICA ica = new NeuralICA(3);       
        double [][] y = new double[3][K];       
        for(int k=0; k<K; k++){
            double[] res = ica.ica(getCol(x,k));
            setCol(y,res,k);
            
        }       
       // new MultiPlot("y",y);
    }
    
    public static double[] getCol(double [][] a, int col){
        int M = a.length;
        double [] y = new double[M];
        for(int i =0; i<M; i++){
            y[i] = a[i][col];
        }
        return y;
    }
    
    public static void setCol(double [][] a, double[] b, int col){
        int M = b.length;       
        for(int i =0; i<M; i++){
            a[i][col] = b[i];
        }       
    }
}