package com.windanesz.tracesofthefallen;

import net.minecraft.entity.ai.EntityMoveHelper;

import java.lang.reflect.Method;

public class TestMoveHelper {
    public static void main(String[] args) {
        for (Method m : EntityMoveHelper.class.getDeclaredMethods()) {
            System.out.println(m.getName());
            for (Class<?> p : m.getParameterTypes()) {
                System.out.println("  " + p.getName());
            }
        }
    }
}
