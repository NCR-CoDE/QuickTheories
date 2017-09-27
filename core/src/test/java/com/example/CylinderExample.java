package com.example;

import java.math.BigDecimal;

import org.junit.Test;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;

public class CylinderExample implements WithQuickTheories {

  static class Cylinder {
    private final int radius;
    private final int height;

    Cylinder(int radius, int height) {
      this.radius = radius;
      this.height = height;
    }

    int radius() {
      return radius;
    }

    int height() {
      return height;
    }

    BigDecimal area() {
      BigDecimal r = new BigDecimal(radius);
      BigDecimal h = new BigDecimal(height);

      return BigDecimal.valueOf(Math.PI)
          .multiply(BigDecimal.valueOf(2))
          .multiply(r)
          .multiply(r.add(h));
    }

     @Override
     public String toString() {
     return "Cylinder [radius=" + radius + ", height=" + height + ", area"
     + area() + "]";
     }

  }

  @Test
  public void someTestInvolvingCylinders() {
    qt()
        .forAll(integers().allPositive().describedAs(r -> "Radius = " + r),
            integers().allPositive().describedAs(h -> "Height = " + h))
        .as((r, h) -> new Cylinder(r, h))
        .describedAs(cylinder -> "Cylinder r =" + cylinder.radius() + " h ="
            + cylinder.height())
        .check(l -> false);
  }

  @Test
  public void someTestInvolvingCylinders2() {
    qt()
        .forAll(integers().allPositive().describedAs(r -> "Radius = " + r),
            integers().allPositive().describedAs(h -> "Height = " + h))
        .asWithPrecursor((r, h) -> new Cylinder(r, h),
            cylinder -> "Cylinder r =" + cylinder.radius() + " h ="
                + cylinder.height())
        .check((i, j, l) -> false);
  }

  @Test
  public void areaIsAlwaysPositive() {
    qt()
        .forAll(cylinders())
        .assuming(cylinder -> cylinder.height > 0 && cylinder.radius > 0)
        .check(cylinder -> cylinder.area().compareTo(BigDecimal.ZERO) > 1000);

  }

  private Gen<Cylinder> cylinders() {
    return radii().zip(heights(),
        (radius, height) -> new Cylinder(radius, height));
  }

  private Gen<Integer> heights() {
    return integers().from(79).upToAndIncluding(1004856);
  }

  private Gen<Integer> radii() {
    return integers().allPositive();
  }

}
