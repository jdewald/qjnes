/**
 * $Id: Ringbuffer.java,v 1.1.1.1 2007/01/19 07:14:52 jwoolev Exp $
 * <p>
 * (c)1997 IoS Gesellschaft fr innovative Softwareentwicklung mbH
 * http://www.IoS-Online.de    mailto:info@IoS-Online.de
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * <p>
 * This class implements an Ringbuffer. This
 * means, it has a fixed size and you can add
 * as many elements as you want. If it the
 * buffer is full the first elements are
 * overwritten.
 */

package org.fortytwo.common.util;

import java.util.Vector;
import java.util.stream.IntStream;

public class Ringbuffer<T> {
    /**
     * Ringbuffer construction
     *
     * @param _size Size of buffer
     */
    public Ringbuffer(int _size) {
        buffer = new Vector<>(_size);
        IntStream.range(0, _size)
                .forEach(i -> buffer.add((T) new Object()));//TODO: This is silly
        first = last = current = -1;
    }

    /**
     * Add Element to the end of the buffer.
     * The current Element is the one just
     * inserted.
     */
    public void add(T o) {
        addLast(o);
    }

    /**
     * Add Element to the end of the buffer.
     * The current Element is the one just
     * inserted.
     */
    public void addLast(T o) {
        if (last >= 0) {
            // not empty!
            ++last;
            if (last >= buffer.size()) last = 0;
            if (last == first) first++;
            if (first >= buffer.size()) first = 0;
        } else {
            first = last = 0;
        }
        buffer.set(last, o);
        current = last;
    }

    /**
     * Get the first Object
     */
    public T getFirst() {
        current = first;
        return getCurrent();
    }

    /**
     * Get the next T.
     */
    public T getNext() {
        if (last >= 0) {
            // not empty
            if (current == last)
                return null;
            current++;
            if (current >= buffer.size()) current = 0;
            return getCurrent();
        } else
            return null;
    }

    /**
     * Get the current T
     */
    public T getCurrent() {
        if (current < 0)
            return null;
        return buffer.get(current);
    }

    /**
     * Removes all elements.
     */
    public void clear() {
        buffer.forEach(obj -> buffer.setElementAt(null, buffer.indexOf(obj))); //This looks awful !!!
        first = last = current = -1;
    }

    Vector<T> buffer;
    int first;
    int last;
    int current;
}

/*
 * $Log: Ringbuffer.java,v $
 * Revision 1.1.1.1  2007/01/19 07:14:52  jwoolev
 * Initial safety import
 *
 * Revision 1.2  2007/01/19 07:14:52  quay
 * No real change... just a declaration of nb
 *
 * Revision 1.1.1.1  2006/06/18 04:28:43  quay
 * Commodore 64 Emulator
 *
 * Revision 1.1.1.1  2001/02/07 15:25:00  rtfm
 * initial
 *
 * Revision 1.6  1998/04/09 09:56:01  bw
 * .
 *
 * Revision 1.5  1998/01/08 14:06:34  bw
 * Bugs fixed.
 * Now resizeable.
 * Elements can be inserted at the end or the begin.
 *
 * Revision 1.4  1997/12/18 17:48:39  bw
 * Wieder da.
 *
 * Revision 1.2  1997/12/10 19:38:27  bb
 * Copyright eingefgt.
 *
 * Revision 1.1  1997/12/10 11:23:53  bb
 * Erste Version
 *
 */
