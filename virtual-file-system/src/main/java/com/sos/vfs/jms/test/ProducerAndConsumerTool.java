package com.sos.vfs.jms.test;

import java.util.Arrays;
import java.util.HashSet;

import javax.jms.MessageListener;

import org.apache.activemq.protobuf.compiler.CommandLineSupport;

public class ProducerAndConsumerTool extends FileConsumerTool implements MessageListener {

    public static void main(String[] args) {
        FileConsumerTool consumerTool = new FileConsumerTool();
        String[] unknown = CommandLineSupport.setOptions(consumerTool, args);
        HashSet<String> set1 = new HashSet<String>(Arrays.asList(unknown));
        FileProducerTool producerTool = new FileProducerTool();
        unknown = CommandLineSupport.setOptions(producerTool, args);
        HashSet<String> set2 = new HashSet<String>(Arrays.asList(unknown));
        set1.retainAll(set2);
        if (!set1.isEmpty()) {
            System.out.println("Unknown options: " + set1);
            System.exit(-1);
        }
        consumerTool.run();
        producerTool.run();
    }

}