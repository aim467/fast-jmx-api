package com.fast.jmx;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

public class VmTest {

    public static void main(String[] args) throws IOException, AttachNotSupportedException {
        long i = 122880000;
        System.out.println(i / 1024 / 1024);
    }
}
