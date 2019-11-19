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

package se.juneday.lifegame.domain;

import se.juneday.lifegame.util.Log;

import java.util.function.Predicate;

public class Exit {

    private static final String LOG_TAG = Exit.class.getSimpleName();
    private Predicate<Game> predicate;
    private String exitSituation;
    private String explanation;

    public Exit(Predicate<Game> predicate, String exitSituation) {
        this.predicate = predicate;
        this.exitSituation = exitSituation;
    }

    public Exit(Predicate<Game> predicate, String exitSituation, String explanation) {
        this.predicate = predicate;
        this.exitSituation = exitSituation;
        this.explanation = explanation;
    }

    public String exit() {
        return exitSituation;
    }

    public String explanation() {
        return explanation;
    }

    public Boolean isTrue(Game game) {
        Log.v(LOG_TAG, "isTrue(): " + predicate + "  ===> " + predicate.test(game) + "   exit situation" + exitSituation);
        return predicate.test(game);
    }


    @Override
    public String toString() {
        return "Exit{" +
                "predicate=" + predicate +
                ", exitSituation='" + exitSituation + '\'' +
                '}';
    }

}
