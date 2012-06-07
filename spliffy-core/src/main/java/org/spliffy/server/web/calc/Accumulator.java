package org.spliffy.server.web.calc;

import org.spliffy.server.web.SpliffyResource;

interface Accumulator {

    void accumulate(SpliffyResource r, Object o);
}
