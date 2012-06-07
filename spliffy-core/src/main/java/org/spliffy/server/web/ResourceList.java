/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spliffy.server.web;

import org.spliffy.server.web.calc.Calc;
import com.bradmcevoy.http.Resource;
import java.util.*;
import org.apache.log4j.Logger;
import org.spliffy.server.web.templating.ChildrenOfTypeMap;

/**
 *
 * @author brad
 */
public class ResourceList extends ArrayList<SpliffyResource> {

    private static final Logger log = Logger.getLogger(ResourceList.class);
    private static final long serialVersionUID = 1L;
    private final Map<String, SpliffyResource> map = new HashMap<>();

    public ResourceList() {
    }

    public ResourceList(SpliffyResource[] array) {
        addAll(Arrays.asList(array));
    }

    public ResourceList(ResourceList copyFrom) {
        super(copyFrom);
    }

    @Override
    public boolean add(SpliffyResource e) {
        if (e == null) {
            throw new NullPointerException("Attempt to add null node");
        }
        if (e.getName() == null) {
            throw new NullPointerException("Attempt to add resource with null name: " + e.getClass().getName());
        }
        if (map.containsKey(e.getName())) {
            log.debug("identical child names: " + e.getName());//,ex);

            // BM: can't remove files with identical names because this list is often used across folders, so name is not unique

//            Resource cur = map.get(e.getName());
//            if (cur.getModifiedDate() == null) {
//                remove(cur);
//            } else if (e.getModifiedDate() == null) {
//                // ignore
//                return true;
//            } else {
//                if (e.getModifiedDate().after(cur.getModifiedDate())) {
//                    remove(cur);
//                } else {
//                    return true;
//                }
//            }
        }
        map.put(e.getName(), e);
        boolean b = super.add(e);
        return b;
    }

    /**
     * Just adds the elements in the given list to this list and returns list to
     * make it suitable for chaining and use from velocity
     *
     * @param otherList
     * @return
     */
    public ResourceList add(ResourceList otherList) {
        addAll(otherList);
        return this;
    }

    public SpliffyResource get(String name) {
        return map.get(name);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof SpliffyResource) {
            SpliffyResource e = (SpliffyResource) o;
            map.remove(e.getName());
        }
        int i = super.indexOf(o);
        Object removed = super.remove(i);
        return (removed != null);
    }

    public SpliffyResource getFirst() {
        if (isEmpty()) {
            return null;
        }
        return this.get(0);
    }

    public SpliffyResource getLast() {
        if (this.size() > 0) {
            return this.get(this.size() - 1);
        } else {
            return null;
        }
    }

    public SpliffyResource getRandom() {
        int l = this.size();
        if (l == 0) {
            return null;
        }

        List<SpliffyResource> list = new ArrayList<>();
        for (SpliffyResource res : this) {
            list.add(res);
        }
        if (list.isEmpty()) {
            return null;
        }

        Random rnd = new Random();
        int pos = rnd.nextInt(list.size());
        return list.get(pos);
    }

    public ResourceList getReverse() {
        ResourceList list = new ResourceList(this);
        Collections.reverse(list);
        return list;
    }

    public ResourceList getSortByModifiedDate() {
        ResourceList list = new ResourceList(this);
        Collections.sort(list, new Comparator<Resource>() {

            @Override
            public int compare(Resource o1, Resource o2) {
                Date dt1 = o1.getModifiedDate();
                Date dt2 = o2.getModifiedDate();
                if (dt1 == null) {
                    return -1;
                }
                return -1 * dt1.compareTo(dt2);
            }
        });
        return list;
    }

    public ResourceList getRandomSort() {
        SpliffyResource[] array = new SpliffyResource[this.size()];
        this.toArray(array);

        Random rng = new Random();   // i.e., java.util.Random.
        int n = array.length;        // The number of items left to shuffle (loop invariant).
        while (n > 1) {
            int k = rng.nextInt(n);  // 0 <= k < n.
            n--;                     // n is now the last pertinent index;
            SpliffyResource temp = array[n];     // swap array[n] with array[k] (does nothing if k == n).
            array[n] = array[k];
            array[k] = temp;
        }
        ResourceList newList = new ResourceList(array);
        return newList;
    }

    public ResourceList exclude(String s) {
        return _exclude(s);
    }

    public ResourceList exclude(String s1, String s2) {
        return _exclude(s1, s2);
    }

    public ResourceList exclude(String s1, String s2, String s3) {
        return _exclude(s1, s2, s3);
    }

    public ResourceList exclude(String s1, String s2, String s3, String s4) {
        return _exclude(s1, s2, s3, s4);
    }

    public ResourceList _exclude(String... s) {
        ResourceList newList = new ResourceList(this);
        Iterator<SpliffyResource> it = newList.iterator();
        while (it.hasNext()) {
            Resource ct = it.next();
            if (contains(s, ct.getName())) {
                it.remove();
            }
        }
        return newList;
    }

    private boolean contains(String[] arr, String name) {
        for (String s : arr) {
            if (name.equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a new list where elements satisfy is(s)
     *
     * @param s
     * @return
     */
    public ResourceList ofType(String s) {
        ResourceList newList = new ResourceList(this);
        Iterator<SpliffyResource> it = newList.iterator();
        while (it.hasNext()) {
            SpliffyResource ct = it.next();
            if (!ct.is(s)) {
                it.remove();
            }
        }
        return newList;
    }

    /**
     * Return a new list with a size no greater then the given argument
     *
     * @param maxSize - the maximum number of elements in the new list
     * @return
     */
    public ResourceList truncate(int maxSize) {
        ResourceList list = new ResourceList();
        for (int i = 0; i < maxSize && i < size(); i++) {
            list.add(get(i));
        }
        return list;
    }

    public Map<String, ResourceList> getOfType() {
        return new ChildrenOfTypeMap(this);
    }

    /**
     * Returns the next item after the one given. If the given argument is null,
     * returns the first item in the list
     *
     * @param from
     * @return
     */
    public Resource next(Resource from) {
        if (from == null) {
            return getFirst();
        } else {
            boolean found = false;
            for (Resource r : this) {
                if (found) {
                    return r;
                }
                if (r == from) {
                    found = true;
                }
            }
            return null;
        }
    }
}
