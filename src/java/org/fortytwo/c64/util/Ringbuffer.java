/**
 * $Id: Ringbuffer.java,v 1.1.1.1 2007/01/19 07:14:52 jwoolev Exp $ 
 *
 * (c)1997 IoS Gesellschaft fr innovative Softwareentwicklung mbH
 * http://www.IoS-Online.de    mailto:info@IoS-Online.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.  
 *
 * This class implements an Ringbuffer. This
 * means, it has a fixed size and you can add
 * as many elements as you want. If it the 
 * buffer is full the first elements are
 * overwritten.
 */

//package de.ios.framework.basic;
package org.fortytwo.c64.util;

public class Ringbuffer {
  /**
   * Ringbuffer construction
   *
   * @param size Size of buffer
   */
  public Ringbuffer(int _size) {
    buffer = (Object[])(new Object[_size]);
    first = last = current = -1;
  }

  /**
   * Add Element to the end of the buffer. 
   * The current Element is the one just 
   * inserted.
   */
  public void add(Object o) {
    addLast( o );
  }

  /**
   * Add Element to the end of the buffer. 
   * The current Element is the one just 
   * inserted.
   */
  public void addLast(Object o) {
    if (last >= 0) {
      // not empty!
      ++last;
      if (last  >= buffer.length) last = 0;
      if (last  == first        ) first++;
      if (first >= buffer.length) first = 0;
    } else {
      first = last = 0;
    }
    buffer[last] = o;
    current = last;
  }

  /**
   * Add an elemenent at the begin of the buffer.
   * The current Element is the one just 
   * inserted.
   */
  public void addFirst(Object o) {
    if (last >= 0) {
      // not empty
      --first;
      if (first < 0) first = buffer.length-1;
      if (first == last) last--;
      if (last  < 0) last  = buffer.length-1;
    } else {
      first = last = 0;
    }
    buffer[ first ] = o;
    current = first;
  }

  /**
   * Get the first Object
   */
  public Object getFirst() {
    current = first;
    return getCurrent();
  }

  /**
   * Get the next T.
   */
  public Object getNext() {
    if (last >= 0) {
      // not empty
      if (current == last)
	return null;
      current++;
      if (current >= buffer.length) current = 0;
      return getCurrent();
    } else
      return null;
  }

  /**
   * Get the previous T.
   */
  public Object getPrev() {
    if (current >= 0) {
      // not empty
      if (current == first) 
	return null;
      current--;
      if (current < 0) current = buffer.length-1;
      return getCurrent();
    } else
      return null;
  }

  /**
   * Get the current T
   */
  public Object getCurrent() {
    if (current < 0)
      return null;
    return buffer[current];
  }

  /**
   * Removes all elements.
   */
  public void clear() {
    int i;
    for (i=0 ; i<buffer.length ; i++)
      buffer[i] = null;
    first = last = current = -1;
  }

  /**
   * Resize buffer. Copies maximal the last "size" elements. 
   * If current position is not in range, it is set to the first element.
   *
   * @param size New size of the buffer, must be greater or equal 1.
   */
  public void resize( int size ) {

    /* REMEMBER:
     *   The new buffer is filled from the end. The elements up to "first" are
     *   empty.
     */

    if (size<=0)
      size = 1;
    if (size == buffer.length)
      return;

    Object nb[] = new Object[ size ];

    if (first >= 0) {
      // not empty. copy the last "size" elements.
      Object obj;
      int newFirst   = 0;
      int newCurrent = -1;
      int pos = last;
      int i;
      for (i=size-1 ; i>=0 ; i--) {
	nb[i] = buffer[pos];
	if (pos == current) newCurrent = i;
	if (pos == first  ) {
	  // Begin reached.
	  newFirst = i;
	  break;
	}
	pos--;
	if (pos<0) pos = buffer.length-1;
      }
      last  = size-1;
      first = newFirst;
      current = (newCurrent >= 0) ? newCurrent : first;
    }
    buffer = nb;
  }

  Object buffer[];
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
