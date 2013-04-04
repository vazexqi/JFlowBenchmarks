public class FilterBank {
    int N_sim;

    float[] y;

    int counter;

    public FilterBank(int snum, int cnum) {
        this.N_sim= snum;
        this.y= new float[N_sim];
        for (int i= 0; i < N_sim; i++) {
            y[i]= 0;
        }
        counter= cnum;
    }

    public boolean merge(float[] result) {
        float[] ty= this.y;
        int Nsim= this.N_sim;
        for (int i= 0; i < Nsim; i++) {
            ty[i]+= result[i];
        }
        this.counter--;

        return this.counter == 0;
    }

    public void print() {
        for (int i= 0; i < this.N_sim; i++) {
            System.out.println((int)(this.y[i] * 10000));
        }
    }
}

class FilterBankAtom {
    int ch_index;

    int N_ch;

    int N_col;

    int N_sim;

    int N_samp;

    public float[] vF;

    public FilterBankAtom(int cindex,
            int N_ch,
            int N_col,
            int N_sim,
            int N_samp) {
        this.ch_index= cindex;
        this.N_ch= N_ch;
        this.N_col= N_col;
        this.N_sim= N_sim;
        this.N_samp= N_samp;
        this.vF= new float[this.N_sim];
    }

    public void FBCore() {
        int i, j, k;
        int chindex= this.ch_index;
        int Ncol= this.N_col;
        int Nsim= this.N_sim;
        int Nch= this.N_ch;
        int Nsamp= this.N_samp;
        float[] tvF= this.vF;

        float r[]= new float[Nsim];
        for (i= 0; i < Nsim; i++) {
            r[i]= i + 1;
        }

        float[] H= new float[Ncol];
        float[] F= new float[Ncol];
        for (i= 0; i < Ncol; i++) {
            H[i]= i * Ncol + chindex * Nch + chindex + i + chindex + 1;
            F[i]= i * chindex + chindex * chindex + chindex + i;
        }
        float[] vH= new float[Nsim];
        float[] vDn= new float[(int)(Nsim / Nsamp)];
        float[] vUp= new float[Nsim];

        //convolving H
        for (j= 0; j < Nsim; j++) {
            k= 0;
            boolean stat= false;
            int diff= j;
            do {
                float tmp= H[k] * r[j - k];
                k++;
                diff--;
                stat= (k < Ncol) & (diff >= 0);
                vH[j]+= tmp;
            } while (stat);
        }

        //Down Samplin
        for (j= 0; j < Nsim / Nsamp; j++) {
            vDn[j]= vH[j * Nsamp];
        }

        //Up Sampling
        for (j= 0; j < Nsim / Nsamp; j++) {
            vUp[j * Nsamp]= vDn[j];
        }

        //convolving F
        for (j= 0; j < Nsim; j++) {
            k= 0;
            boolean stat= false;
            int diff= j;
            do {
                float tmp= F[k] * vUp[j - k];
                k++;
                diff--;
                stat= (k < Ncol) & (diff >= 0);
                tvF[j]+= tmp;
            } while (stat);
        }
    }
}
