package com.itmo.r3135.System.Tools;

public class DatagramTrimer {
    private final static int value = 60000;
    private final static byte[] nextKey = {121, 111, 121};
    private final static byte[] finalKey = {121, 123, 121};

    public static byte[][] trimByte(byte[] input) {

        byte[][] output = new byte[input.length / value + 1][value + nextKey.length];
        byte[] lastArray = new byte[(input.length % value) + finalKey.length];
        int lastIndex = input.length / value;

        for (int i = 0; i < input.length; i++) {
            if (i / value == lastIndex) {
                lastArray[i % value] = input[i];
            } else output[i / value][i % value] = input[i];
        }
        output[output.length - 1] = lastArray;
        for (int i = 0; i < output.length - 1; i++) {
            for (int j = 0; j < nextKey.length; j++) {
                output[i][value + j] = nextKey[j];
            }
        }
        for (int j = 0; j < finalKey.length; j++) {
            output[output.length - 1][lastArray.length - 3 + j] = finalKey[j];
        }
        return output;
    }

    public static byte[] connectByte(byte[] thisByte, byte[] connectByte) {
        int lastIndex = 0;
        for (int i = 1; i <= connectByte.length; i++) {
            if (connectByte[connectByte.length - i] != 0) {
                lastIndex = connectByte.length - i;
                break;
            }
        }
        byte[] output = new byte[thisByte.length + lastIndex - 2];
        for (int i = 0; i < thisByte.length; i++) {
            output[i] = thisByte[i];
        }
//        System.out.println("последние символы сообщения " + connectByte[lastIndex - 2] +
//        " " + connectByte[lastIndex - 1] + " " + connectByte[lastIndex]);
        for (int i = 0; i < lastIndex - 2; i++) {
            output[thisByte.length + i] = connectByte[i];
        }
        return output;
    }

    public static boolean isFinal(byte[] input) {
        boolean b = true;
        int lastIndex = 0;
        for (int i = 1; i <= input.length; i++) {
            if (input[input.length - i] != 0) {
                lastIndex = input.length - i;
                break;
            }
        }
        if (lastIndex == 0) b = false;
        for (int i = 0; i < 3; i++) {
            if (input[lastIndex + i - 2] != finalKey[i]) b = false;
//            System.out.println(b);
        }
        return b;
    }

    public static byte[] setFinal(byte[] input) {
        byte[] output = new byte[input.length + 3];
        for (int i = 0; i < input.length; i++) {
            output[i] = input[i];
        }
        for (int j = 0; j < finalKey.length; j++) {
            output[input.length + j] = finalKey[j];
        }
        return output;
    }
}
