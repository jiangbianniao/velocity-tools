/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.velocity.tools.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Simple tool to provide easy in-template instantiation of
 * {@link Alternator}s from varying "list" types.
 *
 * <p><b>Example Use:</b>
 * <pre>
 * toolbox.xml...
 * &lt;tool&gt;
 *   &lt;key&gt;alternator&lt;/key&gt;
 *   &lt;scope&gt;application&lt;/scope&gt;
 *   &lt;class&gt;org.apache.velocity.tools.generic.AlternatorTool&lt;/class&gt;
 * &lt;/tool&gt;
 *
 * template...
 * #set( $color = $alternator.make('red', 'blue') )
 * #set( $style = $alternator.make(true, ['hip','fly','groovy']) )
 * #foreach( $i in [1..5] )
 *  $i is $color.next and $style
 * #end
 *
 * output...
 *  1 is red and hip
 *  2 is blue and fly 
 *  3 is red and groovy
 *  4 is blue and hip 
 *  5 is red and fly
 * </pre></p>
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/04 03:32:22 $
 */
public class AlternatorTool
{

    public AlternatorTool() {}

    /**
     * Make a non-automatic {@link Alternator} from a List.
     */
    public Alternator make(List list)
    {
        return make(false, list);
    }

    /**
     * Make an {@link Alternator} from a List.
     */
    public Alternator make(boolean auto, List list)
    {
        if (list == null)
        {
            return null;
        }
        return new Alternator(auto, list);
    }

    /**
     * Make a non-automatic {@link Alternator} from the values 
     * in the specified collection.
     */
    public Alternator make(Collection collection)
    {
        return make(false, collection);
    }

    /**
     * Make an {@link Alternator} from the values 
     * in the specified collection.
     */
    public Alternator make(boolean auto, Collection collection)
    {
        if (collection == null)
        {
            return null;
        }
        return make(auto, new ArrayList(collection));
    }

    /**
     * Make a non-automatic {@link Alternator} from an object array.
     */
    public Alternator make(Object[] array)
    {
        return make(false, array);
    }

    /**
     * Make an {@link Alternator} from an object array.
     */
    public Alternator make(boolean auto, Object[] array)
    {
        if (array == null)
        {
            return null;
        }
        return make(auto, Arrays.asList(array));
    }

    /**
     * Make a non-automatic {@link Alternator} from a list containing the two
     * specified objects.
     */
    public Alternator make(Object o1, Object o2)
    {
        return make(false, o1, o2);
    }

    /**
     * Make an {@link Alternator} from a list containing the two
     * specified objects.
     */
    public Alternator make(boolean auto, Object o1, Object o2)
    {
        List list = new ArrayList();
        list.add(o1);
        list.add(o2);
        return make(auto, list);
    }

}