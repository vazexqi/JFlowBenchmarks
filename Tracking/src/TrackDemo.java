import java.io.IOException;

public class TrackDemo {
    /* input data and the counter to record the input to be processed next */
    int[][] m_inputs;

    int m_count;

    /* current processing image related */
    float[] m_image; // Icur/Jpyr1

    int m_rows;

    int m_cols;

    float[] m_image_resized; // Jpyr2

    int m_rows_r;

    int m_cols_r;

    /* BP related */
    int m_num_bp;

    /* BPL related */
    int m_num_bpl;

    /* feature related */
    float[] m_features;

    int m_rows_f;

    int m_cols_f;

    /*  */
    float[][] m_3f;

    int m_rows_3f;

    int m_cols_3f;

    int m_counter_3f;

    int m_num_p;

    /* benchmark constants */
    public int WINSZ;

    public int N_FEA;

    int SUPPRESION_RADIUS;

    int LK_ITER;

    int m_counter;

    float accuracy;

    /* constructor */
    public TrackDemo(int nump) {
        this.m_inputs= new int[2][];

        int rows= 10 * 60; // * 2;
        int cols= 12 * 5;
        int offset= 0;
        this.m_inputs[0]= this.makeImage(rows, cols, offset);
        offset= 100;
        this.m_inputs[1]= this.makeImage(rows, cols, offset);
        this.m_count= 0;

        this.m_num_bp= 0;
        this.m_num_bpl= 0;

        this.WINSZ= 8;
        this.N_FEA= 1600; // 00;
        this.SUPPRESION_RADIUS= 10;
        this.LK_ITER= 20;
        this.accuracy= (float)0.03;
        this.m_counter= 2;
        // #ifdef test
        /*
         * this.WINSZ = 2; this.N_FEA = 10; this.LK_ITER = 1; this.m_counter = 2;
         * this.accuracy = (float)0.1; /* //#ifdef sim_fast this.WINSZ = 4;
         * this.N_FEA = 10; this.LK_ITER = 2; this.counter = 2;
         * 
         * //#ifdef sim this.WINSZ = 4; this.N_FEA = 20; this.LK_ITER = 4;
         * this.counter = 2;
         */
        this.m_3f= new float[3][this.N_FEA];
        this.m_rows_3f= this.N_FEA;
//    this.m_cols_3f = this.N_FEA;
        this.m_cols_3f= 1;
//    this.m_counter_3f = 3;
        this.m_num_p= nump;

        this.m_rows= 0;
        this.m_cols= 0;
        this.m_image= null;
        this.m_image_resized= null;
        this.m_rows_r= 0;
        this.m_cols_r= 0;

        this.m_features= null;
        this.m_rows_f= 0;
        this.m_cols_f= 0;
    }

    public int getNumP() {
        return this.m_num_p;
    }

    public boolean isFinish() {
        return (this.m_count == this.m_counter);
    }

    public int getRows() {
        return this.m_rows;
    }

    public int getCols() {
        return this.m_cols;
    }

    public float[] getImage() {
        return this.m_image;
    }

    public int getRowsR() {
        return this.m_rows_r;
    }

    public int getColsR() {
        return this.m_cols_r;
    }

    public float[] getImageR() {
        return this.m_image_resized;
    }

    public int[] getInput(boolean isadvance) {
        int[] input= this.m_inputs[this.m_count];
        if (isadvance) {
            this.m_count++;
        }

        return input;
    }

    public void setBPNum(int num) {
        this.m_num_bp= num;
    }

    public void setBPLNum(int num) {
        this.m_num_bpl= num;
    }

    public int[] makeImage(int rows, int cols, int offset) {
        int k, i, j;
        int[] out;

        out= new int[rows * cols + 4];
        out[0]= rows;
        out[1]= cols;
        out[2]= rows * cols;
        out[3]= 2;

        k= offset;
        for (i= 0; i < rows; i++) {
            for (j= 0; j < cols; j++) {
                out[i * cols + j + 4]= ((k++) * rows) % 255;
            }
        }

        return out;
    }

    public boolean addBP(BlurPiece bp) {
        int startRow= bp.getRowsRS();
        int endRow= bp.getRowsRE();
        int i, j, k, cols;
        float[] image, input;

        if (this.m_image == null) {
            this.m_rows= bp.getRows();
            this.m_cols= bp.getCols();
            this.m_image= new float[this.m_rows * this.m_cols];
        }
        image= this.m_image;
        cols= this.m_cols;

        input= bp.getResult();
        k= 0;
        for (i= startRow; i < endRow; i++) {
            for (j= 0; j < cols; j++) {
                image[i * cols + j]= input[k * cols + j];
            }
            k++;
        }

        this.m_num_bp--;
        return (0 == this.m_num_bp);
    }

    public boolean addBPL(BlurPieceL bpl) {
        int startRow= bpl.getRowsRS();
        int endRow= bpl.getRowsRE();
        int i, j, k, cols;
        float[] image, input;

        if (this.m_image == null) {
            this.m_rows= bpl.getRows();
            this.m_cols= bpl.getCols();
            this.m_image= new float[this.m_rows * this.m_cols];
        }
        image= this.m_image;
        cols= this.m_cols;

        input= bpl.getResult();
        k= 0;
        for (i= startRow; i < endRow; i++) {
            for (j= 0; j < cols; j++) {
                image[i * cols + j]= input[k * cols + j];
            }
            k++;
        }

        this.m_num_bpl--;
        return (0 == this.m_num_bpl);
    }

    public void postBlur() {
        int rows, cols;
        float temp;
        int[] kernel;
        int rows_k, cols_k;
        int k, i, j;
        int kernelSize, startCol, endCol, halfKernel, startRow, endRow, kernelSum;
        float[] image;

        rows= this.m_rows;
        cols= this.m_cols;
        image= this.m_image;

        kernel= new int[5];
        rows_k= 1;
        cols_k= 5;

        kernel[0]= 1;
        kernel[1]= 4;
        kernel[2]= 6;
        kernel[3]= 4;
        kernel[4]= 1;

        kernelSize= 5;
        kernelSum= 16;

        startCol= 2; // ((kernelSize)/2);
        endCol= cols - 2; // round(cols - (kernelSize/2));
        halfKernel= 2; // (kernelSize-1)/2;

        startRow= 2; // (kernelSize)/2;
        endRow= rows - 2; // (rows - (kernelSize)/2);

        for (i= startRow; i < endRow; i++) {
            for (j= startCol; j < endCol; j++) {
                temp= 0.0f;
                for (k= -halfKernel; k <= halfKernel; k++) {
                    temp+= image[(i + k) * cols + j] * kernel[k + halfKernel];
                }
                //System.out.println(temp*10000 + " " + i + " " + j); // TODO
                image[i * cols + j]= (float)(temp / kernelSum);
            }
        }
    }

    public float[] resize(float image[]) {
        int m, k, i, j;
        int kernel[];
        int rows_k, cols_k;
        float tempVal;
        int kernelSize, startCol, endCol, halfKernel, startRow, endRow, kernelSum;
        int outputRows, outputCols;
        float temp[];
        int rows_t;
        int cols_t;
        float[] resized;
        int rows= this.m_rows;
        int cols= this.m_cols;

        // level 1 is the base image.

        outputRows= (int)(Math.floor((rows + 1) / 2));
        outputCols= (int)(Math.floor((cols + 1) / 2));

        rows_t= rows;
        cols_t= outputCols;
        temp= new float[rows_t * cols_t];

        this.m_rows_r= outputRows;
        this.m_cols_r= outputCols;
        resized= new float[this.m_rows_r * this.m_cols_r];

        rows_k= 1;
        cols_k= 5;
        kernel= new int[rows_k * cols_k];

        kernel[0]= 1;
        kernel[1]= 4;
        kernel[2]= 6;
        kernel[3]= 4;
        kernel[4]= 1;

        kernelSize= 5;
        kernelSum= 16;

        startCol= 2; // (kernelSize/2);
        endCol= cols - 2; // (int)(cols - (kernelSize/2));
        halfKernel= 2; // (kernelSize-1)/2;

        startRow= 2; // kernelSize/2;
        endRow= rows - 2; // (rows - (kernelSize)/2);

        for (i= startRow; i < endRow; i++) {
            m= 0;
            for (j= startCol; j < endCol; j+= 2) {
                tempVal= 0;
                for (k= -halfKernel; k <= halfKernel; k++) {
                    tempVal+= (float)(image[i * cols + (j + k)] * (kernel[k + halfKernel]));
                }
                temp[i * outputCols + m]= (float)(tempVal / kernelSum);
                m= m + 1;
            }
        }

        m= 0;
        for (i= startRow; i < endRow; i+= 2) {
            for (j= 0; j < outputCols; j++) {
                tempVal= 0;
                for (k= -halfKernel; k <= halfKernel; k++) {
                    tempVal+= (float)(temp[(i + k) * outputCols + j] * (kernel[k + halfKernel]));
                }
                resized[m * outputCols + j]= (float)(tempVal / kernelSum);
            }
            m= m + 1;
        }
        return resized;
    }

    public void resize() {
        int m, k, i, j;
        int kernel[];
        int rows_k, cols_k;
        float tempVal;
        int kernelSize, startCol, endCol, halfKernel, startRow, endRow, kernelSum;
        int outputRows, outputCols;
        float temp[];
        int rows_t;
        int cols_t;
        float[] image, resized;
        int rows= this.m_rows;
        int cols= this.m_cols;

        // level 1 is the base image.

        outputRows= (int)(Math.floor((rows + 1) / 2));
        outputCols= (int)(Math.floor((cols + 1) / 2));

        rows_t= rows;
        cols_t= outputCols;
        temp= new float[rows_t * cols_t];

        this.m_rows_r= outputRows;
        this.m_cols_r= outputCols;
        resized= this.m_image_resized= new float[this.m_rows_r * this.m_cols_r];
        image= this.m_image;

        rows_k= 1;
        cols_k= 5;
        kernel= new int[rows_k * cols_k];

        kernel[0]= 1;
        kernel[1]= 4;
        kernel[2]= 6;
        kernel[3]= 4;
        kernel[4]= 1;

        kernelSize= 5;
        kernelSum= 16;

        startCol= 2; // (kernelSize/2);
        endCol= cols - 2; // (int)(cols - (kernelSize/2));
        halfKernel= 2; // (kernelSize-1)/2;

        startRow= 2; // kernelSize/2;
        endRow= rows - 2; // (rows - (kernelSize)/2);

        for (i= startRow; i < endRow; i++) {
            m= 0;
            for (j= startCol; j < endCol; j+= 2) {
                tempVal= 0;
                for (k= -halfKernel; k <= halfKernel; k++) {
                    tempVal+= (float)(image[i * cols + (j + k)] * (kernel[k + halfKernel]));
                }
                temp[i * outputCols + m]= (float)(tempVal / kernelSum);
                m= m + 1;
            }
        }

        m= 0;
        for (i= startRow; i < endRow; i+= 2) {
            for (j= 0; j < outputCols; j++) {
                tempVal= 0;
                for (k= -halfKernel; k <= halfKernel; k++) {
                    tempVal+= (float)(temp[(i + k) * outputCols + j] * (kernel[k + halfKernel]));
                }
                resized[m * outputCols + j]= (float)(tempVal / kernelSum);
            }
            m= m + 1;
        }
    }

    public boolean addIDX(IDX idx) {
        float[][] m3f= this.m_3f;
        int rows= idx.getRowsRS();
        int rowe= idx.getRowsRE();
        int threshold= this.N_FEA;
        int[] ind= idx.getInd();
        float[] image= idx.getImage();
        int r= idx.getR();
        int nfea= this.N_FEA;
        int length= this.m_rows * this.m_cols;
        int[] h_ind= new int[this.N_FEA];
        boolean[] f_ind= new boolean[this.N_FEA];
        for (int i= 0; i < this.N_FEA; i++) {
            f_ind[i]= false;
        }

        int j= 0;
        int localindex= 0;
        int rindex= 0;
        for (int i= rows; i < rowe; i++) {
            rindex= length - ind[j];
            if (rindex < nfea) {
                localindex= j + rows;
                if (!f_ind[rindex]) {
                    // empty
                    m3f[2][rindex]= image[localindex];
                    h_ind[rindex]= localindex;
                    localindex++;
                    m3f[0][rindex]= (float)Math.ceil((float)(localindex / (float)r));
                    m3f[1][rindex]= (float)(localindex - (m3f[0][rindex] - 1) * r * 1.0);
                    f_ind[rindex]= true;
                } else {
                    // previously held by some others with the same value
                    int k= rindex; // the place to insert
                    int k1= rindex; // the first one which is not set
                    for (; k1 < nfea; k1++) {
                        if (h_ind[k1] > localindex) {
                            k= k1;
                        }
                        if (!f_ind[k1]) {
                            break;
                        }
                    }
                    if (k == nfea) {
                        // System.printI(77777777);
                        return false;
                    } else if (k == rindex) {
                        k= k1;
                    }
                    if (f_ind[k] && (m3f[2][k] != image[localindex])) {
                        // System.printI(88888888);
                        return false;
                    }
                    // move all things after k behind
                    int p= k1;
                    for (; p > k; p--) {
                        m3f[2][p]= m3f[2][p - 1];
                        h_ind[p]= h_ind[p - 1];
                        m3f[0][p]= m3f[0][p - 1];
                        m3f[1][p]= m3f[1][p - 1];
                        f_ind[p]= true;
                    }
                    // insert
                    m3f[2][p]= image[localindex];
                    h_ind[p]= localindex;
                    localindex++;
                    m3f[0][p]= (float)Math.ceil((float)(localindex / (float)r));
                    m3f[1][p]= (float)(localindex - (m3f[0][p] - 1) * r * 1.0);
                    f_ind[p]= true;
                }
            }
            j++;
        }

        this.m_num_p--;
        //print3f();

        return (0 == this.m_num_p);
    }

    public void calcFeatures() {
        float[] f1, f2, f3;
        int rows_f1, cols_f1, rows_f2, cols_f2, rows_f3, cols_f3;
        float[] interestPnts;
        int[] rows_ip, cols_ip;
        int rows_ipt, cols_ipt;
        rows_ip= new int[1];
        cols_ip= new int[1];

        f1= this.m_3f[0];
        f2= this.m_3f[1];
        f3= this.m_3f[2];
        rows_f1= this.m_rows_3f;
        rows_f2= this.m_rows_3f;
        rows_f3= this.m_rows_3f;
        cols_f1= this.m_cols_3f;
        cols_f2= this.m_cols_3f;
        cols_f3= this.m_cols_3f;

        interestPnts=
                this.getANMs(f1, rows_f1, cols_f1, f2, rows_f2, cols_f2, f3, rows_f3, cols_f3, rows_ip,
                        cols_ip);
        rows_ipt= rows_ip[0];
        cols_ipt= cols_ip[0];
        rows_ip= cols_ip= null;

        // TODO
//    for(int i = 0; i < interestPnts.length; i++) {
//      if(interestPnts[i] > 0.0) {
//        System.out.println("index: " + i + " " + interestPnts[i]);
//      }
//    }

        // fTranspose(interestPnts)
        float[] trans;
        int i, j, k, rows_trans, cols_trans;

        rows_trans= cols_ipt;
        cols_trans= rows_ipt;
        trans= new float[rows_trans * cols_trans];

        k= 0;
        for (i= 0; i < cols_ipt; i++) {
            for (j= 0; j < rows_ipt; j++) {
                trans[k++]= interestPnts[j * cols_ipt + i];
            }
        }

        // fDeepCopyRange(interestPnt, 0, 2, 0, cols_ip[0])
        int rows, cols;
        int numberRows= 2;
        int startRow= 0;
        int numberCols= cols_trans;
        int startCol= 0;

        rows= numberRows + startRow;
        cols= numberCols + startCol;

        rows_ipt= numberRows;
        cols_ipt= numberCols;
        interestPnts= new float[rows_ipt * cols_ipt];

        k= 0;
        for (i= startRow; i < rows; i++) {
            for (j= startCol; j < cols; j++) {
                interestPnts[k++]= trans[i * cols_trans + j];
            }
        }

        float[] features;
        this.m_rows_f= 2;
        this.m_cols_f= cols_ipt;

        features= this.m_features= new float[this.m_rows_f * this.m_cols_f];
        for (i= 0; i < 2; i++) {
            for (j= 0; j < cols_ipt; j++) {
                features[i * cols_ipt + j]= interestPnts[i * cols_ipt + j];
            }
        }
    }

    public float[] horzcat(float[] f1, int rows_f1, int cols_f1, float[] f2, int rows_f2,
            int cols_f2, float[] f3, int rows_f3, int cols_f3) {
        float[] out;
        int rows= 0, cols= 0, i, j, k, c_1, c_2, r_3, c_3;

        c_1= cols_f1;
        cols+= c_1;

        c_2= cols_f2;
        cols+= c_2;

        r_3= rows_f3;
        c_3= cols_f3;
        cols+= c_3;

        rows= r_3;

        out= new float[rows * cols];

        for (i= 0; i < rows; i++) {
            k= 0;
            for (j= 0; j < c_1; j++) {
                out[i * cols + k]= f1[i * c_1 + j];
                k++;
            }
            for (j= 0; j < c_2; j++) {
                out[i * cols + k]= f2[i * c_2 + j];
                k++;
            }
            for (j= 0; j < c_3; j++) {
                out[i * cols + k]= f3[i * c_3 + j];
                k++;
            }
        }

        return out;
    }

    public int[] fSortIndices(float[] input, int rows, int cols) {
        float[] in;
        int i, j, k;
        int[] ind;

        // fDeepCopy
        in= new float[input.length];
        for (i= 0; i < input.length; i++) {
            in[i]= input[i];
        }

        ind= new int[rows * cols];

        for (k= 0; k < cols; k++) {
            for (i= 0; i < rows; i++) {
                float localMax= in[i * cols + k];
                int localIndex= i;
                ind[i * cols + k]= i;
                for (j= 0; j < rows; j++) {
                    if (localMax < in[j * cols + k]) {
                        ind[i * cols + k]= j;
                        localMax= in[j * cols + k];
                        localIndex= j;
                    }
                }
                in[localIndex * cols + k]= 0;
            }
        }

        return ind;
    }

    public float[] ffVertcat(float[] matrix1, int rows_m1, int cols_m1, float[] matrix2, int rows_m2,
            int cols_m2) {
        float[] outMatrix;
        int rows_o, cols_o, i, j, k;

        rows_o= rows_m1 + rows_m2;
        cols_o= cols_m1;
        outMatrix= new float[rows_o * cols_o];

        for (i= 0; i < cols_m1; i++) {
            for (j= 0; j < rows_m1; j++) {
                outMatrix[j * cols_m1 + i]= matrix1[j * cols_m1 + i];
            }
            for (k= 0; k < rows_m2; k++) {
                outMatrix[(k + rows_m1) * cols_m1 + i]= matrix2[k * cols_m2 + i];
            }
        }

        return outMatrix;

    }

    public float[] getANMs(float[] f1, int rows_f1, int cols_f1, float[] f2, int rows_f2,
            int cols_f2, float[] f3, int rows_f3, int cols_f3, int[] rows_ip, int[] cols_ip) {
        float MAX_LIMIT= (float)100000000;
        float C_ROBUST= (float)1.0;
        float[] suppressR, points, srtdPnts, tempF, srtdV, interestPnts= null, temp;
        int rows_sr, cols_sr, rows_p, cols_p, rows_sp, cols_sp, rows_tf, cols_tf;
        int rows_sv, cols_sv, rows_tmp, cols_tmp;
        int[] srtdVIdx, supId;
        int rows_svi, cols_svi, rows_si, cols_si;
        float t, t1, r_sq;
        int n, i, j, k, validCount, cnt, end, iter, rows, cols;
        int supIdPtr= 0;

        // TODO
//    for(i = 0; i < rows_f1; i++) {
//      System.out.println("++ " + f1[i] + " " + f2[i] + " " + f3[i]);
//    }

        r_sq= (float)(this.SUPPRESION_RADIUS ^ 2);
        points= this.horzcat(f1, rows_f1, cols_f1, f2, rows_f2, cols_f2, f3, rows_f3, cols_f3);
        rows_p= rows_f3;
        cols_p= cols_f1 + cols_f2 + cols_f3;
        n= rows_f3;

        // TODO
//    for(i = 0; i < points.length; i++) {
//      System.out.println(points[i]);
//    }

        /** sort() arg 2 is for descend = 1, arg3 = indices. Returns sorted values **/

        srtdVIdx= this.fSortIndices(f3, rows_f3, cols_f3);
        rows_svi= rows_f3;
        cols_svi= cols_f3;

        rows_sp= rows_svi;
        cols_sp= cols_p;
        srtdPnts= new float[rows_sp * cols_sp];

        for (i= 0; i < rows_sp; i++) {
            for (j= 0; j < cols_sp; j++) {
                srtdPnts[i * cols_sp + j]= points[srtdVIdx[i] * cols_sp + j];
            }
        }

        rows_tmp= 1;
        cols_tmp= 3;
        temp= new float[rows_tmp * cols_tmp];
        rows_sr= n;
        cols_sr= 1;
        suppressR= new float[rows_sr * cols_sr];
        for (i= 0; i < rows_sr; i++) {
            for (j= 0; j < cols_sr; j++) {
                suppressR[i * cols_sr + j]= MAX_LIMIT;
            }
        }

        validCount= 0;
        iter= 0;
        for (i= 0; i < rows_sr; i++) {
            if (suppressR[i] > r_sq) {
                validCount++;
            }
        }

        k= 0;
        rows_si= validCount;
        cols_si= 1;
        supId= new int[rows_si * cols_si];
        for (i= 0; i < (rows_sr * cols_sr); i++) {
            if (suppressR[i] > r_sq) {
                supId[k++]= i;
            }
        }

        while (validCount > 0) {
            float[] tempp, temps;
            int rows_tpp, cols_tpp, rows_tps, cols_tps;
            temp[0]= srtdPnts[supId[0] * cols_sp + 0];
            temp[1]= srtdPnts[supId[0] * cols_sp + 1];
            temp[2]= srtdPnts[supId[0] * cols_sp + 2];

            if (iter == 0) {
                interestPnts= temp;
                rows_ip[0]= rows_tmp;
                cols_ip[0]= cols_tmp;
            } else {
                interestPnts= this.ffVertcat(interestPnts, rows_ip[0], cols_ip[0], temp, rows_tmp, cols_tmp);
                rows_ip[0]= rows_ip[0] + rows_tmp;
                cols_ip[0]= cols_ip[0];
            }


            iter++;

            // fDeepCopy
            rows_tpp= rows_sp;
            cols_tpp= cols_sp;
            tempp= new float[rows_tpp * cols_tpp];
            for (i= 0; i < rows_tpp * cols_tpp; i++) {
                tempp[i]= srtdPnts[i];
            }
            // fDeepCopy
            rows_tps= rows_sr;
            cols_tps= cols_sr;
            temps= new float[rows_tps * cols_tps];
            for (i= 0; i < rows_tps * cols_tps; i++) {
                temps[i]= suppressR[i];
            }

            rows_sp= validCount - 1;
            cols_sp= 3;
            srtdPnts= new float[rows_sp * cols_sp];
            rows_sr= validCount - 1;
            cols_sr= 1;
            suppressR= new float[rows_sr * cols_sr];

            k= 0;
            for (i= 0; i < (validCount - 1); i++) {
                srtdPnts[i * cols_sp + 0]= tempp[supId[i + 1] * cols_sp + 0];
                srtdPnts[i * cols_sp + 1]= tempp[supId[i + 1] * cols_sp + 1];
                srtdPnts[i * cols_sp + 2]= tempp[supId[i + 1] * cols_sp + 2];
                suppressR[i * cols_sr + 0]= temps[supId[i + 1] * cols_sr + 0];
            }

            int rows1= rows_ip[0] - 1;
            int cols1= cols_ip[0];
            for (i= 0; i < rows_sp; i++) {
                t= (float)0;
                t1= (float)0;
                if ((C_ROBUST * interestPnts[rows1 * cols1 + 2]) >= srtdPnts[i * cols_sp + 2]) {
                    t= srtdPnts[i * cols_sp + 0] - interestPnts[rows1 * cols1 + 0];
                    t1= srtdPnts[i * cols_sp + 1] - interestPnts[rows1 * cols1 + 1];
                    t= t * t + t1 * t1;
                    t1= (float)0;
                }

                if ((C_ROBUST * interestPnts[rows1 * cols1 + 2]) < srtdPnts[i * cols_sp + 2]) {
                    t1= /* (float) 1 **/(float)MAX_LIMIT;
                }

                if (suppressR[i] > (t + t1)) {
                    suppressR[i]= t + t1;
                }
            }

            validCount= 0;
            for (i= 0; i < rows_sr; i++) {
                if (suppressR[i] > r_sq) {
                    validCount++;
                }
            }

            k= 0;
            rows_si= validCount;
            cols_si= 1;
            supId= new int[rows_si * cols_si];

            for (i= 0; i < rows_sr * cols_sr; i++) {
                if (suppressR[i] > r_sq) {
                    supId[k++]= i;
                }
            }
        }

        return interestPnts;
    }

    public void startTrackingLoop() {
        this.m_image= null;
        this.m_image_resized= null;
    }

    public float[] getInterpolatePatch(float[] src, int rows, int cols, float centerX, float centerY) {
        float[] dst;
        int rows_d, cols_d;
        float a, b, a11, a12, a21, a22;
        int i, j, srcIdxX, dstIdxX, srcIdy, dstIdy, dstIndex;

        a= (float)(centerX - Math.floor(centerX));
        b= (float)(centerY - Math.floor(centerY));

        a11= (1 - a) * (1 - b);
        a12= a * (1 - b);
        a21= (1 - a) * b;
        a22= a * b;

        rows_d= 1;
        cols_d= 2 * this.WINSZ * 2 * this.WINSZ;
        dst= new float[rows_d * cols_d];

        for (i= -this.WINSZ; i <= (this.WINSZ - 1); i++) {
            srcIdxX= (int)(Math.floor(centerX)) + i;
            dstIdxX= i + this.WINSZ;

            for (j= -this.WINSZ; j <= (this.WINSZ - 1); j++) {
                srcIdy= (int)(Math.floor(centerY)) + j;
                dstIdy= j + this.WINSZ;
                dstIndex= dstIdy * 2 * this.WINSZ + dstIdxX;
                // printf("%f\t%f\t%d\t%d\n", centerX, centerY, srcIdxX, srcIdy);
                dst[dstIndex]=
                        src[srcIdy * cols + srcIdxX] * a11 + src[(srcIdy + 1) * cols + srcIdxX] * a12
                                + src[srcIdy * cols + (srcIdxX + 1)] * a21
                                + src[(srcIdy + 1) * cols + (srcIdxX + 1)] * a22;
            }
        }

        return dst;
    }

    public int[] calcPyrLKTrack(float[][] Ipyrs, int[] rows, int[] cols, float[] newPnt) {
        float[] ip1, ip2, idxp1, idxp2, idyp1, idyp2, jp1, jp2, fPnt;
        int k= 0;

        ip1= Ipyrs[k++];
        ip2= Ipyrs[k++];
        idxp1= Ipyrs[k++];
        idyp1= Ipyrs[k++];
        idxp2= Ipyrs[k++];
        idyp2= Ipyrs[k++];
        jp1= this.m_image;
        jp2= this.m_image_resized;
        fPnt= this.m_features;

        int idx, level, pLevel, i, winSizeSq;
        int[] valid, imgDims;
        int rows_v, cols_v, rows_id, cols_id;
        float[] rate, iPatch, jPatch, iDxPatch, iDyPatch;
        int rows_r, cols_r, rows_ip, cols_ip, rows_jp, cols_jp;
        int rows_idxp, cols_idxp, rows_idyp, cols_idyp;
        float x, y, dX, dY, c_xx, c_yy, c_xy, tr;
        int imgSize_1, /* max_iter, */imgSize_2;
        float mX, mY, dIt, eX, eY, c_det;
        int nFeatures= this.m_cols_f;

        rows_id= 4;
        cols_id= 1;
        imgDims= new int[rows_id * cols_id];

        imgDims[0]= rows[0];
        imgDims[1]= cols[0];
        imgDims[2]= rows[1];
        imgDims[3]= cols[1];

        pLevel= 2;
        rows_r= 1;
        cols_r= 6;
        rate= new float[rows_r * cols_r];

        rate[0]= (float)1;
        rate[1]= (float)0.5;
        rate[2]= (float)0.25;
        rate[3]= (float)0.125;
        rate[4]= (float)0.0625;
        rate[5]= (float)0.03125;

        winSizeSq= 4 * this.WINSZ * this.WINSZ;
        rows_ip= 1;
        cols_ip= winSizeSq;
        iPatch= new float[rows_ip * cols_ip];
        rows_jp= 1;
        cols_jp= winSizeSq;
        jPatch= new float[rows_jp * cols_jp];
        rows_idxp= 1;
        cols_idxp= winSizeSq;
        iDxPatch= new float[rows_idxp * cols_idxp];
        rows_idyp= 1;
        cols_idyp= winSizeSq;
        iDyPatch= new float[rows_idyp * cols_idyp];

        rows_v= 1;
        cols_v= nFeatures;
        valid= new int[rows_v * cols_v];
        for (int valid_idx= 0; valid_idx < valid.length; valid_idx++) {
            valid[valid_idx]= 1;
        }

        for (i= 0; i < nFeatures; i++) {
            dX= (float)0;
            dY= (float)0;
            x= fPnt[i * 2 + 0] * rate[pLevel];
            y= fPnt[i * 2 + 1] * rate[pLevel];
            c_det= (float)0;

            for (level= pLevel - 1; level >= 0; level--) {
                x= x + x;
                y= y + y;
                dX= dX + dX;
                dY= dY + dY;
                imgSize_1= imgDims[level * 2];
                imgSize_2= imgDims[level * 2 + 1];

                c_xx= (float)0;
                c_xy= (float)0;
                c_yy= (float)0;

                if ((x - (float)this.WINSZ) < (float)0 || (y - (float)this.WINSZ) < (float)0
                        || (y + (float)this.WINSZ) >= (float)imgSize_1
                        || (x + (float)this.WINSZ) >= (float)imgSize_2) {
                    valid[i]= 0;
                    break;
                }

                if (level == 0) {
                    iPatch= getInterpolatePatch(ip1, rows[0], cols[0], x, y);
                    iDxPatch= getInterpolatePatch(idxp1, rows[2], cols[2], x, y);
                    iDyPatch= getInterpolatePatch(idyp1, rows[3], cols[3], x, y);
                }
                if (level == 1) {
                    iPatch= getInterpolatePatch(ip2, rows[1], cols[1], x, y);
                    iDxPatch= getInterpolatePatch(idxp2, rows[4], cols[4], x, y);
                    iDyPatch= getInterpolatePatch(idyp2, rows[5], cols[5], x, y);
                }
                rows_ip= rows_idxp= rows_idyp= 1;
                cols_ip= cols_idxp= cols_idyp= 2 * this.WINSZ * 2 * this.WINSZ;

                for (idx= 0; idx < this.WINSZ; idx++) {
                    c_xx+= iDxPatch[idx] * iDxPatch[idx];
                    c_xy+= iDxPatch[idx] * iDyPatch[idx];
                    c_yy+= iDyPatch[idx] * iDyPatch[idx];
                }

                c_det= (c_xx * c_yy - c_xy * c_xy);
                tr= c_xx + c_yy;

                if (c_det == (float)0) {
                    break;
                }

                //System.out.println((float) (c_det / (tr + (float) 0.00001)) );
                if ((float)(c_det / (tr + 0.00001)) < (float)this.accuracy) {
                    valid[i]= 0;
                    break;
                }

                c_det= (float)(1 / c_det);
                for (k= 0; k < this.LK_ITER; /* max_iter; */k++) {
                    if ((x + dX - (float)this.WINSZ) < (float)0
                            || (y + dY - (float)this.WINSZ) < (float)0
                            || (y + dY + (float)this.WINSZ) >= (float)imgSize_1
                            || (x + dX + (float)this.WINSZ) >= (float)imgSize_2) {
                        valid[i]= 0;
                        break;
                    }

                    // printf("x and dx = %d\t%d\t%f\t%f\t%f\t%f\n", i, level, x, dX, y,
                    // dY);
                    if (level == 0) {
                        jPatch= getInterpolatePatch(jp1, this.m_rows, this.m_cols, x + dX, y + dY);
                    }
                    if (level == 1) {
                        jPatch= getInterpolatePatch(jp2, this.m_rows_r, this.m_cols_r, x + dX, y + dY);
                    }
                    rows_jp= 1;
                    cols_jp= 2 * this.WINSZ * 2 * this.WINSZ;

                    eX= 0;
                    eY= 0;
                    for (idx= 0; idx < winSizeSq; idx++) {
                        dIt= iPatch[idx] - jPatch[idx];
                        eX+= dIt * iDxPatch[idx];
                        eY+= dIt * iDyPatch[idx];
                    }

                    mX= c_det * (eX * c_yy - eY * c_xy);
                    mY= c_det * (-eX * c_xy + eY * c_xx);
//        printf("mx = %d\t%d\t%f\t%f\t%f\t%f\t%f\n", i, level, mX, mY, c_det, eX, eY);
//          System.out.println(i+" "+level+" "+mX+" "+mY+" "+c_det+" "+eX+" "+eY);
                    dX= dX + mX;
                    dY= dY + mY;

                    if ((mX * mX + mY + mY) < this.accuracy) {
                        break;
                    }
                }
            }

            newPnt[i]= fPnt[i * 2] + dX;
            newPnt[1 * nFeatures + i]= fPnt[i * 2 + 1] + dY;

        }

        return valid;
    }

    public void calcTrack(float[] prevImage, float[] prevImageR, IXLM ixlm, IYLM iylm, IXLMR ixlmr, IYLMR iylmr) {
        float[][] Ipyrs= new float[6][];
        int[] rows= new int[6];
        int[] cols= new int[6];
        float[] newpoints, features, np_temp;
        int rows_n, cols_n, rows_np, cols_np, i, j, k, m, n, numFind;
        int[] status;
        int rows_s, cols_s;

//    Ipyrs[0] = ixlm.getImage();
        Ipyrs[0]= prevImage;
        rows[0]= ixlm.getRows();
        cols[0]= ixlm.getCols();
        Ipyrs[2]= ixlm.getResult();
        rows[2]= ixlm.getRowsR();
        cols[2]= ixlm.getColsR();
        Ipyrs[3]= iylm.getResult();
        rows[3]= iylm.getRowsR();
        cols[3]= iylm.getColsR();

//    Ipyrs[1] = ixlmr.getImage();
        Ipyrs[1]= prevImageR;
        rows[1]= ixlmr.getRows();
        cols[1]= ixlmr.getCols();
        Ipyrs[4]= ixlmr.getResult();
        rows[4]= ixlmr.getRowsR();
        cols[4]= ixlmr.getColsR();
        Ipyrs[5]= iylmr.getResult();
        rows[5]= iylmr.getRowsR();
        cols[5]= iylmr.getColsR();

        features= this.m_features;
        rows_n= 2;
        cols_n= this.m_cols_f;
        newpoints= new float[rows_n * cols_n];

        // status_ = calcPyrLKTrack(...)
        status= this.calcPyrLKTrack(Ipyrs, rows, cols, newpoints);
        rows_s= 1;
        cols_s= this.m_cols_f;

//    //TODO
//    System.out.println("###########");
//    for(i=0;i<status.length;i++){
//      System.out.println(status[i]);
//    }

        // fDeepCopy
        np_temp= new float[newpoints.length];
        rows_np= rows_n;
        cols_np= cols_n;
        for (i= 0; i < newpoints.length; i++) {
            np_temp[i]= newpoints[i];
        }
        if (rows_s * cols_s > 0) {
            int[] findInd;
            int rows_f, cols_f;
            rows_f= rows_s * cols_s;
            cols_f= 1;
            findInd= new int[rows_f * cols_f];

            k= 0;
            m= 0;
            numFind= 0;
            for (i= 0; i < cols_s; i++) {
                for (j= 0; j < rows_s; j++) {
                    if (status[j * cols_s + i] != 0) {
                        findInd[k]= m;
                        numFind++;
                    } else {
                        findInd[k]= 0;
                    }

                    m++;
                    k++;
                }
            }

            rows_n= rows_np;
            cols_n= numFind;
            newpoints= new float[rows_n * cols_n];

            k= 0;
            n= 0;
            for (i= 0; i < rows_np; i++) {
                for (j= 0; j < cols_np; j++) {
                    m= findInd[j];
                    if (m > 0) {
                        newpoints[k++]= np_temp[i * cols_np + m];
                    }
                }
            }
        }

        // features_ = fDeepCopy(newpoints_);
        this.m_rows_f= rows_n;
        this.m_cols_f= cols_n;
        features= this.m_features= new float[newpoints.length];
        for (k= 0; k < newpoints.length; k++) {
            features[k]= newpoints[k];
        }
    }

    public void printImage() {
        // result validation
        for (int i= 0; i < this.m_rows; i++) {
            for (int j= 0; j < this.m_cols; j++) {
                System.out.println((int)(this.m_image[i * this.m_cols + j] * 10));
            }
        }
    }

    public void print3f() {
        // result validation
        for (int j= 0; j < this.N_FEA; j++) {
            System.out.println("-- " + this.m_3f[0][j] + ",  " + this.m_3f[1][j] + ",  " + this.m_3f[2][j]);
        }
    }

    public void printFeatures() {
        // result validation
        System.out.println("this.m_rows_f=" + this.m_rows_f);
        System.out.println("this.m_cols_=" + this.m_cols_f);
        System.out.println("m_features.length=" + this.m_features.length);
        for (int i= 0; i < this.m_rows_f; i++) {
            for (int j= 0; j < this.m_cols_f; j++) {
                System.out.println(this.m_features[i * this.m_cols_f + j]);
            }
        }
    }

    public void run() throws IOException {
        long start= System.currentTimeMillis();
        ImageReader imageReader= new ImageReader();

        int[] input= imageReader.readImage("1.bmp");

        // TASK: blur & mergeBP
        int pnum= 32; // 60;
        setBPNum(pnum);
        int range= (input[0]) / pnum;
        for (int i= 0; i < pnum; i++) {
            BlurPiece bp= new BlurPiece(i, range, input, pnum);
            bp.blur();
            addBP(bp);
        }
        postBlur();

        float[] Icur= getImage();

        pnum= 16; // 30;
        range= getRows() / pnum;
        int rows= getRows();
        int cols= getCols();

        // create ImageX to calc Sobel_dX
        ImageXM imageXM= new ImageXM(pnum, rows, cols);
        for (int i= 0; i < pnum; i++) {
            ImageX imageX= new ImageX(i, range, Icur, rows, cols, pnum);
            imageX.calcSobel_dX();
            imageXM.addCalcSobelResult(imageX);
        }
        imageXM.calcSobel_dX();

        // create ImageY to calc Sobel_dY
        ImageYM imageYM= new ImageYM(pnum, rows, cols);
        for (int i= 0; i < pnum; i++) {
            ImageY imageY= new ImageY(i, range, Icur, rows, cols, pnum);
            imageY.calcSobel_dY();
            imageYM.addCalcSobelResult(imageY);
        }
        imageYM.calcSobel_dY();


        // create a Lambda to aggregate results from the ImageXs
        Lambda lda= new Lambda(WINSZ, N_FEA, pnum, getNumP());
        lda.calcGoodFeature(imageXM, imageYM);
        // validation
        //lda.printImage();
        lda.reshape();
        // validation
        // lda.printImage();

        // TASK: calculates indicies
        int r= lda.getR();
        float[] data= lda.getImage();
        int c_rows= lda.getRows();
        int c_cols= lda.getCols();
        int c_pnum= lda.getNumP();
        int c_range= c_rows / c_pnum;

        // TASK: processIDX
        IDX IDXarray[]= new IDX[c_pnum];
        long startCritical= System.currentTimeMillis();

        for (int i= 0; i < c_pnum; i++) {
//      sese parallel_IDX{
            IDX idx= new IDX(lda.N_FEA, i, c_range, data, c_rows, c_cols, r, c_pnum);
            idx.fSortIndices();
//      }
//      sese serial_IDX{
            IDXarray[i]= idx;
//      }
        }
        long stopCritical= System.currentTimeMillis();

        resize();

        // TASK: merge IDX 
        for (int i= 0; i < c_pnum; i++) {
            addIDX(IDXarray[i]);
        }

        // TASK: calcFeatures
        calcFeatures();

        // TASK: startTrackingLoop
        for (int count= 1; count <= m_counter; count++) {

            int prevSize= getRows() * getCols();
            float[] prevImage= new float[prevSize];
            System.arraycopy(getImage(), 0, prevImage, 0, prevSize);

            prevSize= getRowsR() * getColsR();
            float[] prevImageR= new float[prevSize];
            System.arraycopy(getImageR(), 0, prevImageR, 0, prevSize);

            //TASK: processIXL, mergeIXL , processIYL, mergeIYL
            int pnum1= 8; // 15; // * 2;
            data= getImage();
            rows= getRows();
            cols= getCols();
            range= rows / pnum1;

            IXLM ixlm= new IXLM(pnum1, data, rows, cols);
            IYLM iylm= new IYLM(pnum1, data, rows, cols);
            for (int i= 0; i < pnum1; i++) {
                IXL ixl= new IXL(i, range, data, rows, cols, pnum1);
                ixl.calcSobel_dX();
                ixlm.addCalcSobelResult(ixl);
                IYL iyl= new IYL(i, range, data, rows, cols, pnum1);
                iyl.calcSobel_dY();
                iylm.addCalcSobelResult(iyl);
            }

            ixlm.calcSobel_dX();
            iylm.calcSobel_dY();

            //TASK: processIXLR, mergeIXLR , processIYLR, mergeIYLR
            data= getImageR();
            rows= getRowsR();
            cols= getColsR();
            range= rows / pnum1;
            IXLMR ixlmr= new IXLMR(pnum1, data, rows, cols);
            IYLMR iylmr= new IYLMR(pnum1, data, rows, cols);

            for (int i= 0; i < pnum1; i++) {
                IXLR ixl= new IXLR(i, range, data, rows, cols, pnum1);
                ixl.calcSobel_dX();
                ixlmr.addCalcSobelResult(ixl);
                IYLR imy= new IYLR(i, range, data, rows, cols, pnum1);
                imy.calcSobel_dY();
                iylmr.addCalcSobelResult(imy);
            }
            ixlmr.calcSobel_dX();
            iylmr.calcSobel_dY();

            int pnum2= 32; // 60; // * 2;
            System.out.println("read image: " + count + ".bmp");
            input= imageReader.readImage(count + ".bmp");
            this.m_count++;

            range= (input[0]) / pnum2;
            BlurPieceL bplArray[]= new BlurPieceL[pnum2];
            for (int i= 0; i < pnum2; i++) {
                BlurPieceL bpl= new BlurPieceL(i, range, input, pnum2);
                bpl.blur();
                bplArray[i]= bpl;
            }
            setBPLNum(pnum2);
            startTrackingLoop();

            //TASK: blurL, addBPL
            for (int i= 0; i < pnum2; i++) {
                addBPL(bplArray[i]);
            }
            postBlur();

            resize();

            //TASK: calcTrack
            calcTrack(prevImage, prevImageR, ixlm, iylm, ixlmr, iylmr);

        }

        printFeatures();
        long stop= System.currentTimeMillis();
        System.out.println("Entire duration: " + (stop - start));
        System.out.println("Critical duration: " + (stopCritical - startCritical));
    }
}
