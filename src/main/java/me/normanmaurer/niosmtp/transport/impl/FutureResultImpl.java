package me.normanmaurer.niosmtp.transport.impl;

import me.normanmaurer.niosmtp.transport.FutureResult;



public class FutureResultImpl<E> extends FutureResult<E>{

    private E result;
    public FutureResultImpl(E result) {
        super(null);
        this.result = result;
    }
    @Override
    public E getResult() {
        return result;
    }

}
