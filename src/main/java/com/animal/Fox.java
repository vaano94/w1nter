package com.animal;

@SnowFlake("Mindy")
@Report("/report")
public class Fox {
    public int age = 25;

    public void printAge() {
        System.out.println(age);
    }
}
