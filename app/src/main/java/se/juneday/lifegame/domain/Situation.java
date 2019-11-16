package se.juneday.lifegame.domain;

import java.util.List;
import java.util.ArrayList;

public class Situation {

    private String gameTitle;
    private String gameSubTitle;
    private String title;
  private String description;
  private String question;
  private List<Suggestion> suggestions;
    private List<ThingAction> actions ;
    private List<ThingAction> things ;
    private String explanation;
    private String gameId;

    
    public static Situation endSituation = new Situation(null, null, null, "End of game", "Your life is complete, you've made it.", null, null, null, null, null);

    public Situation(String gameTitle, String gameSubTitle, String gameId, String title, String description, String question, List<Suggestion> suggestions, List<ThingAction> actions, List<ThingAction> things, String explanation) {
        this.gameTitle = gameTitle;
        this.gameId = gameId;
        this.gameSubTitle = gameSubTitle;
        this.title = title;
        this.description = description;
        this.question = question;
        this.suggestions = suggestions;
        this.actions = actions;
        if (actions==null) {
            this.actions = new ArrayList<>();
        }
        this.things = things;
        if (things==null) {
            this.things= new ArrayList<>();
        }
        this.explanation = explanation;
    }

    public String gameId() {
        return gameId;
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
                ", suggestions=" + suggestions+
                '}';
    }

}
