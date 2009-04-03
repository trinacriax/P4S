package p4s.util;

/**
 * Class used to generate random numbers with many kind of distribution
 *
 *
 */
import java.math.*;
import peersim.config.*;
import peersim.util.ExtendedRandom;

public class RandomRLC extends ExtendedRandom {

    private static final double c_0 = 2.515517;
    private static final double c_1 = 0.802853;
    private static final double c_2 = 0.010328;
    private static final double d_1 = 1.432788;
    private static final double d_2 = 0.189269;
    private static final double d_3 = 0.001308;
    private final int MODULE;
    private final long MODULE64;
    private final long A;
    private final long LASTXN;
    private final long UPTOMOD;
    private final BigDecimal RATIO;
    private static final String PAR_SEED = "seed";

    public RandomRLC(String prefix) {
        super(Configuration.getLong(prefix + "." + PAR_SEED));
        long seed = Configuration.getLong(prefix + "." + PAR_SEED);
        setSeed(seed);
        MODULE = 2147483647;
        MODULE64 = new Long(Long.MAX_VALUE);
        //A = 16807;
        A = new BigDecimal("7.218551535e13").longValue();
        //LASTXN = 127773;
        LASTXN = new BigDecimal("5.487808563e14").longValue();
        //UPTOMOD = -2836;
        UPTOMOD = new BigDecimal("-1.229649137e13").longValue();
        RATIO = new BigDecimal("1.08420217e-19");
    }
    /*
     **  Function  : long rnd32(long seed)
     **  Return    : the updated value of 'seed'.
     **  Remarks   : congruential generator of pseudorandom sequences of numbers
     **              uniformly distributed between 1 and 2147483646, using the
     **              congruential relation: Xn+1 = 16807 * Xn  mod  2147483647 .
     **              The input to the routine is the integer Xn, while the returned
     **              integer is the number Xn+1.
     */
//public long rnd32(long seed)
//{
// long times, rest, prod1, prod2;
// times = seed / LASTXN;
// rest  = seed - times * LASTXN;
// prod1 = times * UPTOMOD;
// prod2 = rest * A; 
// seed  = prod1 + prod2;
// if(seed < 0) 
//	 seed = seed + MODULE;
// return seed;
//}

    public long rnd64(long seed) {
        long times, rest, prod1, prod2;
        times = seed / LASTXN;
        rest = seed - times * LASTXN;
        prod1 = times * UPTOMOD;
        prod2 = rest * A;
        seed = prod1 + prod2;
        if (seed < 0) {
            seed = seed + MODULE64;
        }
        return seed;
    }

    public long rnd64() {
        long times, rest, prod1, prod2;
        long seed = this.getLastSeed();
        times = seed / LASTXN;
        rest = seed - times * LASTXN;
        prod1 = times * UPTOMOD;
        prod2 = rest * A;
        seed = prod1 + prod2;
        if (seed < 0) {
            seed = seed + MODULE64;
        }
        this.setSeed(seed);
        return seed;
    }


    /*
     **  Function  : double uniform_0_1(long *seed)
     **  Return    : a value uniformly distributed between 0 and 1
     **  Remarks   : the value of '*seed' is changed.
     */
    public double uniform_0_1(long seed) {
        double u;
        //seed = this.rnd32(seed);
        seed = this.rnd64(seed);
        u = (RATIO.multiply(new BigDecimal(seed))).doubleValue();
        return u;
    }

    /*
     **  Function  : double uniform(double a, double b, long *seed)
     **  Return    : a value uniformly distributed between 'a' and 'b'
     **  Remarks   : the value of '*seed' is changed.
     */
    public double uniform(double a, double b, long seed) {
        double u;
        //seed = this.rnd32(seed);
        seed = this.rnd64(seed);
        u = (RATIO.multiply(new BigDecimal(seed))).doubleValue();
        u = a + u * (b - a);
        return u;
    }

    public int uniform(int a, int b) {
        double u;
        //seed = this.rnd32(seed);
        long seed = this.rnd64();
        u = (RATIO.multiply(new BigDecimal(seed))).doubleValue();
        u = a + u * (b - a);
        return (int) u;
    }

    /*
     **  Function  : double negexp(double mean, long *seed)
     **  Return    : a value exponentially distributed with mean 'mean'.
     **  Remarks   : the value of '*seed' is changed.
     */
    public double negexp(double mean, long seed) {
        double u;
        //seed = this.rnd32(seed);
        seed = this.rnd64(seed);
        u = (RATIO.multiply(new BigDecimal(seed))).doubleValue();
        return (-mean * Math.log(u));
    }


    /*
     **  Function  : int poisson(double alpha,long *seed)
     **  Return    : the number of users arrived, according to a poisson process
     **              with rate 'alpha' user/slot, within a slot.
     **  Remarks   : the value of '*seed' is changed.
     */
    public int poisson(double alpha, long seed) {
        int n = 0;
        double pn, lim;
        double prob;

        lim = pn = Math.exp(-alpha);
        prob = uniform_0_1(seed);
        while (prob > lim) {
            n++;
            pn *= alpha / n;
            lim += pn;
        }
        return n;
    }


    /*
     **  Function  : int geometric0(double mean,long *seed)
     **  Return    : a random value distributed geometrically with average 'mean',
     **              starting from 0 (0 w.p. 1-p, 1 w.p. p(1-p), etc.).
     **  Remarks   : the value of '*seed' is changed.
     */
    public int geometric0(double mean, long seed) {
        int n;
        double prob, status;

        status = mean / (1.0 + mean);          /* E[X] = p/(1-p) -> p = E[X]/(1+E[X])  */
        prob = uniform_0_1(seed);          /* 1-p = prob. di avere n = 0           */
        n = (int) Math.floor(Math.log(1 - prob) / Math.log(status));
        return n;
    }


    /*
     **  Function: int geometric1(double mean,long *seed)
     **  Return  : a random value distributed geometrically with average 'mean',
     **            starting from 1 (1 w.p. 1-p, 2 w.p. p(1-p), etc.).
     **  Remarks : the value of '*seed' is changed.
     */
    public int geometric1(double mean, long seed) {
        int n;
        double prob, status;

        status = (mean - 1) / mean;            /* E[X] = 1/(1-p) -> p = (E[X]-1)/E[X]  */
        prob = uniform_0_1(seed);          /* 1-p = prob. di avere n = 1           */
        n = 1 + (int) Math.floor(Math.log(1 - prob) / Math.log(status));
        return n;
    }


    /*
     **  Function  : int geometric_trunc1(double mean,int max_len,long *seed)
     **  Return    : a random value distributed geometrically with average 'mean',
     **              starting from 1.
     **              The distribution is truncated at the value 'max_len'.
     **  Remarks   : the value of '*seed' is changed.
     */
    public int geometric_trunc1(double mean, int max_len, long seed) {
        /* These function returns a number distributed quasi-geometrically with   */
        /* average mean and maximum value 'max_len'.                              */
        /* There are some problems with the calculation. Here we explain the way  */
        /* the numbers are calculated.                                            */
        /* The mean value of the random variable is:                              */
        /*                                                                        */
        /*                     Sum(i*p^(i-1),i=1..N)                              */
        /*              E[x] = --------------------- = m                          */
        /*                      Sum(p^(i-1),i=1..N)                               */
        /* i.e.                                                                   */
        /*                     p^N ( Np - N - 1) + 1                              */
        /*                 m = ---------------------          (1)                 */
        /*                         (1-p)(1-p^N)                                   */
        /*                                                                        */
        /* where p is the transition probability in the Markov chain of the model.*/
        /*                                                                        */
        /* We need the value of p as a function of m and N. The only solution is  */
        /* to solve the equation (1) in the variable p using the Newton method,   */
        /* i.e.                                                                   */
        /*           p' = p - f(p)/f'(p)                                          */
        /* being p' the value of p at the step i+1, p the value at the step i,    */
        /* f(p) is (1) and f'(p) is df(p)/dp.                                     */
        /* In our calculations, we use:                                           */
        /*                                                                        */
        /*    f(p)  = p^N * ((m-N)p + N - m + 1) - mp + m -1                      */
        /*    f'(p) = (m-N) p^N + N p^(N-1)((m-N)p + N - m + 1) - m               */
        /*                                                                        */
        /* and the value  p = (m-1)/m  as starting point. This is the value of    */
        /* p when N tends to infinity.                                            */
        /*                                                                        */
        /* This value of p is used to find the number n to be returned.  A random */
        /* variable q uniformly distributed in (0,1) is extracted, so if          */
        /*                                                                        */
        /*               sum(p^(i-1),i=1..n)    1 - p^n                           */
        /*          q = --------------------- = -------                           */
        /*               sum(p^(i-1),i=1..N)    1 - p^N                           */
        /*                                                                        */
        /* we found that                                                          */
        /*                                                                        */
        /*              |~  log(p^N * q - q - 1) ~|                               */
        /*          n = |   --------------------  |                               */
        /*              |         log(p)          |                               */
        /*                                                                        */
        /* In order to avoid large computations, the previous values of 'mean'    */
        /* and 'max_len' are recorded, so if the function is called twice or more */
        /* times consecutively with the same parameters, the previously computed  */
        /* value of p can be used.                                                */
        /*                                                                        */
        /* In the code, there is the corrispondence:                              */
        /*            p     -> status                                             */
        /*            m     -> mean                                               */
        /*            N     -> max_len                                            */
        /*            q     -> prob                                               */
        /*            f(p)  -> f_p                                                */
        /*            f'(p) -> df_p                                               */
        /* between the symbols used in this comment and the variables names.      */
        int n;
        double prob, f_p, df_p;
        double temp_status, temp_res, len;
        double status = 0.0;
        double old_mean = 0.0;
        double status_N = 0.0;
        int old_max = 0;

        if (mean >= (double) max_len) {
            System.err.println("Error Calling Geometric_Trunc1()\n");
            return 1;
        }
        if (Math.abs(old_mean - mean) > 1e-5 || old_max != max_len) {
            len = (double) max_len;
            temp_status = (mean - 1) / mean;
            do {
                status = temp_status;
                status_N = Math.pow(status, len);
                temp_res = (mean - len) * status + len - mean + 1;
                f_p = status_N * temp_res - mean * status + mean - 1;
                df_p = (mean - len) * status_N + len * status_N * temp_res / status - mean;
                temp_status = status - f_p / df_p;
            } while (Math.abs(temp_status - status) > 1e-9);
            status = temp_status;
            status_N = Math.pow(status, len);
            old_mean = mean;
            old_max = max_len;
        }

        prob = uniform_0_1(seed);
        n = 1 + (int) Math.floor(Math.log(1 - prob + prob * status_N) / Math.log(status));
        return n;

    }

//    public long trunc_exp(double mean, long length) {
//        return this.trunc_exp(mean, length, this.nextLong());
//    }

    /*
     **  Function  : int trunc_exp(double mean,long length,long *seed)
     **  Return    : a value extracted from a truncated exponential density
     **              function.
     **  Remarks   : mean and length are expressed in bytes.
     **              The value of '*seed' is changed.
     */
    public long trunc_exp(double mean, long length, long seed) {
        double len, prob;
        //seed = rnd32(seed);
//        seed = rnd64(seed);
//        System.out.println("S "+seed);
        prob = (RATIO.multiply(new BigDecimal(seed))).doubleValue();
//        System.out.println("P "+prob);
        /* len =  - 8*mean*(log(*seed)-21.4875626); */
        len = -prob*mean * Math.log(prob);
//        System.out.println("L "+len);
        len = (len > length ) ? length : len/prob;
//        System.out.println("L2 "+len);
        return ((long) len == 0 ? 1 : (long)len);
    }

        public long trunc_exp(double mean, long length) {
        double len, prob;

        //seed = rnd32(seed);
//        System.out.println("S "+seed);
        prob = (RATIO.multiply(new BigDecimal(this.nextLong()))).doubleValue();
//        System.out.println("P "+prob);
        /* len =  - 8*mean*(log(*seed)-21.4875626); */
        len = -1*mean * Math.log(prob);
//        System.out.println("L "+len);
        len = (len > length ) ? length : len;
//        System.out.println("L2 "+len);
        return ((long) len == 0 ? 1 : (long)len);
    }

    /** Box muller transform
     * La trasformazione di Box-Muller è un metodo per generare coppie di numeri casuali
     * indipendenti e distribuiti gaussianamente con media nulla e varianza uno.
     * Media è CENTER e la varianze è STDEV
     */
    public double gaussian(double mu, double sigma) {
        double x1, x2, w, y1;
        double y2 = 0;
        int use_last = 0;

        if (use_last != 0) /* use value from previous call */ {
            y1 = y2;
            use_last = 0;
        } else {
            do {
                x1 = 2.0 * this.uniform_0_1(this.A + this.nextLong()) - 1.0;
                x2 = 2.0 * this.uniform_0_1(this.LASTXN + this.nextLong()) - 1.0;
                w = x1 * x1 + x2 * x2;
            } while (w >= 1.0);
            w = Math.sqrt((-2.0 * Math.log(w)) / w);
            y1 = x1 * w;
            y2 = x2 * w;
            use_last = 1;
        }

        return (mu + y1 * sigma);
    }

//    public double NextGaussian(long mu, double sigma) {
//
//        return mu + (this.CumulativeGaussian(this.uniform_0_1(this.nextLong())) * sigma < 10 ? 10 : this.CumulativeGaussian(this.uniform_0_1(this.nextLong())) * sigma);
//    }

    public double NextGaussian(double mu, double sigma ) {

        return mu + this.CumulativeGaussian(this.uniform_0_1(this.nextLong())) * sigma;

    }

    private static double CumulativeGaussian(double p) {

        // p is a rectangular probability between 0 and 1

        // convert that into a gaussian.

        // Apply the inverse cumulative gaussian distribution function

        // This is an approximation by Abramowitz and Stegun; Press, et al.

        // See http://www.pitt.edu/~wpilib/statfaq/gaussfaq.html

        // Because of the symmetry of the normal

        // distribution, we need only consider 0 < p < 0.5. If you have p > 0.5,

        // then apply the algorithm below to q = 1-p, and then negate the value

        // for X obtained.

        boolean fNegate = false;



        if (p > 0.5) {

            p = 1.0 - p;

            fNegate = true;

        }



        double t = Math.sqrt(Math.log(1.0 / (p * p)));

        double tt = t * t;

        double ttt = tt * t;

        double X = t - ((c_0 + c_1 * t + c_2 * tt) / (1 + d_1 * t + d_2 * tt + d_3 * ttt));



        return fNegate ? -X : X;

    }

    /***
     *
     *
     * //RICERND Random samples from the Rice/Rician probability distribution.
     * r = ricernd(v, s) returns random sample(s) from the Rice (aka Rician)
     * distribution with parameters v and s.
     * (either v or s may be arrays, if both are, they must match in size)
     *
     *   R ~ Rice(v, s) if R = sqrt(X^2 + Y^2), where X ~ N(v*cos(a), s^2) and
     *   Y ~ N(v*sin(a), s^2) are independent normal distributions (any real a).
     *   Note that v and s are *not* the mean and standard deviation of R!
     *
     *   The size of Y is the common size of the input arguments.  A scalar
     *   input functions as a constant matrix of the same size as the other
     *   inputs.
     *
     *   Note, to add Rician noise to data, with given s and data-dependent v:
     *     new = ricernd(old, s);
     *
     *   Reference: http://en.wikipedia.org/wiki/Rice_distribution (!)
     *
     *   Example:
     *
     *     // Compare histogram of random samples with theoretical PDF:
     *     v = 4; s = 3; N = 1000;
     *     r = ricernd(v*ones(1, N), s);
     *     c = linspace(0, ceil(max(r)), 20);
     *     w = c(2); // histogram bin-width
     *     h = histc(r, c); bar(c, h, 'histc'); hold on
     *     xl = xlim; x = linspace(xl(1), xl(2), 100);
     *     plot(x, N*w*ricepdf(x, v, s), 'r');
     *
     *   See also RICEPDF, RICESTAT, RICEFIT
     *   Missing (?) 'See also's RICECDF, RICEINV, RICELIKE
     *   Inspired by normpdf from the MATLAB statistics toolbox
     *   Copyright 2008 Ged Ridgway (Ged at cantab dot net)
     *
     */
    public int rice_rnd(int v, int s) {
        double x = s * this.nextInt(v) + v;
        double y = s * this.nextInt(v);
        double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        return (int) r;
    }
}


