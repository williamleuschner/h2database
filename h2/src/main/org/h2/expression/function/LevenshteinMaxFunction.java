
package org.h2.expression.function;

import org.h2.engine.SessionLocal;
import org.h2.expression.Expression;
import org.h2.value.*;


public final class LevenshteinMaxFunction extends FunctionN {

    public LevenshteinMaxFunction(Expression... args) {
        super(args);
    }

    // TODO: Replace with function from Ekta and William
    // Levenstein distance function in O(N) time and space
    // found at https://stackoverflow.com/questions/13564464/problems-with-levenshtein-algorithm-in-java
    // Author: stemm (https://stackoverflow.com/users/653511/stemm)
    private static int dist( char[] s1, char[] s2 ) {

        // memoize only previous line of distance matrix
        int[] prev = new int[ s2.length + 1 ];
        for( int j = 0; j < s2.length + 1; j++ ) {
            prev[ j ] = j;
        }
        for( int i = 1; i < s1.length + 1; i++ ) {
            // calculate current line of distance matrix
            int[] curr = new int[ s2.length + 1 ];
            curr[0] = i;
            for( int j = 1; j < s2.length + 1; j++ ) {
                int d1 = prev[ j ] + 1;
                int d2 = curr[ j - 1 ] + 1;
                int d3 = prev[ j - 1 ];
                if ( s1[ i - 1 ] != s2[ j - 1 ] ) {
                    d3 += 1;
                }
                curr[ j ] = Math.min( Math.min( d1, d2 ), d3 );
            }
            // define current line of distance matrix as previous
            prev = curr;
        }
        return prev[ s2.length ];
    }

    @Override
    public Value getValue(SessionLocal session) {
        Value max_distance = args[2].getValue(session);
        Value v1 = args[0].getValue(session);
        if (v1 != ValueNull.INSTANCE) {
            v1 = v1.convertTo(TypeInfo.TYPE_VARCHAR, session);
        }
        Value v2 = args[1].getValue(session);
        if (v2 != ValueNull.INSTANCE) {
            v2 = v2.convertTo(TypeInfo.TYPE_VARCHAR, session);
        }
        /*
        if(dist(v1.getString().toCharArray(), v2.getString().toCharArray()) > max_distance.getInt()){
            return ValueBoolean.FALSE;
        }
        else{
            return ValueBoolean.TRUE;
        }
        */

        // Temporarily checking if first argument is not Longer than max distance
        // rather than actual distance
        if (v1.charLength() > max_distance.getInt()){
            return ValueBoolean.FALSE;
        }
        else{
            return ValueBoolean.TRUE;
        }
    }

    @Override
    public Expression optimize(SessionLocal session) {
        type = TypeInfo.TYPE_BOOLEAN;
        return this;
    }

    @Override
    public String getName() {
        return "LEVENSHTEIN_MAX";
    }
}
