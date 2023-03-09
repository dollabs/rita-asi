package utils;

/* Adapted from:
 * Author: Nicolas Garcia Belmonte
 * Copyright: Copyright 2007 by Nicolas Garcia Belmonte.  All rights reserved.
 * License: MIT License
 * Homepage: http://hypertree.woot.com.ar
 * Source: http://hypertree.woot.com.ar/js/HyperbolicTree.js
 * Version: 1.0
 */
public class Complex {
	public double	r, i;

	public Complex(double r, double i) {
		this.r = r;
		this.i = i;
	}

	public Complex(double angle) {
		r = Math.cos(angle);
		i = Math.sin(angle);
	}

	public double norm() {
		return Math.hypot(r, i);
	}

	public double squaredNorm() {
		return r * r + i * i;
	}

	public Complex add(Complex o) {
		return new Complex(r + o.r, i + o.i);
	}

	public Complex multiply(Complex o) {
		return new Complex(r * o.r - i * o.i, i * o.r + r * o.i);
	}

	public Complex conjugate() {
		return new Complex(r, -i);
	}

	public Complex scale(double factor) {
		return new Complex(r * factor, i * factor);
	}

	public Complex moebiusTransformation(double theta, Complex c) {
		Complex num = add(c.scale(-1));
		Complex den = new Complex(1, 0).add(c.conjugate().multiply(this).scale(-1));
		Complex numProd = den.conjugate();
		double denProd = den.multiply(den.conjugate()).r;
		return num.multiply(numProd).scale(1 / denProd);
	}

	public String toString() {
		return r + (i < 0 ? "-" : "+") + i + "i";
	}
}
