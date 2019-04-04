/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge.actionapis;

import java.util.Objects;

/**
 *
 * @author saud
 */
public class Device {
    public String ip;
    public int port;
    public String label;
    public Device(String ip, int port, String label) {
        this.ip = ip;
        this.port = port;
        this.label = label;
    }
    public Device(String ip, int port) {
        this(ip, port, null);
    }

    @Override
    public String toString() {
        return label;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.ip);
        hash = 89 * hash + this.port;
        hash = 89 * hash + Objects.hashCode(this.label);
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
        final Device other = (Device) obj;
        if (this.port != other.port) {
            return false;
        }
        if (!Objects.equals(this.ip, other.ip)) {
            return false;
        }
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        return true;
    }

    public String toFormattedString() {
        return label+" = "+ip+":"+port;
    }

}
    
