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

import java.lang.reflect.AccessibleObject;

public class ThingAction {

    /*    public enum Action {
            TAKE,
            DROP
        } ;
    */
    //  private Action action;
    private String thing;
/*
    public ThingAction(String actionString, String thing) {
        try {
            this.action = Action.valueOf(actionString.toUpperCase());
        } catch (IllegalArgumentException e) {
            // TODO: thrown exception
        }
        this.thing = thing;
    }

    public ThingAction(Action action, String thing) {
        this.action = action;
        this.thing = thing;
    }
*/

    public ThingAction(String thing) {
        this.thing = thing;
    }

/*    public Action action() {
        return action;
    }
*/

    public String thing() {
        return thing;
    }

    @Override
    public String toString() {
        return thing;
    }

}
