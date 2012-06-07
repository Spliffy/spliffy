package org.spliffy.server.web.calc;

import java.math.BigDecimal;
import org.spliffy.server.web.SpliffyResource;
import org.spliffy.server.web.templating.Formatter;

class Sumor implements Accumulator {

    private final int decimals;
    private final Formatter formatter;

    public Sumor(int decimals, Formatter formatter) {
        super();
        this.decimals = decimals;
        this.formatter = formatter;
    }
    BigDecimal value = new BigDecimal(0);

    @Override
    public void accumulate(SpliffyResource r, Object o) {
        BigDecimal bd = formatter.toBigDecimal(o, decimals);
        if (bd != null) {
            value = value.add(bd);
        }
    }
}
