/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf298.saudstoolbox;

import java.util.HashSet;
import java.util.Objects;

/**
 *
 * @author saud
 */
public class BitSet2 {
    
    private int size = 0;
    private HashSet<Integer> set = new HashSet<>();
    private String stringCache = null;
    
    public BitSet2() {
        
    }
    public BitSet2(BitSet2 b) {
        this.size = b.size;
        this.set = new HashSet<>(b.set);
        this.stringCache = b.stringCache;
    }
    public static BitSet2 parse(byte b) {
        BitSet2 out = new BitSet2();
        for(int i=0; i<8; i++) {
            out.set(8-i-1, (b&1)==1);
            b = (byte)(b >>> 1);
        }
        return out;
    }
    public static BitSet2 parse(byte[] b) {
        BitSet2 out = new BitSet2();
        for(int i=0; i<b.length; i++) {
            out.append(parse(b[i]));
        }
        return out;
    }
    
    public int length() {
        return size;
    }
    
    public void set(int i, boolean value) {
        clearCache();
        if(i<0)
            throw new IndexOutOfBoundsException();
        if(value) {
            set.add(i);
        } else {
            set.remove(i);
        }
        if(i >= size) size = i+1;
    }
    
    public void append(boolean value) {
        this.set(size, value);
    }
    public void append(BitSet2 value) {
        for(int i=0; i<value.size; i++) {
            this.append(value.get(i));
        }
    }
    
    public void flip(int i) {
        clearCache();
        if(i<0)
            throw new IndexOutOfBoundsException();
        if(set.contains(i)) {
            set.add(i);
        } else {
            set.remove(i);
        }
    }
    
    public BitSet2 shiftLeft(int spaces) {
        if(spaces < 1) return new BitSet2(this);
        BitSet2 out = new BitSet2();
        for(Integer i : set) {
            out.set.add(i-spaces);
        }
        return out;
    }
    public BitSet2 shiftRight(int spaces) {
        if(spaces < 1) return new BitSet2(this);
        BitSet2 out = new BitSet2();
        for(Integer i : set) {
            int newPos = i+spaces;
            if(0<=newPos && newPos<size)
                out.set.add(newPos);
        }
        return out;
    }
    
    public boolean get(int i) {
        if(i<0 || i>=size)
            throw new IndexOutOfBoundsException();
        return set.contains(i);
    }
    
    public long getAsLong() {
        long out = 0;
        for(int i=0; i<this.length(); i++) {
            out = out << 1;
            if(set.contains(i)) out++;
        }
        return out;
    }
    public int getAsInt() {
        int out = 0;
        for(int i=0; i<this.length(); i++) {
            out = out << 1;
            if(set.contains(i)) out++;
        }
        return out;
    }
    
    public BitSet2 subset(int start, int length) {
        BitSet2 out = new BitSet2();
        for(int i=0; i<length; i++) {
            out.append(this.get(i+start));
        }
        return out;
    }
    
    public byte[] toByteArray() {
        byte[] out = new byte[(int)Math.ceil(size/8.0)];
        for(int byteI=0; byteI<out.length; byteI++) {
            byte b = 0;
            for(int bitI=0; bitI<8; bitI++) {
                b = (byte)(b << 1);
                if(set.contains(byteI*8+bitI)) b++;
            }
            out[byteI] = b;
        }
        return out;
    }
    
    public BitSet2 reverseBits() {
        BitSet2 out = new BitSet2();
        for(Integer i : set) {
            out.set.add(size-i-1);
        }
        out.size = size;
        return out;
    }
    
    public BitSet2 reverseBytes() {
        BitSet2 out = new BitSet2();
        int sizeBytes = size/8;
        for(Integer i : set) {
            int byteI = i/8;
            int byteR = i%8;
            
            int newPos = (sizeBytes-byteI-1)*8 + byteR;
            out.set.add(newPos);
        }
        out.size = size;
        return out;
    }
    
    public BitSet2 swapBytePairs() {
        BitSet2 out = new BitSet2();
        for(Integer i : set) {
            int byteI = i/8; // 1
            int isByteOdd = ((byteI&1)==1)?-1:1;
            int byteR = i%8;
            
            int newPos = (byteI+isByteOdd)*8 + byteR;
            out.set.add(newPos);
        }
        out.size = size;
        return out;
    }
    
    private void clearCache() {
        hashcodeCache = -1;
    }
    
    private int hashcodeCache = -1;
    @Override
    public int hashCode() {
        if(hashcodeCache == -1) {
            hashcodeCache = 7;
            hashcodeCache = 61 * hashcodeCache + this.size;
            hashcodeCache = 61 * hashcodeCache + Objects.hashCode(this.set);
        }
        return hashcodeCache;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BitSet2 other = (BitSet2) obj;
        if (this.size != other.size) {
            return false;
        }
        if (!Objects.equals(this.set, other.set)) {
            return false;
        }
        return true;
    }
    
}
