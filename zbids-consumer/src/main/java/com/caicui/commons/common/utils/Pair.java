package com.caicui.commons.common.utils;

import java.io.Serializable;

/**
 * Description: All Rights Reserved.
 *
 * @version 1.0 2013-12-13 下午3:53:46 by 于科为 kw.yu@zuche.com
 */
public class Pair<L, R> implements Serializable {
    private static final long serialVersionUID = -547163732852159913L;
    private L left;
    private R right;

    /**
     * @param left
     * @param right
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public R getRight() {
        return right;
    }

    public void setRight(R right) {
        this.right = right;
    }

    public static <L, R> Pair<L, R> makePair(L left, R right) {
        return new Pair<L, R>(left, right);
    }
}


