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
    public String ip_id;
    public int port;
    public String label;
    public Device(String ip_id, int port, String label) {
        this.ip_id = ip_id;
        this.port = port;
        this.label = label;
    }
    public Device(String ip_id, int port) {
        this(ip_id, port, null);
    }
    public Device(Device d) {
        this(d.ip_id, d.port, d.label);
    }

    @Override
    public String toString() {
        return label;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.ip_id);
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
        if (!Objects.equals(this.ip_id, other.ip_id)) {
            return false;
        }
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        return true;
    }

    public String toFormattedString() {
        return label+" = "+ip_id+":"+port;
    }

}
    
