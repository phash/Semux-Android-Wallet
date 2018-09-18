/*
 * MIT License
 *
 * Copyright (c) 2018 Manuel Roedig / Phash
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.phash.manuel.asw.semux.key;

import android.os.Build;

import java.math.BigDecimal;
import java.math.BigInteger;

import static java.math.RoundingMode.FLOOR;
import static java.util.Arrays.stream;

public final class Amount {

    public static final Amount ZERO = new Amount(0);
    private final long nano;

    public Amount(long nano) {
        this.nano = nano;
    }

    public static Amount neg(Amount a) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return new Amount(Math.negateExact(a.nano));
        }
        return new Amount(a.nano * -1L);
    }

    public static Amount sum(Amount a1, Amount a2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return new Amount(Math.addExact(a1.nano, a2.nano));
        }
        return new Amount(a1.nano + a2.nano);
    }

    public static Amount sub(Amount a1, Amount a2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return new Amount(Math.subtractExact(a1.nano, a2.nano));
        }
        return new Amount(a1.nano - a2.nano);
    }

    public long getNano() {
        return nano;
    }

    public int compareTo(Amount other) {
        return this.lt(other) ? -1 : (this.gt(other) ? 1 : 0);
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Long.hashCode(nano);
        }
        return Long.valueOf(nano).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Amount && ((Amount) other).nano == nano;
    }

    @Override
    public String toString() {
        return Unit.SEM.toDecimal(this, 9).stripTrailingZeros().toPlainString() + " SEM";
    }

    public boolean gt(Amount other) {
        return nano > other.nano;
    }

    public boolean gte(Amount other) {
        return nano >= other.nano;
    }

    public boolean gt0() {
        return gt(ZERO);
    }

    public boolean gte0() {
        return gte(ZERO);
    }

    public boolean lt(Amount other) {
        return nano < other.nano;
    }

    public boolean lte(Amount other) {
        return nano <= other.nano;
    }

    public boolean lt0() {
        return lt(ZERO);
    }

    public boolean lte0() {
        return lte(ZERO);
    }

    public enum Unit {
        NANO_SEM(0, "nSEM"),

        MICRO_SEM(3, "μSEM"),

        MILLI_SEM(6, "mSEM"),

        SEM(9, "SEM"),

        KILO_SEM(12, "kSEM"),

        MEGA_SEM(15, "MSEM");

        public final String symbol;
        private final int exp;
        private final long factor;

        Unit(int exp, String symbol) {
            this.exp = exp;
            this.factor = BigInteger.TEN.pow(exp).longValue();
            this.symbol = symbol;
        }

        public static Unit ofSymbol(String s) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return stream(values()).filter(i -> s.equals(i.symbol)).findAny().get();
            } else {
                for (Unit ele : values()) {
                    if (ele.symbol.equals(s)) {
                        return ele;
                    }
                }
            }
            return null;
        }

        public Amount of(long a) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return new Amount(Math.multiplyExact(a, factor));
            }
            return new Amount(a * factor);
        }

        public BigDecimal toDecimal(Amount a, int scale) {
            BigDecimal $nano = BigDecimal.valueOf(a.nano);
            return $nano.movePointLeft(exp).setScale(scale, FLOOR);
        }

        public Amount fromDecimal(BigDecimal d) {
            return new Amount(d.movePointRight(exp).setScale(0, FLOOR).longValueExact());
        }
    }

}
