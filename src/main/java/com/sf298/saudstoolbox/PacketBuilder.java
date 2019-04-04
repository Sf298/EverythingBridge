/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf298.saudstoolbox;

import com.sf298.saudstoolbox.BitSet2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author saud
 */
public class PacketBuilder {
    
    public ArrayList<Field> data = new ArrayList<>();
    public HashMap<String, Integer> fieldNames = new HashMap<>();
    public boolean swapBytePairs;
    public boolean reverseBytes;
    
    public PacketBuilder(boolean swapBytePairs, boolean reverseBytes) {
        this.swapBytePairs = swapBytePairs;
        this.reverseBytes = reverseBytes;
    }
    public PacketBuilder(boolean swapBytePairs, boolean reverseBytes, int... fieldSizes) {
        this(swapBytePairs, reverseBytes);
        for(int i : fieldSizes) {
            data.add(new Field(i, 0));
        }
    }
    public PacketBuilder(PacketBuilder pb) {
        this(pb.swapBytePairs, pb.reverseBytes);
        for(Field f : pb.data) {
            data.add(new Field(f));
        }
        lengthCache = pb.lengthCache;
        bitsetCache = pb.bitsetCache;
        stringCache = pb.stringCache;
    }
    
    public void addField(int length, long value) {
        clearCache();
        if(value >= (1l<<(length)))
            throw new RuntimeException("Value bits exceeds field length");
        data.add(new Field(length, value));
    }
    public void addField(String fieldName, int length, long value) {
        fieldNames.put(fieldName, data.size());
        addField(length, value);
    }
    
    public void setFieldValue(int fieldIndex, int value) {
        clearCache();
        Field f = data.get(fieldIndex);
        if(value >= (1l<<(f.length-1)))
            throw new RuntimeException("Value bits exceeds field length");
        f.value = value;
    }
    public void setFieldValue(String fieldName, int value) {
        int i = fieldNames.getOrDefault(fieldName, -1);
        if(i == -1)
            throw new RuntimeException("Field name '"+fieldName+"' not found");
        this.setFieldValue(i, value);
    }
    
    public long getFieldValue(int fieldIndex) {
        return data.get(fieldIndex).value;
    }
    public long getFieldValue(String fieldName) {
        int i = fieldNames.getOrDefault(fieldName, -1);
        if(i == -1)
            throw new RuntimeException("Field name '"+fieldName+"' not found");
        return this.getFieldValue(i);
    }
    
    public void setFieldLength(int fieldIndex, int length) {
        clearCache();
        Field f = data.get(fieldIndex);
        if(f.value >= (1l<<(length-1)))
            throw new RuntimeException("Value bits exceeds field length");
        f.length = length;
    }
    public void setFieldLength(String fieldName, int length) {
        int i = fieldNames.getOrDefault(fieldName, -1);
        if(i == -1)
            throw new RuntimeException("Field name '"+fieldName+"' not found");
        this.setFieldLength(i, length);
    }
    
    public int getFieldLength(int fieldIndex) {
        return data.get(fieldIndex).length;
    }
    public int getFieldLength(String fieldName) {
        int i = fieldNames.getOrDefault(fieldName, -1);
        if(i == -1)
            throw new RuntimeException("Field name '"+fieldName+"' not found");
        return this.getFieldLength(i);
    }
    
    public void swapBytePairs(int fieldIndex) {
        Field f = data.get(fieldIndex);
        BitSet2 b = f.toBitSet2();
        b.swapBytePairs();
        f.value = b.getAsLong();
    }
    public void swapBytePairs(String fieldName) {
        int i = fieldNames.getOrDefault(fieldName, -1);
        if(i == -1)
            throw new RuntimeException("Field name '"+fieldName+"' not found");
        this.swapBytePairs(i);
    }
    
    public void reverseBytes(int fieldIndex) {
        Field f = data.get(fieldIndex);
        BitSet2 b = f.toBitSet2();
        b.reverseBytes();
        f.value = b.getAsLong();
    }
    public void reverseBytes(String fieldName) {
        int i = fieldNames.getOrDefault(fieldName, -1);
        if(i == -1)
            throw new RuntimeException("Field name '"+fieldName+"' not found");
        this.reverseBytes(i);
    }
    
    public void loadPacketData(byte[] bytes) {
        if(bytes.length*8 != this.length())
            throw new RuntimeException("Bit length mismatch, got: "+(bytes.length*8)+", expected: "+this.length());
        clearCache();
        BitSet2 bits = BitSet2.parse(bytes);
        if(swapBytePairs) bits = bits.swapBytePairs();
        
        int startPos = 0;
        for(int i=0; i<data.size(); i++) {
            Field f = data.get(i);
            BitSet2 temp = bits.subset(startPos, f.length);
            f.value = temp.getAsInt();
            startPos += f.length;
        }
        
    }
    
    private void clearCache() {
        bitsetCache = null;
        stringCache = null;
        lengthCache = -1;
    }
    
    private int lengthCache = -1;
    public int length() {
        if(lengthCache == -1) {
            lengthCache = 0;
            for(int i=0; i<data.size(); i++) {
                lengthCache += data.get(i).length;
            }
        }
        return lengthCache;
    }
    
    private BitSet2 bitsetCache = null;
    public BitSet2 compileBits() {
        if(bitsetCache == null) {
            bitsetCache = new BitSet2();
            for(Field p : data) {
                BitSet2 temp = p.toBitSet2();
                if(reverseBytes)
                    temp = temp.reverseBytes();
                bitsetCache.append(temp);
            }
        }
        if(swapBytePairs) bitsetCache = bitsetCache.swapBytePairs();
        return bitsetCache;
    }
    
    private String stringCache = null;
    @Override
    public String toString() {
        if(stringCache == null) {
            BitSet2 bits = compileBits();
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<bits.length(); i++) {
                sb.append(bits.get(i) ? 1 : 0);
            }
            stringCache = sb.toString();
        }
        return stringCache;
    }
    public String getFieldsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for(int i=0; i<data.size(); i++) {
            String key = ""+i;
            for (Map.Entry<String, Integer> entry : fieldNames.entrySet()) {
                String key1 = entry.getKey();
                Integer value = entry.getValue();
                if(value == i) key = key1;
            }
            sb.append(key).append("=").append(data.get(i)).append(", ");
        }
        return sb.substring(0, sb.length()-2)+"}";
    }
    /*public String toFormattedString() {
        if(stringCache == null) {
            BitSet2 bits = compileBits();
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<bits.length(); i++) {
                sb.append(bits.get(i) ? 1 : 0);
                if(i%8==7) sb.append(" ");
            }
            stringCache = sb.toString();
        }
        return stringCache;
    }*/

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.data);
        return hash;
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
        final PacketBuilder other = (PacketBuilder) obj;
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }
    
    private class Field {
        int length;
        long value;

        public Field(int length, long value) {
            this.length = length;
            this.value = value;
        }
        
        public Field(Field f) {
            this.length = f.length;
            this.value = f.value;
        }
        
        public BitSet2 toBitSet2() {
            BitSet2 out = new BitSet2();
            long temp = value;
            for(int i=0; i<length; i++) {
                out.append((temp&1) == 1);
                temp = temp >>> 1;
            }
            return out.reverseBits();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + this.length;
            hash = 89 * hash + Long.hashCode(this.value);
            return hash;
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
            final Field other = (Field) obj;
            if (this.length != other.length) {
                return false;
            }
            if (this.value != other.value) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "{l="+length+",v="+value+'}';
        }
        
    }
    
    public static byte[] rev(byte[] arr) {
        byte[] out = new byte[arr.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = rev(arr[i]);
        }
        return out;
    }
    public static byte rev(byte b) {
        byte out = 0;
        for (int i = 0; i < 8; i++) {
            out = (byte) (out << 1);
            out += b & 1;
            b = (byte) (b>>>1);
        }
        return out;
    }
    
}
