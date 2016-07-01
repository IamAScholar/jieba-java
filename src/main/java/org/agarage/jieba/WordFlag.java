package org.agarage.jieba;

/**
 * Created by Nicholas on 2016/7/2.
 */
public enum WordFlag {
    DF, DG, UD, UG, UJ, UL, MG, UV, AD, MQ, UZ, AG, ZG, AN, VD, NRT, VG, VI, RG, NRFG, VN, NG, VQ, RR, NR, NS, NT, RZ, NZ, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, TG, T, U, V, X, Y, Z,
    JN, QE, LN, BG, IN, QG, YG, ENG;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
