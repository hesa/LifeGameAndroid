/*
 *    Copyright (C) 2019 Henrik Sandklef
 *
 *    This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package se.juneday;

import java.util.Collection;
import java.io.File;

public class ObjectCacheReader<T> {

    ObjectCache<T> cache;


    /**
     * Creates a new ObjectCacheReader instance.
     *
     * @param fileName A String used to pass to the ObjectCache constructor.
     */
    public ObjectCacheReader(String fileName) {
        cache = new ObjectCache<>(fileName);
    }

    /**
     * Retrieves and prints the list of stored objects from
     * file.
     */
    public void printObject() {
        if (cache == null ||
                cache.readObject() == null) {
            System.err.println("cache or cached objects null, nothing to print");
        } else {

            if (cache.readObject() instanceof Object[]) {
                String collectionName;
                Object[] objects = (Object[]) cache.readObject();
                if (objects == null) {
                    System.err.println("Array null, nothing to print");
                    return;
                }
                if (objects.length == 0) {
                    System.err.println("Array empty, nothing to print");
                    return;
                }
                collectionName = objects.getClass().getSimpleName();

                System.out.println("Array[" +
                        objects[0].getClass().getName() +
                        "] (" + objects.length +
                        " cached objects):");
                for (int i = 0; i < objects.length; i++) {
                    System.out.println(" " + i + ": " + objects[i]);
                }
        /*      } else if ( cache.readObject() instanceof Collection ) {
        String collectionName ;
        Collection<Object> objects = (Collection<Object>) cache.readObject();
        if ( objects == null ) {
          System.err.println("Collection null, nothing to print");
          return;
        }
        if ( objects.size() == 0 ) {
          System.err.println("Collection empty, nothing to print");
          return;
        }
        collectionName = objects.getClass().getSimpleName();
        
        System.out.println( "Cached objects " + collectionName + "<>:" );
        for (Object o : objects) {
          System.out.println(" * " + o + "    <" + o.getClass().getName()+ ">");
          } */
            } else {
                System.out.println();
                System.out.println("Cached Object: " + cache.readObject().getClass().getName());
                System.out.println(cache.readObject());
            }

        }
    }

    /**
     * Retrieves and returns the stored object from
     * file.
     *
     * @return cached object
     */
    public T object() {
        return cache.readObject();
    }

    /**
     * Program to read and print Serialized objects
     * <p>
     * Starting the program with the command line arguments "--test" is
     * a way for you to test if the ObjectCache classes can be
     * found. This is useful for softwares such as ADHD
     * (https://github.com/progund/adhd)
     *
     * @param args Either a file or "--test"
     */
    public static void main(String args[]) {
        if (args.length == 0) {
            System.err.println("Missing argument (filename)");
            System.exit(1);
        }

        if (args[0].equals("--test")) {
            System.exit(0);
        }

        ObjectCacheReader<Object> ocr = new ObjectCacheReader<>(args[0]);
        ocr.printObject();
    }

}
