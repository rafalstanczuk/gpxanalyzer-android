package com.itservices.gpxanalyzer.data.dsp;


import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
/**
 * A simple in-place iterative FFT implementation (Cooley–Tukey).
 * <p>
 * Note: This performs a forward FFT of real/imag data stored as two
 * separate arrays: x (real part) and y (imag part).
 * <p>
 * Requirements:
 *  - The input length n must be a power of two.
 */
public class FFT {
    private int n;  // total number of points
    private int m;  // number of stages = log2(n)

    // Lookup tables for cosine and sine (a.k.a. twiddle factors)
    // Only need to recompute when n changes.
    private double[] cos;
    private double[] sin;

    @Inject
    public FFT() {
    }

    /**
     * Initialize the FFT for a particular SignalSamplingProperties buffer size.
     * Ensures the buffer size is a power of two, and sets up twiddle factor tables.
     */
    public void init(SignalSamplingProperties signalSamplingProperties) {
        // Get the buffer size and store it
        this.n = signalSamplingProperties.getBufferSize();

        // Compute m = log2(n)
        this.m = (int) (Math.log(n) / Math.log(2));

        // Make sure n is indeed a power of two: (1 << m) == n
        if (n != (1 << m)) {
            throw new RuntimeException("FFT length must be a power of 2");
        }

        // Allocate arrays for twiddle factors
        cos = new double[n / 2];
        sin = new double[n / 2];

        // Precompute the twiddle factors for the forward transform
        // e^{-i * 2π * k / n} = cos(-2πk/n) + i sin(-2πk/n)
        //  which is equivalent to cos(2πk/n) - i sin(2πk/n).
        //  We store cos(...) in cos[k], sin(...) in sin[k].
        for (int i = 0; i < n / 2; i++) {
            cos[i] = Math.cos(-2 * Math.PI * i / n);
            sin[i] = Math.sin(-2 * Math.PI * i / n);
        }
    }

    /**
     * Performs an in-place forward FFT on the data in x (real part) and y (imag part).
     * After calling fft(x, y):
     *  - x[] and y[] will hold the FFT output (complex numbers) in-place.
     *  - The magnitude of the k-th frequency bin is sqrt(x[k]^2 + y[k]^2).
     *
     * @param x The real part (in-place)
     * @param y The imaginary part (in-place)
     */
    public void fft(double[] x, double[] y) {
        int i, j, k, n1, n2, a;
        double c, s, t1, t2;

        // ---------------------------------------------------------
        // 1) Bit-reversal: Reorder the input arrays in bit-reversed order
        // ---------------------------------------------------------
        j = 0;
        n2 = n / 2;
        for (i = 1; i < n - 1; i++) {
            n1 = n2;
            while (j >= n1) {
                j -= n1;
                n1 >>= 1;  // n1 = n1 / 2
            }
            j += n1;
            if (i < j) {
                // swap x[i] with x[j]
                t1 = x[i];
                x[i] = x[j];
                x[j] = t1;
                // swap y[i] with y[j]
                t1 = y[i];
                y[i] = y[j];
                y[j] = t1;
            }
        }

        // ---------------------------------------------------------
        // 2) The FFT itself (iterative Cooley–Tukey)
        // ---------------------------------------------------------
        n1 = 0;
        n2 = 1;

        for (i = 0; i < m; i++) {
            n1 = n2;       // current size of sub-FFTs = 2^i
            n2 <<= 1;      // next size of sub-FFTs = 2^(i+1)
            a = 0;         // twiddle factor index

            // For each sub-FFT "butterfly" group
            for (j = 0; j < n1; j++) {
                // Twiddle factors: cos[a], sin[a]
                c = cos[a];
                s = sin[a];

                // increment 'a' by how big a step we take for each j
                a += 1 << (m - i - 1);

                // Perform the butterfly
                for (k = j; k < n; k += n2) {
                    t1 = c * x[k + n1] - s * y[k + n1];
                    t2 = s * x[k + n1] + c * y[k + n1];

                    x[k + n1] = x[k] - t1;
                    y[k + n1] = y[k] - t2;
                    x[k]      = x[k] + t1;
                    y[k]      = y[k] + t2;
                }
            }
        }
    }
}
