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

import java.util.List;
import java.util.ArrayList;

public class Situation {

    private int situationCount;
    private int score;
    private String gameTitle;
    private String gameSubTitle;
    private String title;
    private String description;
    private String question;
    private List<Suggestion> suggestions;
    private List<ThingAction> actions;
    private List<ThingAction> things;
    private String explanation;
    private String gameId;
    private long millisLeft;


    public static Situation endSituation = new Situation(null, null, null, "End of game", "Your life is complete, you've made it.", null, null, null, null, null, 0, 0, 0);

    public Situation(String gameTitle, String gameSubTitle, String gameId, String title, String description, String question, List<Suggestion> suggestions, List<ThingAction> actions, List<ThingAction> things, String explanation,
                     long millisLeft, int score, int situationCount) {
        this.gameTitle = gameTitle;
        this.gameId = gameId;
        this.gameSubTitle = gameSubTitle;
        this.title = title;
        this.description = description;
        this.question = question;
        this.suggestions = suggestions;
        this.actions = actions;
        if (actions == null) {
            this.actions = new ArrayList<>();
        }
        this.things = things;
        if (things == null) {
            this.things = new ArrayList<>();
        }
        this.explanation = explanation;
        this.millisLeft = millisLeft;
        this.score=score;
        this.situationCount=situationCount;
    }

    public int score() {
        return score;
    }

    public int situationCount() {
        return situationCount;
    }

    public String gameId() {
        return gameId;
    }

    public long millisLeft() {
        return millisLeft;
    }

    public String gameSubTitle() {
        return gameSubTitle;
    }

    public String gameTitle() {
        return gameTitle;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String question() {
        return question;
    }

    public String explanation() {
        return explanation;
    }

    public List<Suggestion> suggestions() {
        return suggestions;
    }

    public List<ThingAction> actions() {
        return actions;
    }

    public List<ThingAction> things() {
        return things;
    }

    public void removeActionThing(ThingAction t) {
        actions.remove(t);
    }

    public void addActionThing(ThingAction t) {
        actions.add(t);
    }

    @Override
    public String toString() {
        return "Situation{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", question='" + question + '\'' +
                ", actions='" + actions + '\'' +
                ", suggestions=" + suggestions +
                '}';
    }

}
