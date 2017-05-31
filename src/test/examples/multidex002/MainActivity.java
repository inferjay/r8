// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package multidex002;

import multidexfakeframeworks.Activity;

public class MainActivity extends Activity {

    private static final String TAG = "MultidexLegacyTestApp";
    private int instanceFieldNotInited;
    private int instanceFieldInited =
            new multidex002.manymethods.Big043().get43();
    private static int staticField =
            new multidex002.manymethods.Big044().get44();

    public MainActivity() {
        instanceFieldNotInited = new multidex002.manymethods.Big042().get42();
    }

    protected void onCreate() {
        int value = getValue();

        System.out.println("Here's the count " + value);
    }

    public int getValue() {
        int value = new multidex002.manymethods.Big001().get1()
                + new multidex002.manymethods.Big002().get2()
                + new multidex002.manymethods.Big003().get3()
                + new multidex002.manymethods.Big004().get4()
                + new multidex002.manymethods.Big005().get5()
                + new multidex002.manymethods.Big006().get6()
                + new multidex002.manymethods.Big007().get7()
                + new multidex002.manymethods.Big008().get8()
                + new multidex002.manymethods.Big009().get9()
                + new multidex002.manymethods.Big010().get10()
                + new multidex002.manymethods.Big011().get11()
                + new multidex002.manymethods.Big012().get12()
                + new multidex002.manymethods.Big013().get13()
                + new multidex002.manymethods.Big014().get14()
                + new multidex002.manymethods.Big015().get15()
                + new multidex002.manymethods.Big016().get16()
                + new multidex002.manymethods.Big017().get17()
                + new multidex002.manymethods.Big018().get18()
                + new multidex002.manymethods.Big019().get19()
                + new multidex002.manymethods.Big020().get20()
                + new multidex002.manymethods.Big021().get21()
                + new multidex002.manymethods.Big022().get22()
                + new multidex002.manymethods.Big023().get23()
                + new multidex002.manymethods.Big024().get24()
                + new multidex002.manymethods.Big025().get25()
                + new multidex002.manymethods.Big026().get26()
                + new multidex002.manymethods.Big027().get27()
                + new multidex002.manymethods.Big028().get28()
                + new multidex002.manymethods.Big029().get29()
                + new multidex002.manymethods.Big030().get30()
                + new multidex002.manymethods.Big031().get31()
                + new multidex002.manymethods.Big032().get32()
                + new multidex002.manymethods.Big033().get33()
                + new multidex002.manymethods.Big034().get34()
                + new multidex002.manymethods.Big035().get35()
                + new multidex002.manymethods.Big036().get36()
                + new multidex002.manymethods.Big037().get37()
                + new multidex002.manymethods.Big038().get38()
                + new multidex002.manymethods.Big039().get39()
                + new multidex002.manymethods.Big040().get40()
                + new multidex002.manymethods.Big041().get41()
                + instanceFieldNotInited + instanceFieldInited + staticField
                + IntermediateClass.get() + Referenced.get(instanceFieldNotInited);
        return value;
    }

    public int getAnnotation2Value() {
        return ((AnnotationWithEnum2) TestApplication.annotation2).value().get();
    }

}
