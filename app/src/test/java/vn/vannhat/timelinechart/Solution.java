package vn.vannhat.timelinechart;

import org.junit.Test;

import java.util.*;


public class Solution {
    static String slurpStdin() {
        String input = "";
        Scanner scan = new Scanner(System.in);

        while (true) {
            input += scan.nextLine();
            if (scan.hasNextLine()) {
                input += "\n";
            } else {
                break;
            }
        }

        return input;
    }

    public static void main(String[] args) {
        String input = slurpStdin();
        String[] array = input.split(" ");
        Long n = Long.parseLong(array[0]);
        int length = array.length;
        for (int i = 1; i < length; i++) {
            Long num = Long.parseLong(array[i].replace("\n",""));

            if (isPrime(num)) System.out.println("YES");
            else System.out.println("NO");
        }
    }

    static boolean  isPrime(Long num)
    {
        if(num==1) return false;
        if(num==2 || num==3) return true;
        if(num%2==0 || num%3==0) return false;
        for(Long i=5L; i*i<=num; i+=6)
        {
            if(num%i==0 || num%(i+2)==0)
                return false;
        }
        return true;
    }
}




