package io.github.orioncraftmc.meditate.internal.detail;

//----------------------------------------------------------------------------------------
//	Copyright © 2006 - 2021 Tangible Software Solutions, Inc.
//	This class can be used by anyone provided that the copyright notice remains intact.
//
//	This class is used to replicate the ability to pass arguments by reference in Java.
//----------------------------------------------------------------------------------------
public final class RefObject<T> {
    public T argValue;

    public RefObject(T refArg) {
        argValue = refArg;
    }
}
