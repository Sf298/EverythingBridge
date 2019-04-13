/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf298.saudstoolbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Saud
 */
public class DeepFileIterator implements Iterator {
    
    private File root = null;
    private File curr = null;
    private File selectedFile = null;
    
    public DeepFileIterator(File rootFolder) {
        try {
            if(rootFolder.exists() && rootFolder.isDirectory() && rootFolder.listFiles().length > 0) {
                this.root = rootFolder;
                this.curr = rootFolder;
                goDeep();
            }
        } catch(NullPointerException e) {
            
        }
    }

    @Override
    public boolean hasNext() {
        return curr != null;
    }

    ArrayList<File[]> siblings = new ArrayList<>();
    ArrayList<Integer> treeIndex = new ArrayList<>();
    @Override
    public File next() {
        selectedFile = curr;
        File[] siblings = lastSibs();
        
        if(lastInd() < siblings.length-1) { // not end of folder
            lastIndInc(1);
            curr = siblings[lastInd()]; 
        } else {
            goUp();
        }
        if(curr != null) {
            goDeep();
        }
        return selectedFile;
    }
    
    public File get() {
        return selectedFile;
    }
    
    public String getRelative() {
        return selectedFile.getAbsolutePath().substring((int) root.getAbsolutePath().length());
    }

    @Override
    public void remove() {}
    
    private void goDeep() {
        while(curr.isDirectory()) {
            File[] files;
            try {
                files = curr.listFiles();
            } catch(NullPointerException e) {
                return;
            }
            if(files == null || files.length == 0) {
                return;
            }
            //todo add depth
            siblings.add(files);
            treeIndex.add(0);
            curr = lastSibs()[0];
        }
    }
    
    private void goUp() {
        while(lastInd() == lastSibs().length-1) { // while end of folder
            curr = curr.getParentFile();
            if(curr.equals(root)) {
                curr = null;
                return;
            }
            siblings.remove(siblings.size()-1);
            treeIndex.remove(treeIndex.size()-1);
        }
        lastIndInc(1);
        curr = lastSibs()[lastInd()];
    }
    
    private File[] lastSibs() {
        return siblings.get(siblings.size()-1);
    }
    private int lastInd() {
        return treeIndex.get(treeIndex.size()-1);
    }
    private void lastIndInc(int add) {
        treeIndex.set(treeIndex.size()-1, lastInd()+add);
    }
    
}
