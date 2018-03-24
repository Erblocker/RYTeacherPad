package com.inqbarna.tablefixheaders;

import android.view.View;
import java.util.EmptyStackException;
import java.util.Stack;

public class Recycler {
    private Stack<View>[] views;

    public Recycler(int size) {
        this.views = new Stack[size];
        for (int i = 0; i < size; i++) {
            this.views[i] = new Stack();
        }
    }

    public void addRecycledView(View view, int type) {
        this.views[type].push(view);
    }

    public View getRecycledView(int typeView) {
        try {
            return (View) this.views[typeView].pop();
        } catch (EmptyStackException e) {
            return null;
        }
    }
}
