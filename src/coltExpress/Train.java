package coltExpress;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import static coltExpress.Action.NB_ACTIONS;
import static coltExpress.Controleur.round;
import static coltExpress.Train.*;


/**
 * Interface des objets observateurs.
 */

interface Observer {
    void update();
}

/**
 * Classe des objets pouvant être observés.
 */
abstract class Observable {
    private final ArrayList<Observer> observers;
    public Observable() {
        this.observers = new ArrayList<>();
    }
    public void addObserver(Observer o) {
        observers.add(o);
    }
    public void notifyObservers() {
        for(Observer o : observers) {
            o.update();
        }
    }
}



/**
 * La classe principale de notre jeu
 */
public class Train {
    /** Fix the maximum number of rounds in a game. */
    public static final int NB_MAX_ROUNDS=3;
    /** Fix the maximum number of wagons in a train. */
    public static final int NB_WAGONS = 5;
    /** Fix the maximum number of players in a game. */
    public static final int NB_JOUEURS = 3;
    /** Elements of train are represented by a 2D array list called "wagons".
     * There are two rows in "wagons", one for the wagons (the insides) of the train, the other for the roofs of each wagon.*/
    public static ArrayList<ArrayList<TrainElts>> wagons = new ArrayList<>();
    /** Players inside the train are represented by an array list since there are multiple of them. */
    public static ArrayList<Bandit> players = new ArrayList<>();
    /** There's only 1 marshall. */
    public static Marshall marshall ;

    /**
     * L'amorçage est fait en créant le modèle et la vue, par un simple appel
     * à chaque constructeur.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            /** Voici le contenu qui nous intéresse. */
            CModele modele = new CModele();
            CVue vue = new CVue(modele);
            vue.firstPage();
            vue.pickACharac(0);
            Controleur controleur = new Controleur(modele);
        });

    }

    /**
     * Constructor of the Train class, where the game is initialized
     */
    public Train() {
        ArrayList<TrainElts> insideWagRows = new ArrayList<>();
        ArrayList<TrainElts> roofWagRows = new ArrayList<>();

        /** Locomotive's wagon */
        TrainElts locomotiveI = new TrainElts(false, 0);
        /** Locomotive's roof */
        TrainElts locomotiveR = new TrainElts(true, 0);

        insideWagRows.add(locomotiveI);

        /** Magot is only placed in the first wagon */
        insideWagRows.get(0).placeButin(new Butin("magot"));

        roofWagRows.add(locomotiveR);

        for (int i = 1; i < NB_WAGONS; i++) {
            TrainElts inside = new TrainElts(false, i);
            TrainElts roof = new TrainElts(true, i);
            /** The wagons are index 0 */
            insideWagRows.add(inside);
            /** The roofs are index 1 */
            roofWagRows.add(roof);
        }
        wagons.add(insideWagRows);
        wagons.add(roofWagRows);

        /** Initializing each player to start at the roofs of the train */
        for (int i =0; i<NB_JOUEURS;i++){
            Bandit b = new Bandit(i, getInitialPositionForBandit(i+1));
            players.add(b);
        }
        /** Marshall initialized to the locomotive's wagon */
        marshall = new Marshall(wagons.get(0).get(0));

    }

    /**
     * Retrieves the initial position for a bandit based on the provided index.
     * @param i The index of the wagon number.
     * @return The initial position (TrainElts) for the bandit.
     */
    public TrainElts getInitialPositionForBandit(int i) {
        return wagons.get(1).get(i);
    }


    /**
     * Removes all bandits in the train. Used only for the tests.
     */
    public static void removeAllBanditsForTests(){
        for (int i = 0; i< NB_WAGONS; i++) {
           wagons.get(1).get(i).getPlayers().clear();
        }
        players.clear();
    }



}

/**
 * An enum for Direction
 */
enum Direction { AVANT, ARRIERE, HAUT, BAS;
    private static final Random PRNG = new Random();

    /**
     * Retrieves a random direction between ARRIERE and AVANT
     * @return The random direction for the Marshall's move()
     */
    public static Direction randomDirection()  {
        Direction[] directions = {Direction.ARRIERE, Direction.AVANT};
        return directions[PRNG.nextInt(directions.length)];
    }
}

/**
 * Définition d'une classe pour les elements de Train.
 * For simplicity, we'll proceed to call everything "wagon", even the roof part of each wagon.
 */
class TrainElts {

    /** Whether a certain element of our Train is a roof or not is represented by a boolean. */
    private boolean isRoof ;

    /** The wagon number of the wagons of our Train. */
    final private int wagonNum;
    /** An array list of loots that each wagon has. */
    private ArrayList<Butin>butins; //1 to 4 butins in a wagon
    /** Players inside each wagon are represented by an array list since there could be multiple of them. */
    private ArrayList<Bandit>players;
    /** There could be 1 marshall in a wagon. */
    private Marshall marshall;
    /**
     * Constructs a new TrainElts with the given player ID.
     * @param r Whether the wagon is roof or not.
     * @param num The unique number of a wagon.
     */
    public TrainElts(boolean r, int num){
        isRoof  = r;
        wagonNum = num;
        butins = new ArrayList<>();
        Random rand = new Random();
        /**
         * Initializing the loots in each wagon except for locomotive since locomotive only has magot.
         * There could be 1 to 4 loots in a wagon (initialized inside the train wagons).
         */
        int numButins = rand.nextInt(4) + 1; // Generate between 1 and 4
        String[] butinTypes = {"bourses", "bijoux"};
        for (int i = 0; i < numButins; i++) {
            if( this.wagonNum!=0 && !isRoof){
                int index = rand.nextInt(butinTypes.length);
                String type = butinTypes[index];
                Butin b = new Butin(type);
                butins.add(b);}
        }
        players = new ArrayList<>();
        marshall = null;
    }

    /**
     * Check whether the wagon is a roof. Only used in tests.
     */
    public boolean isRoof() {
        return isRoof;
    }

    /**
     * Getter : returns the Marshall in the wagon.
     */
    public Marshall getMarshall() {
        return marshall;
    }

    /**
     * toString of TrainElts.
     */
    public String toString() {
        return "TrainElts{" +
                "isRoof=" + isRoof +
                ", wagonNum=" + wagonNum +
                '}';
    }


    /**
     * Retrieves the wagon in the specified direction.
     * @param d The direction specified.
     * @return The wagon in the specified direction.
     */
    public TrainElts neighbor(Direction d) {
        int ind = wagonNum;
        boolean roof = isRoof;
        switch (d) {
            case ARRIERE:
                if (wagonNum < Train.NB_WAGONS - 1) {
                    ind = wagonNum+1;
                }
                break;
            case AVANT:
                if (wagonNum > 0) {
                    ind = wagonNum-1;
                }
                break;
            case HAUT:
                if (!isRoof) roof = true;
                else return null; //player can't go to the roof if it's alr on roof
                break;
            case BAS:
                if (isRoof) roof = false;
                else return null;
                break;
            default:
                return null;
        }
        int indRoof = roof ? 1 : 0;
        return Train.wagons.get(indRoof).get(ind);
    }

    /**
     * Retrieves another Bandit than the specified Bandit in the same wagon.
     * @param p The Bandit specified.
     * @return The Bandit that is not Bandit p who is also in their wagon.
     */
    public Bandit anotherBanditThan(Bandit p) {
        for(Bandit b : players) {
            if (p.getPosition()==b.getPosition()){
                if(!b.equals(p)) return b;}
        }
        return null;
    }

    /**
     * Getter : returns the wagon number.
     */
    public int getWagonNum() {
        return wagonNum;
    }

    /**
     * Getter : returns the array list of loots in the wagon
     */
    public ArrayList<Butin> getButins() {
        return butins;
    }


    /**
     * Places back all the loots dropped by players to the wagon
     */
    public void placeButin(Butin loot){ //add dropped loots back to wagon
        butins.add(loot);
    }
    /**
     * Adds (places) a bandit to the wagon.
     */
    public void addBandit(Bandit p) {
        players.add(p);
    }
    /**
     * Getter : returns an array list of all the bandits that are in the wagon
     */
    public ArrayList<Bandit> getPlayers() {
        return players;
    }
    /**
     * Places the marshall in the wagon.
     */
    public void addMarshall(Marshall marshall) {
        this.marshall = marshall;
    }
    /**
     * Removes the specified bandit from the wagon.
     * @param bandit The bandit to be removed.
     */
    public void removeBandit(Bandit bandit) {
        players.remove(bandit);
    }
    /**
     * Removes the marshall from the wagon.
     */
    public void removeMarshall() {
        this.marshall = null;
    }


}

/**
 * Represents a specific type of loot in the game.
 */
class Butin {
    /** The different types of loot represented by a String. */
    private String type;
    /** The different value of each loot. */
    private int amount;
    /**
     * Constructs a new Butin with the given type.
     * @param type The type of loot.
     */
    public Butin(String type){
        this.type = type;
        Random rand = new Random();
        int min,max;

        if (type == "bourses") {
            min = 0;
            max = 500;
            amount = rand.nextInt(max - min + 1) + min;
        }
        if (type == "bijoux") {
            amount = 500;
        }
        if (type == "magot") {
            amount = 1000;
        }
    }
    /**
     * toString of Butin.
     */
    public String toString() {
        return "Butin{" +
                "type='" + type + '\'' +
                '}';
    }
    /**
     * Getter : returns the type of loot.
     */
    public String getType() {
        if (this!=null) return type;
        else return "null";
    }

    /**
     * Getter : returns the value of loot.
     */
    public int getAmount() {
        return amount;
    }
}


/**
 * Represents a Bandit character in the game.
 */
class Bandit extends Observable { //Player
    /** The maximum number of bullets each Bandit can have. */
    public static int NB_BALLES = 6;
    /** The number of bullets each Bandit has left in their possession. */
    private int nbBalles;
    /** The unique ID of Bandit. */
    private int playerID;
    /** The score obtained by Bandit after collecting loots. */
    int score;
    /** The position of bandit represented by TrainElts. */
    private TrainElts position;
    /** An array list of loots each Bandit has collected. */
    private ArrayList<Butin>loots;

    /**
     * Constructs a new Bandit with the given player ID.
     * @param id The unique player ID.
     */
    public Bandit (int id){
        playerID=id;
        this.nbBalles=NB_BALLES;
        this.score=0;
        this.loots = new ArrayList<>();

    }
    /**
     * Constructs a new Bandit with the given player ID and the position in the Train.
     * @param id The unique player ID.
     * @param p The position of bandit represented by TrainElts.
     */
    public Bandit (int id, TrainElts p){
        playerID=id;
        this.nbBalles=NB_BALLES;
        this.score=0;
        this.loots = new ArrayList<>();
        position = p;
        p.addBandit(this);
    }
    /**
     * Decreases the score when bandit drops a loot.
     */
    public void decrScore(int val){
        score-=val;
    }
    /** Drops a random loot when bandit is shot by another Bandit or by Marshall.
     * @return The random loot that was dropped.
     **/
    public Butin dropRandomLoot() {
        if (!loots.isEmpty()) {
            Random rand = new Random();
            int rdm = rand.nextInt(loots.size());
            Butin removed = loots.get(rdm);
            decrScore(removed.getAmount());
            return loots.remove(rdm);
        } else {
            return null;
        }

    }
    /**
     * Getter : returns the number of bullets the Bandit possess.
     */
    public int getNbBalles(){
        return this.nbBalles;
    }

    /**
     * Decreases the number of bullets as Bandit shoots.
     */
    public void decreaseBalls(){
        if(nbBalles>0) this.nbBalles--;
    }

    /**
     * toString of Bandit.
     */
    public String toString() {
        return "Bandit{" +
                "playerID=" + playerID +
                ", loots=" + loots +
                ", position=" + position +
                '}';
    }

    /**
     * Performs the specified action of Bandit.
     * @param action The action that we want the bandit to do.
     */
    public void performBanditAction(Action action) {
        //modele.getMarshall().move();
        action.execute();
        notifyObservers();
    }
    /**
     * Getter : returns the unique ID of Bandit.
     */
    public int getPlayerID() {
        return playerID;
    }
    /**
     * Getter : returns the number of bullets the Bandit possess.
     */
    public TrainElts getPosition() {
        return position;
    }
    /**
     * Sets the position of the Bandit as the wagon specified.
     * @param wagon The new position of Bandit.
     */
    public void setWagon(TrainElts wagon) {
        if (this.position != null) {
            this.position.removeBandit(this); // make sure to remove bandit from wagon first
        }
        this.position = wagon;
        wagon.addBandit(this);

    }
    /**
     * Add the specified loot the Bandit's inventory (array list) and manages the score at the same time.
     * @param loot The loot to be added.
     */
    public void addButin(Butin loot){
        loots.add(loot);
        if(loot!=null)score+=loot.getAmount();
    }
    /**
     * Getter : returns an array list of loots that the Bandit possess.
     */
    public ArrayList<Butin> getLoots() {
        return loots;
    }
    /**
     * Returns the number of the specific loot that the Bandit possess.
     * @param type The type of loot.
     * @return The number of the specific loot.
     */
    public int getNbLoot(String type) {
        int nb= 0;
        for (Butin b: loots){
            if(b!=null){
                if ( b.getType() == type ) nb++;
            }}

        return nb;
    }
    /**
     * Getter : returns the score of the Bandit.
     */
    public int getScore() {
        return score;
    }
}
/**
 * Represents the Marshall in the game.
 */
class Marshall  {
    /** Fix the nervousness of Marshall. */
    public static final double NERVOSITE_MARSHALL = 0.3;
    /** Position of the Marshall, represented by TrainElts, which is always inside the train. */
    private TrainElts position; private final boolean onRoof = false;
    /**
     * toString of Marshall.
     */
    public String toString() {
        return "Marshall{" +
                "position=" + position +
                '}';
    }
    /**
     * Getter : returns the position of the Marshall, represented by TrainElts
     */
    public TrainElts getPosition() {
        return position;
    }
    /**
     * Getter : returns the True of False value of whether Marshall is on the roof. Only used in tests.
     */
    public Boolean isNotOnRoof(){
        return !onRoof;
    }
    /**
     * Constructs a new Marshall.
     */
    public Marshall(){
        /** Marshall starts from locomotive. */
        position = Train.wagons.get(0).get(0);
        this.setWagonMarshall(position);
    }
    /**
     * Constructs a new Marshall with the given position in the Train.
     * @param pos The position of Marshall represented by TrainElts.
     */
    public Marshall (TrainElts pos){
        position=pos;this.setWagonMarshall(position);
    }
    /**
     * Set the new position of Marshall.
     * @param wagon The new position of Marshall represented by TrainElts.
     */
    public void setWagonMarshall(TrainElts wagon) {
        if (this.position != null) {
            this.position.removeMarshall(); // make sure to remove marshall from wagon first
        }
        this.position = wagon;
        wagon.addMarshall(this);
    }
    /**
     * Moves the marshall in a random position (AVANT and ARRIERE), or not (depending on marshall's nervousness),
     * and manages the action of Bandit when Marshall and Bandit are in the same wagon.
     */
    public void move(){
        Random rand = new Random();
        double r = rand.nextDouble();

        if (r <= NERVOSITE_MARSHALL) {
            TrainElts newPos;
            if (this.getPosition() == Train.wagons.get(0).get(0)){  newPos = position.neighbor(Direction.ARRIERE);}
            else { newPos = position.neighbor(Direction.randomDirection());}
            if (newPos != null) {
                this.setWagonMarshall(newPos);
                checkBandits(position.getPlayers());
                System.out.println("Marshall moved to wagon " + position.getWagonNum());
            }
        }else checkBandits(position.getPlayers());
    }

    /**
     * Manages the action of Bandit when meeting the Marshall :
     * Bandit drops a random loot to the wagon they are in and climbs to the roof to escape from Marshall.
     */
    public void checkBandits(ArrayList<Bandit> bandits) {
        if (bandits.isEmpty()) return;
        else{
            for (int i = 0; i< bandits.size();i++) {
                if(bandits.get(i)!=null){
                    dropLoot(bandits.get(i));
                    moveBanditToRoof(bandits.get(i));
                }
            }}
    }
    /**
     * Bandit drops a random loot because of Marshall.
     * @param p The specified Bandit.
     */
    private void dropLoot(Bandit p) {
        if (!p.getLoots().isEmpty()) {
            Butin droppedLoot = p.dropRandomLoot();
            position.placeButin(droppedLoot);
            System.out.println("Player"+p.getPlayerID() + " dropped " + droppedLoot.getType() +" because of Marshall.");

        }
    }
    /**
     * Bandit climbs to the roof to escape from Marshall.
     * @param bandit The specified Bandit.
     */
    public void moveBanditToRoof(Bandit bandit) {
        int i = bandit.getPosition().getWagonNum();
        bandit.setWagon(Train.wagons.get(1).get(i));
        System.out.println("Player"+bandit.getPlayerID() + " moved to the roof because of Marshall!");

    }

}

/**
 * Abstract Class : Represents an Action of Bandit in the game.
 */
abstract class Action{
    /** Fix the maximum number of actions a Bandit can do in a round. */
    public static final int NB_ACTIONS = 4;
    /** The texts of what is happening to facilitate users to understand where they are in the game. */
    public  String textAction;
    /** The player doing the action. */
    Bandit player;
    /**
     * Executing an action.
     */
    abstract void execute();
    /**
     * toString of Action.
     */
    @Override
    public String toString() {
        return "Action{" +
                "player=" + player +
                '}';
    }
    /**
     * Getter : returns the text of the Action executed.
     */
    public String getTextAction() {
        return textAction;
    }
    /**
     * Setter : sets the text of the Action executed.
     */
    public void setTextAction(String textAction) {
        this.textAction = textAction;
    }
}

/**
 * Subclass of Action : The Action of Robbing a loot.
 */
class Braquer extends Action{
    /**
     * Constructs a new Braquer action with the given bandit.
     * @param player The player doing the action of robbing.
     */
    public Braquer(Bandit player) {
        this.player = player;
    }
    /**
     * Executing the action of Robbing a random loot in a wagon that has loot(s).
     */
    public void execute() {
        TrainElts currentPos = player.getPosition();
        ArrayList<Butin> wagonsButins = currentPos.getButins();

        if (!wagonsButins.isEmpty()) {
            Random rand = new Random();
            int rdm = rand.nextInt(wagonsButins.size());
            Butin looted = wagonsButins.remove(rdm);
            player.addButin(looted);
            if (looted != null) {
                System.out.println("Player" + player.getPlayerID() + " robbed " + looted.getType());
                String actionText = "Player" + player.getPlayerID() + " robbed " + looted.getType();
                setTextAction(actionText);
            } else {
                System.out.println("Player" + player.getPlayerID() + " tried to rob, but loot is null.");
                String actionText = "Player" + player.getPlayerID() + " tried to rob, but loot is null.";
                setTextAction(actionText);
            }
        } else {
            System.out.println("Player" + player.getPlayerID() + " failed to rob because no loot in sight.");
            String actionText = "Player" + player.getPlayerID() + " failed to rob because no loot in sight.";
            setTextAction(actionText);
        }
    }
}
/**
 * Subclass of Action : The Action of Shooting (pulling the trigger) at another player.
 */
class Tirer extends Action{ //pull the trigger
    /** The direction as to where the player is shooting at. */
    Direction dir;
    /**
     * Constructs a new Tirer action with the given bandit and direction.
     * @param player The player doing the action of shooting.
     * @param d The direction as to where the player is shooting at.
     */
    public Tirer(Bandit player, Direction d){
        this.player = player;
        dir =d;
    }
    /**
     * Executing the action of Shooting.
     * A shot is made in one of four directions, which are interpreted in the same way as for movements:
     * forward or backward on the same floor, or up or down in the same wagon.
     * A shot upwards (resp. downwards) when a bandit is on the roof (resp. inside)
     * targets the position occupied by the bandit.
     * A shot always uses up a bullet.
     * A bandit who is shot drops a random loot.
     */
    public void execute() {
        player.decreaseBalls();
        System.out.print("Shooting... ");
        if (player.getNbBalles() > 0) {
            if (player != null && player.getPosition() != null) {
                if ( (dir == Direction.BAS && player.getPosition().neighbor(Direction.BAS) == null) ||
                        (dir == Direction.HAUT && player.getPosition().neighbor(Direction.HAUT) == null) ) {
                    Bandit b = player.getPosition().anotherBanditThan(player);
                    if (b == null) {
                        System.out.println("Player" + player.getPlayerID() + " failed to shoot anybody. No other bandit is in player's wagon.");
                        String actionText = "Player" + player.getPlayerID() + " failed to shoot anybody. No other bandit is in player's wagon.";
                        setTextAction(actionText);
                        return;
                    }
                    if (b != null) {
                        Butin droppedLoot = b.dropRandomLoot();
                        System.out.println("Player" + player.getPlayerID() + " has shot Player" + b.getPlayerID() +" in the same wagon.");
                        String actionText ="Player" + player.getPlayerID() + " has shot Player" + b.getPlayerID() +" in the same wagon.";
                        setTextAction(actionText);
                        player.getPosition().placeButin(droppedLoot);

                    }
                }else {
                    TrainElts currentWagon = player.getPosition();
                    TrainElts targetWagon = currentWagon.neighbor(dir);
                    ArrayList<Bandit> bList = targetWagon.getPlayers();

                    Random rand = new Random();
                    if (!bList.isEmpty()) {
                        Bandit b = bList.get(rand.nextInt(bList.size()));


                        if (targetWagon != null) {

                            Butin droppedLoot = b.dropRandomLoot();
                            b.getPosition().placeButin(droppedLoot);
                            System.out.println("Player" + player.getPlayerID() + " has shot Player" + b.getPlayerID() + " in direction " + dir);
                            String actionText ="Player" + player.getPlayerID() + " has shot Player" + b.getPlayerID() + " in direction " + dir;
                            setTextAction(actionText);
                            //player.decreaseBalls();

                        } else {
                            System.out.println("Player" + player.getPlayerID() + " cannot shoot in direction " + dir + " because no wagon found.");
                            String actionText = "Player" + player.getPlayerID() + " cannot shoot in direction " + dir + " because no wagon found.";
                            setTextAction(actionText);
                        }
                    } else {
                        System.out.println("Player" + player.getPlayerID() + " failed to shoot because no players in targeted wagon.");
                        String actionText = "Player" + player.getPlayerID() + " failed to shoot because no players in targeted wagon.";
                        setTextAction(actionText);
                    }


                }

            } else {
                System.out.println("Cannot shoot because position is nowhere to be found.");
                String actionText = "Cannot shoot because position is nowhere to be found.";
                setTextAction(actionText);
            }

        } else {
            System.out.println("Player" + player.getPlayerID() + " is out of bullets.");
            String actionText = "Player" + player.getPlayerID() +" is out of bullets.";
            setTextAction(actionText);
        }

    }
}


/**
 * Subclass of Action : The Action of Moving to another wagon.
 */
class Deplacer extends Action{
    /** The direction as to where the player is moving to. */
    private Direction dir;
    /**
     * Constructs a new Deplacer action with the given bandit and direction.
     * @param player The player doing the action of moving.
     * @param d The direction as to where the player is moving to.
     */
    public Deplacer (Bandit player, Direction d){
        this.player = player;
        dir = d;
    }
    /**
     * Executing the action of Moving.
     * Moving backwards when you are in the last wagon or forwards when you are in the locomotive has no effect.
     * The same goes for moving towards the roof or towards the interior when you are already there.
     */
    public void execute(){
        if (player == null) {
            System.out.println("Error Deplacer: Player is not initialized!");
            return;
        }

        TrainElts currentPos = player.getPosition();
        if (currentPos == null) {
            System.out.println("Error Deplacer: Player's current position is not set!");
            return;
        }

        TrainElts newPos = currentPos.neighbor(this.dir);
        if (newPos != null) {
            player.setWagon(newPos);
            System.out.println("Player" + player.getPlayerID() + " moves " + dir + " to wagon " + newPos.getWagonNum() );
            String actionText = "Player" + player.getPlayerID() + " moves " + dir + " to wagon " + newPos.getWagonNum();
            setTextAction(actionText);
        } else {
            switch(dir) {
                case HAUT : {
                    System.out.println("Already on roof, can't go further");
                    String actionText = "Already on roof, can't go further";
                    setTextAction(actionText);
                    break;
                }
                case BAS : {
                    System.out.println("Already on bottom, can't go further");
                    String actionText = "Already on bottom, can't go further";
                    setTextAction(actionText);
                    break;
                }
                default: {
                    System.out.println("Deplacer: Invalid move --- no wagon there!");
                    String actionText = "Deplacer: Invalid move --- no wagon there!";
                    break;
                }
            }


        }
    }


}


/**
 * Classe pour notre contrôleur rudimentaire.
 *
 * Le contrôleur implémente l'interface [ActionListener] qui demande
 * uniquement de fournir une méthode [actionPerformed] indiquant la
 * réponse du contrôleur à la réception d'un événement.
 */
class Controleur implements ActionListener {
    /** The current player's ID. */
    static int currentPlayer;
    /** The current round number. */
    static int round;
    /**
     * On garde un pointeur vers le modèle, car le contrôleur doit
     * provoquer un appel de méthode du modèle.
     */
    CModele modele;

    /**
     * Getter : returns the ID of the current player.
     */
    public static int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Setter : sets the ID of the current player indicating their turn.
     * @param id The unique player ID.
     */
    public static void setCurrentPlayer(int id){
        currentPlayer=id;

    }
    /**
     * Constructs a new Controller, initializes the current player from ID 0, and current round from round 1.
     * @param modele The train model.
     */
    public Controleur(CModele modele) { this.modele = modele; currentPlayer=0; round=1; }
    /** Whether it is currently the Marshall's turn in the round. */
    boolean marshallTurn = true;
    /** The index of the current player. */
    int currentPlayerIndex=0;
    /** The index of the previous round to keep track. */
    private int previousRound = 0;
    /** The round counter. */
    static int roundCount=1;
    /** The maximum score to determine the winner of the game. */
    static int scoreMax = 0;
    /** The player ID of the winner. */
    static int winnerID=0;
    /**
     * Action effectuée à réception d'un événement
     */
    public void actionPerformed(ActionEvent e) {
        TrainElts marshallPositionBefore = modele.getMarshall().getPosition(); // Marshall's position before move
        TrainElts marshallPositionAfter = null; //initialiser
        modele.getMarshall().move();
        marshallPositionAfter = modele.getMarshall().getPosition(); // Marshall's position after move
        if (marshallPositionBefore == marshallPositionAfter) {
            System.out.println("Marshall did not move. Tough guy!");
            marshallTurn = false;
        }
        if (marshallTurn) {
            // Marshall's turn
            if (round > NB_ACTIONS) {

                System.out.println("End of all rounds");
                // Reset round counter if all rounds are completed
                round = 1;
                marshallTurn = false; // Ensure Marshall's turn is false when all rounds are completed
            }
        } else {
            // Player's turn
            if (round <= NB_ACTIONS) {
                if (currentPlayerIndex < modele.getPlayers().size()) {
                    setCurrentPlayer(currentPlayerIndex);
                    Bandit currentPlayer = modele.getPlayers().get(currentPlayerIndex);
                    Action currentAction = modele.getActions().get(currentPlayerIndex).get(round - 1);
                    currentPlayer.performBanditAction(currentAction);
                    currentPlayerIndex++; // Move to the next player for the next button press
                    // marshall only moves if position changed
                    if (marshallPositionBefore != marshallPositionAfter) {
                        marshallTurn = true; // Next turn will be Marshall's turn
                    }

                } else {
                    // Reset player index for the next round
                    currentPlayerIndex = 0;
                    setCurrentPlayer(currentPlayerIndex);
                    System.out.println("End of Round " + round);
                    round++; // Move to the next round
                    if (round != previousRound && round !=5) {
                        System.out.println("Starting Round " + round);
                        previousRound = round;
                    }
                }
            } else {
                System.out.println("End of all rounds");
                // Reset round counter if all rounds are completed
                round = 1;
                marshallTurn = false; // Ensure Marshall's turn is false when all rounds are completed
            }
        }

    }

    public static void setWinnerID(int winnerID) {
        Controleur.winnerID = winnerID;
    }
}


/**
 * Le modèle : le coeur de l'application.
 *
 * Le modèle étend la classe [Observable] : il va posséder un certain nombre
 * d'observateurs (ici, un : la partie de la vue responsable de l'affichage)
 * et devra les prévenir avec [notifyObservers] lors des modifications.
 */
class CModele extends Observable {
    Train train;
    /** An array list of bandits in the game. */
    private ArrayList<Bandit> players;
    /** A 2D array list of action done by bandits when they click the buttons.
     * Rows represent players
     * Columns represent round
     */
    private ArrayList<ArrayList<Action>> actions;
    /** The one and only Marshall in the game. */
    private Marshall marshall;
    /** Action counter to keep track. */
    int totalActionCount;
    /** Construction : we initialize everything -- Train, players, actions, and marshall */
    public CModele() {
        totalActionCount=0;
        train = new Train();
        players = train.players;
        actions= new ArrayList<ArrayList<Action>>();
        for (int i=0; i< players.size(); i++){
            actions.add(new ArrayList<>());
        }
        marshall = new Marshall();
    }
    /** Getter : returns the train of the game. */
    public Train getTrain() {
        return train;
    }
    /** Getter : returns the array list of the players in the game. */
    public ArrayList<Bandit> getPlayers() {
        return players;
    }
    /** Getter : returns the 2D array list of the actions done by players. */
    public ArrayList<ArrayList<Action>> getActions() {
        return actions;
    }
    /**
     * Adds an action and who it's done by to the 2D array list
     * @param id The unique player ID.
     * @param a The Action done.
     */
    public void addAction(Action a, int id){
        if(this.actions.get(id).size()<NB_ACTIONS)this.actions.get(id).add(a);
    }
    /** Getter : returns the Marshall of the game. */
    public Marshall getMarshall() {
        return marshall;
    }
}
/**
 * La vue : l'interface avec l'utilisateur.
 *
 * On définit une classe chapeau [CVue] qui crée la fenêtre principale de
 * l'application et contient :
 *  - An area where our train filled with the loots, Marshall, players are.
 *  - An area of buttons for players' actions and Go! executing those actions one after another.
 *  - An area where we can keep track of each players' advancements in the game.
 *  We also have :
 *  - A screen with a tutorial where we press the Play button to start the game.
 *  - A screen where we can pick our characters before playing.
 */
class CVue extends JPanel {
    /** Représente la fenêtre de l'application graphique. */
    private JFrame frame;
    /**
     * VueTrain, VueJoueurs et VueJoueurs sont trois classes définies nos trois parties de l'interface graphique.
     */
    private VueTrain train;
    private VueJoueurs players;
    private VueCommandes commandes;

    /** Construction d'une vue attachée à un modèle. */
    public CVue(CModele modele) {
        /** Définition de la fenêtre principale. */
        frame = new JFrame();
        frame.setTitle("Colt Express :D");
        frame.setSize(1300, 600);
        frame.setLocationRelativeTo(null);
        //frame.setLayout(new BorderLayout());

        /** Définition des deux vues et ajout à la fenêtre. */
        train = new VueTrain(modele);
        frame.getContentPane().add(train, BorderLayout.CENTER);

        commandes = new VueCommandes(modele);
        frame.getContentPane().add(commandes, BorderLayout.NORTH);

        players = new VueJoueurs(modele.getPlayers());
        frame.getContentPane().add(players, BorderLayout.SOUTH);

        this.train.repaint();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    /** whether the first page (play screen) is to be displayed. */
    protected boolean deb;
    /** whether the second page (character picker screen) is to be displayed. */
    protected boolean characDeb;
    /** Represents the window for the character picker screen. */
    JFrame fr;
    /**
     * Method to facilitate creating labels given the name.
     * @param name The name you want your label to be.
     */
    public JLabel setLabel(String name){
        JLabel label = new JLabel(name);
        Color color = new Color(255,242,242);
        Font font = new Font("Monospaced", Font.PLAIN, this.getFont().getSize());
        label.setFont(font);
        label.setSize(this.getSize());
        label.setForeground(color);
        return label;
    }
    /** Method to facilitate creating buttons of characters given the file source path,
     * player ID, color, and the start button.
     */
    public JButton settingButton(String filename, int id, Color col, JButton start){
        JButton b=new JButton();
        setButton(b,filename);
        Image img = new ImageIcon(getClass().getResource(filename)).getImage();
        b.addActionListener(e -> { train.pickACharac(id,img);
            start.setBackground(col);
            start.setEnabled(true);
        });
        return b;
    }
    /**
     * Designing the Character picker window.
     */
    public void pickACharac(int playerid){
        characDeb=true;
        //JFrame.setDefaultLookAndFeelDecorated(true);
        fr = new JFrame("Choose your character !");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setLayout(new GridBagLayout());
        backgroundLabel.setSize(fr.getSize());
        fr.setContentPane(backgroundLabel);
        fr.setLayout(new FlowLayout());
        fr.setBackground(Color.LIGHT_GRAY);
        fr.setSize(1300, 600);
        fr.setLocationRelativeTo(null);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton b = new JButton("start");

        JLabel label= new JLabel("Player " + playerid + " pick a character!");
        label.setFont(new Font("Verdana", 1, 20));
        label.setSize(this.getSize());

        String[] names = new String[] {"Jack", "Coco", "Harumi", "Dino", "Carmen", "Eddy"};
        JLabel[] labels = new JLabel[names.length];
        for(int i=0; i<names.length;i++){
            labels[i]=setLabel(names[i]);
        }

        String[] filenames = new String[]{
                "/coltExpress/jack.png",
                "/coltExpress/coco.png",
                "/coltExpress/harumi.png",
                "/coltExpress/dino.png",
                "/coltExpress/carmen.png",
                "/coltExpress/eddy.png"
        };
        Color[] colors = new Color[]{
                new Color(165,184,112),
                new Color(255,70,70),
                new Color(252,162,218),
                new Color(111,154,253),
                new Color(249,253,135),
                new Color(163,111,253)
        };
        JButton[] buttons = new JButton[filenames.length];
        for(int i=0; i<filenames.length;i++){
            buttons[i]=settingButton(filenames[i],playerid, colors[i],b);
        }

        b.addActionListener(e-> {
           fr.setVisible(false);
            if (playerid<NB_JOUEURS-1){ pickACharac(playerid+1); fr.setVisible(true);}
            else {
                characDeb = false;
                frame.setVisible(true);
            }


        });

        fr.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.gridx = 0;
        c.gridy = 1;
        fr.getContentPane().add(buttons[0], c);
        c.gridx = 0;
        c.gridy = 2;
        fr.getContentPane().add(labels[0], c);
        c.gridx = 1;
        c.gridy = 1;
        fr.getContentPane().add(buttons[1],c);
        c.gridx = 1;
        c.gridy = 2;
        fr.getContentPane().add(labels[1], c);
        c.gridx = 2;
        c.gridy = 1;
        fr.getContentPane().add(buttons[2],c);
        c.gridx = 2;
        c.gridy = 2;
        fr.getContentPane().add(labels[2], c);
        c.gridx = 0;
        c.gridy = 3;
        fr.getContentPane().add(buttons[3], c);
        c.gridx = 0;
        c.gridy = 4;
        fr.getContentPane().add(labels[3], c);
        c.gridx = 1;
        c.gridy = 3;
        fr.getContentPane().add(buttons[4],c);
        c.gridx = 1;
        c.gridy = 4;
        fr.getContentPane().add(labels[4], c);
        c.gridx = 2;
        c.gridy = 3;
        fr.getContentPane().add(buttons[5],c);
        c.gridx = 2;
        c.gridy = 4;
        fr.getContentPane().add(labels[5], c);
        c.gridx = 1;
        c.gridy = 5;
        fr.getContentPane().add(b,c);
        c.gridx = 1;
        c.gridy = 0;
        fr.getContentPane().add(label,c);
        b.setEnabled(false);

    }

    /** From the internet -- source cited in README */
    public void setButton(JButton b, String file_name){
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        Image img= new ImageIcon(getClass().getResource(file_name)).getImage().getScaledInstance(128,128,Image.SCALE_DEFAULT);
        ImageIcon icon= new ImageIcon(img);
        b.setIcon(icon);
    }
    /** Represents the window for the Play (Home) screen. */
    JFrame f;

    ImageIcon backgroundImage = new ImageIcon(getClass().getResource("/coltExpress/back.jpg"));

    /**
     * Designing the Home window.
     */
    public void firstPage() {
        deb=true;
        f = new JFrame("Welcome to the Colt Express game!");

        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setLayout(new GridBagLayout());
        backgroundLabel.setSize(f.getSize());
        f.setContentPane(backgroundLabel);


        f.setSize(1300, 600);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton playButton = new JButton();
        JLabel label = new JLabel("COLT EXPRESS");
        JLabel label2 = new JLabel("Tutorial for buttons:");
        JLabel blank = new JLabel(" ");
        JLabel blank2 = new JLabel(" ");
        JLabel dep = new JLabel("Press Arrow (> ^ < v ) to Move");
        JLabel shoot = new JLabel("Hold Shift + Arrow to Shoot");
        JLabel rob = new JLabel("Press B to rob (grab loots)");


        /** labels' design editing */
        label.setFont(new Font("Futura", Font.BOLD, 70));
        label.setForeground(Color.WHITE);

        label2.setFont(new Font("Verdana", Font.BOLD, 20));
        blank.setFont(new Font("Verdana", Font.BOLD, 40));
        blank2.setFont(new Font("Verdana", Font.BOLD, 60));

        Color color = new Color(255,242,242);
        dep.setFont(new Font("Monospaced", Font.PLAIN, this.getFont().getSize()));
        dep.setForeground(color);
        shoot.setFont(new Font("Monospaced", Font.PLAIN, this.getFont().getSize()));
        shoot.setForeground(color);
        rob.setFont(new Font("Monospaced", Font.PLAIN, this.getFont().getSize()));
        rob.setForeground(color);


        setButton(playButton,"/coltExpress/play.png");

        playButton.addActionListener(e -> {
            f.setVisible(false);
            fr.setVisible(true);
            deb = false;
            characDeb=true;


        });

        f.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = -1;
        f.getContentPane().add(label, c);
        c.gridy = 1;
        f.getContentPane().add(blank2, c);
        c.gridy = 2;
        f.getContentPane().add(playButton, c);
        c.gridy = 3;
        f.getContentPane().add(blank, c);

        c.gridy = 4;
        f.getContentPane().add(label2, c);

        c.gridy = 5;
        f.getContentPane().add(dep, c);
        c.gridy = 6;
        f.getContentPane().add(shoot, c);
        c.gridy = 7;
        f.getContentPane().add(rob, c);


        f.setVisible(true);
    }

}
/**
 * A class to represent the area where our train filled with the loots, Marshall, and players are.
 * Cette vue va être un observateur du modèle et sera mise à jour à chaque mouvement ou action de Bandits ou Marshall.
 */
class VueTrain extends JPanel implements Observer {
    /** On maintient une référence vers le modèle. */
    private CModele modele;
    /** Fixing the size of a wagon. */
    private final static int largeurWagon = 260;
    private final static int hauteurWagon = 195;
    private Image background; /** Background picture by default*/

    /** Constructeur. */
    public VueTrain(CModele modele) {
        this.modele = modele;
        playersImages= new ArrayList<>(NB_JOUEURS);
        /** On enregistre la vue [this] en tant qu'observateur de [modele]. */
        modele.addObserver(this);
        Dimension dim = new Dimension(this.largeurWagon * NB_WAGONS + 250,
                this.hauteurWagon + 100);
        this.setPreferredSize(new Dimension(500,500));
        this.setBackground(Color.WHITE);
        update();
    }
    /**
     * L'interface [Observer] demande de fournir une méthode [update], qui
     * sera appelée lorsque la vue sera notifiée d'un changement dans le
     * modèle. Ici on se contente de réafficher toute la grille avec la méthode
     * prédéfinie [repaint].
     */
    public void update() { repaint(); }
    /**
     * Les éléments graphiques comme [JPanel] possèdent une méthode
     * [paintComponent] qui définit l'action à accomplir pour afficher cet
     * élément. On la redéfinit ici pour lui confier l'affichage des cellules.
     *
     * La classe [Graphics] regroupe les éléments de style sur le dessin,
     * comme la couleur actuelle.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        background = new ImageIcon(getClass().getResource("/coltExpress/back.jpg")).getImage();
        g.drawImage(background, 0, 0, this);

        int x = 0;
        int y = 100;

        int roofY = y-30;
        int insideY = y+100;

        /** Differentiated for when we want to design locomotive with another image **/
        TrainElts currentWagon = modele.getTrain().wagons.get(0).get(0);
        TrainElts currentWagonRoof = modele.getTrain().wagons.get(1).get(0);

        paintLoco(g, currentWagon, x, y +80 );
        paintLocoRoof(g, currentWagonRoof, x, y - hauteurWagon +80 );

        paintMarshall(g, currentWagon, x, insideY);
        paintPlayers(g, currentWagon, x, insideY);
        paintButins(g, currentWagon, x, insideY);

        /** Knowing that Marshall climbs to the roof. **/
        paintPlayers(g, currentWagonRoof, x, roofY);
        paintButins(g, currentWagonRoof, x, roofY);

        for (int i = 1; i < Train.NB_WAGONS; i++) {
            currentWagon = modele.getTrain().wagons.get(0).get(i);
            currentWagonRoof = modele.getTrain().wagons.get(1).get(i);

            if (currentWagon != null){
                /** Paint wagon components. **/
                paintWagon(g, currentWagon, i * largeurWagon, y +80  );
                paintMarshall(g, currentWagon, i * largeurWagon, insideY);
                paintPlayers(g, currentWagon, i * largeurWagon, insideY);
                paintButins(g, currentWagon, i * largeurWagon, insideY);}
            if (currentWagonRoof != null) {
                /** Paint roof components. **/
                paintRoof(g, currentWagonRoof, i * largeurWagon, y - hauteurWagon +80);
                paintMarshall(g, currentWagonRoof, i * largeurWagon, roofY);
                paintPlayers(g, currentWagonRoof, i * largeurWagon, roofY);
                paintButins(g, currentWagonRoof, i * largeurWagon, roofY);
            }

        } update();
    }
    private Image marshall;
    ArrayList<Image> playersImages;

    /** Paint players if elements of the train contain them. **/
    private void paintPlayers(Graphics g,TrainElts w, int x, int y){
        int k=0;
        if (w.getMarshall()!=null) k+=15;
        for (int j = 0; j<w.getPlayers().size();j++){
            if(playersImages!=null) g.drawImage( playersImages.get(w.getPlayers().get(j).getPlayerID()), x+10+k ,y+30, 100 , 100,this);
            else g.drawString("Player" + w.getPlayers().get(j).getPlayerID(), x+10, y + 90 + k);
            repaint();
            k+=15;
        }
        repaint();
    }

    public void pickACharac(int i, Image im) {
        playersImages.add(i,im);
    }
    /** Paint Marshall if elements of the train contain them. **/
    private void paintMarshall(Graphics g,TrainElts w, int x, int y){
        marshall = new ImageIcon(getClass().getResource("/coltExpress/marshall.png")).getImage();
        if (w.getMarshall() != null)
            g.drawImage(marshall, x+10, y+30, 100,100, this);

        repaint();
    }

    private Image bijou,bourse,magot;
    /** Paint loots if elements of the train contain them. **/
    private void paintButins(Graphics g,TrainElts w, int x, int y) {
        bijou = new ImageIcon(getClass().getResource("/coltExpress/bijou.png")).getImage();
        bourse = new ImageIcon(getClass().getResource("/coltExpress/bourse.png")).getImage();

        magot = new ImageIcon(getClass().getResource("/coltExpress/magot.png")).getImage();
        int k = 0;
        int width=25; int height=25;
        if (w.getButins() != null) {
            for (int j = 0; j < w.getButins().size(); j++) {
                if (w.getButins().get(j) != null) {
                    switch (w.getButins().get(j).getType()) {
                        case "bijoux" -> g.drawImage(bijou, x + 90 + k, y + 90, width, height, this);
                        case "bourses" -> g.drawImage(bourse, x + 90 + k, y + 90, width, height, this);
                        case "magot" -> g.drawImage(magot, x + 90 + k, y + 70, width + 20, height + 20, this);
                        default -> {
                        }
                    }
                    k += 25;
                }
            }
        }
    }

    private Image loco, wagon, locoRoof;
    /** Paint a wagon. **/
    private void paintWagon(Graphics g, TrainElts w, int x, int y) {
        wagon = new ImageIcon(getClass().getResource("/coltExpress/wagon3.jpg")).getImage();

        g.drawImage(wagon,x+10,y, largeurWagon,hauteurWagon,this);
        g.drawString("Wagon " + w.getWagonNum(), x + 10, y + 20);
    }
    /** Paint a roof. **/
    private void paintRoof(Graphics g, TrainElts w, int x, int y) {

        g.drawString("Roof " + w.getWagonNum(), x + 10, y + 20);
    }
    /** Paint a locomotive's roof. **/
    private void paintLocoRoof(Graphics g, TrainElts w, int x, int y) {
        locoRoof = new ImageIcon(getClass().getResource("/coltExpress/roofLoco.jpg")).getImage();
        g.drawImage(locoRoof,x+10,y, largeurWagon,hauteurWagon,this);
        g.drawString("Roof " + w.getWagonNum(), x + 10, y + 20);
    }
    /** Paint a locomotive's wagon. **/
    private void paintLoco(Graphics g, TrainElts w, int x, int y) {
        loco = new ImageIcon(getClass().getResource("/coltExpress/locomotive.jpg")).getImage();

        g.drawImage(loco,x+10,y, largeurWagon,hauteurWagon,this);
        g.drawString("Locomotive", x + 10, y+ 20);

    }
}

/**
 * A class to represent one players' advancements in the game.
 * This view will be an observer of the model and will be updated with each loot picked or dropped, or bullet used by bandit.
 */
class VueJoueur extends JPanel implements Observer {
    private Bandit bandit;
    private JLabel labelMagots, labelBijoux, labelBourses, labelBalles, labelScores;

    /** Constructor given the Bandit. */
    public VueJoueur(Bandit b) {
        this.setBackground(new Color(128, 138, 120));

        this.setBorder(BorderFactory.createLineBorder(Color.white));
        JLabel blank = new JLabel(" ");
        JLabel blank2 = new JLabel(" ");

        this.bandit = b;
        b.addObserver(this);

        JLabel labelPlayer = (new JLabel("Player" + b.getPlayerID()));
        labelPlayer.setFont(new Font("Market deco", Font.BOLD, 16));
        labelPlayer.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(labelPlayer);

        this.setLayout(new GridLayout(4, 2));
        labelMagots = new JLabel("Magots: " + b.getNbLoot("magot"));
        labelBijoux = new JLabel("Bijoux : " + b.getNbLoot("bijoux"));
        labelBourses = new JLabel("Bourses : " + b.getNbLoot("bourses"));
        labelBalles = new JLabel("Bullets : " + b.getNbBalles());
        labelScores = new JLabel("Score : $" + b.getScore());
        labelScores.setFont(new Font("Verdana", Font.BOLD, 13));


        this.add(blank2);

        labelMagots.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(labelMagots);

        labelBalles.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(labelBalles);

        labelBourses.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(labelBourses);

        labelScores.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(labelScores);

        labelBijoux.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(labelBijoux);
        this.add(blank);

        update();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);update();
    }
    public void update() {
        this.labelMagots.setText("Magots: " + bandit.getNbLoot("magot")); //needs to be modified to cater to each butin
        this.labelBijoux.setText("Bijoux : " + bandit.getNbLoot("bijoux"));
        this.labelBourses.setText("Bourses : " + bandit.getNbLoot("bourses"));
        this.labelBalles.setText("Bullets : " +  bandit.getNbBalles());
        this.labelScores.setText("Score : $" + bandit.getScore());
        labelScores.setFont(new Font("Verdana", Font.BOLD, 13));
        repaint();
    }


}
/**
 * A class to represent the area where we can keep track of each players' advancements in the game.
 * This view will be an observer of the model and will be updated with each loot picked or dropped, or bullet used by bandits.
 */
class VueJoueurs extends JPanel{
    protected ArrayList<VueJoueur> vueBandits=new ArrayList<>();
    public VueJoueurs (ArrayList <Bandit>bandits) {
        this.setLayout(new GridLayout( 1, bandits.size()));
        for (Bandit b:  bandits) {
            VueJoueur vj = new VueJoueur (b);
            vueBandits.add(vj);
            this.add(vj);
        }
    }
}
/**
 * Une classe pour représenter la zone contenant les boutons.
 *
 * Cette zone n'aura pas à être mise à jour et ne sera donc pas un observateur.
 * En revanche, comme la zone précédente, celle-ci est un panneau [JPanel].
 */
class VueCommandes extends JPanel {
    /**
     * Pour que le bouton puisse transmettre ses ordres, on garde une
     * référence au modèle.
     */
    private CModele modele;
    /** An array list of all the buttons we have to keep track of players' intended actions. */
    private final ArrayList<JButton> actionButtons;

    JLabel currentPlayerLabel;
    JLabel currentActionLabel;
    JLabel playerButtonLabel;
    JButton restart;
    /**
     * Retrieves the winner's loots value for the Winner display
     * */
    public  int winnerCounter(){
        if(Controleur.roundCount==NB_MAX_ROUNDS){
            for(Bandit b : modele.getPlayers()){
                if(Controleur.scoreMax < b.score) {
                    Controleur.setWinnerID( b.getPlayerID());
                   Controleur.scoreMax= b.score;
                }
            }
        } return Controleur.scoreMax;
    }
    /**
     * Retrieves the winner's player ID for the Winner display
     * */
    public  int playerWinnerCounter(){
        if(Controleur.roundCount==NB_MAX_ROUNDS){
            for(Bandit b : modele.getPlayers()){
                if(Controleur.scoreMax < b.score) {
                    Controleur.winnerID = b.getPlayerID();
                   Controleur.scoreMax= b.score;
                }
            }
        } return Controleur.winnerID;
    }
    /**
     * Checks for players with the same maximum scores to display that no one wins
     * */
    public boolean noWinners(){
        int s = modele.getPlayers().get(0).score;
        if(Controleur.roundCount==NB_MAX_ROUNDS) {
            for (int i = 1; i < modele.getPlayers().size(); i++) {
                if (modele.getPlayers().get(i).score == s) {
                    if(modele.getPlayers().get(i).score == winnerCounter())
                    return true;
                }
            }
        } return false;
    }


    /** Updating the texts of action displayed on the screen to facilitate
     * the keeping track of players' actions and their consequences. */
    public void update() {
        playerButtonLabel.setText(" ");
        ArrayList<Action> playerActions = modele.getActions().get(Controleur.getCurrentPlayer());
        if (!playerActions.isEmpty()) {
            int idx = round - 1;
            if (idx >= 0 && idx < playerActions.size()) {
                Action currentAction = playerActions.get(idx);
                String actionText = currentAction.getTextAction();
                currentActionLabel.setText(actionText);
            } else {
                currentActionLabel.setText(" ");
            }
        } else {
            currentActionLabel.setText(" ");
        }

        /** Check if all rounds are completed. */
        if (round > NB_ACTIONS) {
            for(int i=0; i<NB_JOUEURS; i++) modele.getActions().get(i).clear();
            /** Enable all buttons and disable "GO!" button */
            for (JButton button : actionButtons) {
                button.setEnabled(true);
            }
            /** Disable "GO!" button */
            actionButtons.get(0).setEnabled(false);
            Controleur.setCurrentPlayer(0);
            currentPlayerLabel.setText("Joueur " + Controleur.getCurrentPlayer() + " choisi ses actions");
            if(Controleur.roundCount<NB_MAX_ROUNDS) Controleur.roundCount+=1;

            else {
                if (noWinners()){ currentPlayerLabel.setText("No winner!");}
                else { currentPlayerLabel.setText("Player " + playerWinnerCounter() + " won the game with a score of "+ winnerCounter());}
                for (JButton button : actionButtons) {
                    button.setEnabled(false);
                }
                restart.setEnabled(true);
            }
            /** Exit the method since all buttons have been updated */
            return;
        }
        /** Repaint the panel if necessary */
        repaint();
    }
    /** Constructeur. */
    public VueCommandes(CModele modele) {
        this.setBackground(new Color(161, 147, 116));
        actionButtons = new ArrayList<>();

        this.modele = modele;
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.gridx = 5;
        c.gridy = 7;
        currentActionLabel = new JLabel();
        this.add(currentActionLabel, c);

        c.gridx = 5;
        c.gridy = 5;
        currentPlayerLabel = new JLabel();
        currentPlayerLabel.setFont(new Font("Futura", Font.BOLD, 16));
        currentPlayerLabel.setText("Joueur " + Controleur.getCurrentPlayer() + " choisi ses actions");
        this.add(currentPlayerLabel, c);
        c.gridx = 5;
        c.gridy = 9;
        playerButtonLabel = new JLabel(" ");
        this.add(playerButtonLabel, c);

        JButton boutonAction = new JButton("GO!");
        c.gridx = 10;
        c.gridy = 1;
        this.add(boutonAction, c);
        actionButtons.add(boutonAction);
        boutonAction.setEnabled(false);
        boutonAction.addActionListener(e -> {
            update();
        });
        Controleur ctrl = new Controleur(modele);
        /** Enregistrement du contrôleur comme auditeur du bouton. */
        boutonAction.addActionListener(ctrl);

        /** Inspired from StackOverflow -- source cited in README */
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        /** For keyboard shortcut */
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter.pressed");
        am.put("Enter.pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonAction.doClick();
            }
        });

        /** Button to move forward */
        JButton boutonAvance = new JButton("< Move");
        c.gridx = 1;
        c.gridy = 1;
        this.add(boutonAvance, c);
        actionButtons.add(boutonAvance);
        boutonAvance.addActionListener(e -> {
            int id = Controleur.getCurrentPlayer();
            Bandit player = modele.getPlayers().get(id);
            Deplacer d = new Deplacer(player, Direction.AVANT);
            modele.addAction(d, id);
            System.out.println("Player" + player.getPlayerID() + " avance");
            /** Check if all players have completed their actions for the current round */
            boolean filled = true;
            for (int i = 0; i < modele.getActions().size(); i++) {
                if (modele.getActions().get(i).size() != NB_ACTIONS) {
                    filled = false;
                    break;
                }
            }
            if (filled) {
                boutonAction.setEnabled(true);
                disableOtherButtonsExcept(boutonAction);
            }
            /** Move to the next player's turn after current player's actions are done. */
            if (modele.getActions().get(id).size() >= NB_ACTIONS) {
                int nextPId = id + 1;
                if (nextPId < NB_JOUEURS) {
                    Controleur.setCurrentPlayer(nextPId);
                }
            }
            playerButtonLabel.setText("Player" + player.getPlayerID() + " : button to move forward");
            currentPlayerLabel.setText("Joueur " + Controleur.getCurrentPlayer() + " choisi ses actions");
            if (filled) currentPlayerLabel.setText("TIME TO PRESS GO!");

        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "Left.pressed");
        am.put("Left.pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonAvance.doClick();
            }
        });

        /** Button to move downward */
        JButton boutonDescend = new JButton("Move v");
        c.gridx = 2;
        c.gridy = 2;
        this.add(boutonDescend, c);
        actionButtons.add(boutonDescend);
        boutonDescend.addActionListener(e -> {
            int id = Controleur.getCurrentPlayer();
            Bandit player = modele.getPlayers().get(id);
            Deplacer d = new Deplacer(player, Direction.BAS);
            modele.addAction(d, id);
            System.out.println("Player" + player.getPlayerID() + " descend");
            /** Check if all players have completed their actions for the current round */
            boolean filled = true;
            for (int i = 0; i < modele.getActions().size(); i++) {
                if (modele.getActions().get(i).size() != NB_ACTIONS) {
                    filled = false;
                    break;
                }
            }
            if (filled) {
                /** Enables the Go! button. */
                boutonAction.setEnabled(true);
                disableOtherButtonsExcept(boutonAction);

            }
            /** Move to the next player's turn after current player's actions are done. */
            if (modele.getActions().get(id).size() >= NB_ACTIONS) {
                int nextPId = id + 1;
                if (nextPId < NB_JOUEURS) {
                    Controleur.setCurrentPlayer(nextPId);
                }
            }
            currentPlayerLabel.setText("Joueur " + Controleur.getCurrentPlayer() + " choisi ses actions");
            if (filled) currentPlayerLabel.setText("TIME TO PRESS GO!");
            playerButtonLabel.setText("Player" + player.getPlayerID() + " : button to move downward");
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "Down.pressed");
        am.put("Down.pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonDescend.doClick();
            }
        });
        /** Button to move backward */
        JButton boutonRecule = new JButton("Move >");
        c.gridx = 3;
        c.gridy = 1;
        this.add(boutonRecule, c);
        actionButtons.add(boutonRecule);

        boutonRecule.addActionListener(e -> {
            int id = Controleur.getCurrentPlayer();
            Bandit player = modele.getPlayers().get(id);
            Deplacer d = new Deplacer(player, Direction.ARRIERE);
            modele.addAction(d, id);
            System.out.println("Player" + player.getPlayerID() + " arriere");
            /** check if all players have completed their actions for the current round*/
            boolean filled = true;
            for (int i = 0; i < modele.getActions().size(); i++) {
                if (modele.getActions().get(i).size() != NB_ACTIONS) {
                    filled = false;
                    break;
                }
            }
            if (filled) {
                boutonAction.setEnabled(true);
                disableOtherButtonsExcept(boutonAction);

            }
            /** move to the next player's turn after current player's actions are done*/
            if (modele.getActions().get(id).size() >= NB_ACTIONS) {
                int nextPId = id + 1;
                if (nextPId < NB_JOUEURS) {
                    Controleur.setCurrentPlayer(nextPId);
                }
            }
            currentPlayerLabel.setText("Joueur " + Controleur.getCurrentPlayer() + " choisi ses actions");
            if (filled) currentPlayerLabel.setText("TIME TO PRESS GO!");
            playerButtonLabel.setText("Player" + player.getPlayerID() + " : button to move backward");
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "Right.pressed");
        am.put("Right.pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonRecule.doClick();
            }
        });
        /** Button to climb the roof */
        JButton boutonMonte = new JButton("Move ^");
        c.gridx = 2;
        c.gridy = 0;
        this.add(boutonMonte, c);
        actionButtons.add(boutonMonte);

        boutonMonte.addActionListener(e -> {
            int id = Controleur.getCurrentPlayer();
            Bandit player = modele.getPlayers().get(id);
            Deplacer d = new Deplacer(player, Direction.HAUT);
            modele.addAction(d, id);
            System.out.println("Player" + player.getPlayerID() + " monte");
            /** check if all players have completed their actions for the current round*/
            boolean filled = true;
            for (int i = 0; i < modele.getActions().size(); i++) {
                if (modele.getActions().get(i).size() != NB_ACTIONS) {
                    filled = false;
                    break;
                }
            }
            if (filled) {
                boutonAction.setEnabled(true);
                disableOtherButtonsExcept(boutonAction);

            }
            /** move to the next player's turn after currentplayer's actions are done*/
            if (modele.getActions().get(id).size() >= NB_ACTIONS) {
                int nextPId = id + 1;
                if (nextPId < NB_JOUEURS) {
                    Controleur.setCurrentPlayer(nextPId);
                }
            }
            currentPlayerLabel.setText("Joueur " + Controleur.getCurrentPlayer() + " choisi ses actions");
            if (filled) currentPlayerLabel.setText("TIME TO PRESS GO!");
            playerButtonLabel.setText("Player" + player.getPlayerID() + " : button to climb the roof");
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "Up.pressed");
        am.put("Up.pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonMonte.doClick();
            }
        });

        /** Button to shoot upward */
        JButton boutonTireHaut = new JButton("Tire ^");
        c.gridx = 8;
        c.gridy = 0;
        this.add(boutonTireHaut, c);
        actionButtons.add(boutonTireHaut);


        boutonTireHaut.addActionListener(e -> {
            int id = Controleur.getCurrentPlayer();
            Bandit player = modele.getPlayers().get(id);
            Tirer t = new Tirer(modele.getPlayers().get(id), Direction.HAUT);
            modele.addAction(t, id);
            System.out.println("Player" + player.getPlayerID() + " tire haut");
            /** check if all players have completed their actions for the current round */
            boolean filled = true;
            for (int i = 0; i < modele.getActions().size(); i++) {
                if (modele.getActions().get(i).size() != NB_ACTIONS) {
                    filled = false;
                    break;
                }
            }
            if (filled) {
                boutonAction.setEnabled(true);
                disableOtherButtonsExcept(boutonAction);

            }
            //move to the next player's turn after currentplayer's actions are done
            if (modele.getActions().get(id).size() >= NB_ACTIONS) {
                int nextPId = id + 1;
                if (nextPId < NB_JOUEURS) {
                    Controleur.setCurrentPlayer(nextPId);
                }
            }
            currentPlayerLabel.setText("Joueur " + Controleur.getCurrentPlayer() + " choisi ses actions");
            if (filled) currentPlayerLabel.setText("TIME TO PRESS GO!");
            playerButtonLabel.setText("Player" + player.getPlayerID() + " : button to shoot upward");
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,KeyEvent.SHIFT_DOWN_MASK), "ShiftUP.pressed");
        am.put("ShiftUP.pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonTireHaut.doClick();
            }
        });
    /** Button to shoot downward */
        JButton boutonTireBas = new JButton("Tire V");
        c.gridx = 8;
        c.gridy = 2;
        this.add(boutonTireBas, c);
        actionButtons.add(boutonTireBas);

        boutonTireBas.addActionListener(e -> {
            int id = Controleur.getCurrentPlayer();
            Bandit player = modele.getPlayers().get(id);
            Tirer t = new Tirer(modele.getPlayers().get(id), Direction.BAS);
            modele.addAction(t, id);
            System.out.println("Player" + player.getPlayerID() + " tire bas");
            /** check if all players have completed their actions for the current round */
            boolean filled = true;
            for (int i = 0; i < modele.getActions().size(); i++) {
                if (modele.getActions().get(i).size() != NB_ACTIONS) {
                    filled = false;
                    break;
                }
            }
            if (filled) {
                boutonAction.setEnabled(true);
                disableOtherButtonsExcept(boutonAction);

            } //enable go!
            //move to the next player's turn after currentplayer's actions are done
            if (modele.getActions().get(id).size() >= NB_ACTIONS) {
                int nextPId = id + 1;
                if (nextPId < NB_JOUEURS) {
                    Controleur.setCurrentPlayer(nextPId);
                }
            }
            currentPlayerLabel.setText("Joueur " + Controleur.getCurrentPlayer() + " choisi ses actions");
            if (filled) currentPlayerLabel.setText("TIME TO PRESS GO!");
            playerButtonLabel.setText("Player" + player.getPlayerID() + " : button to shoot downward");
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,KeyEvent.SHIFT_DOWN_MASK), "ShiftDOWN.pressed");
        am.put("ShiftDOWN.pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonTireBas.doClick();
            }
        });

        /** Button to shoot forward */
        JButton boutonTireAvant = new JButton("< Tire");
        c.gridx = 7;
        c.gridy = 1;
        this.add(boutonTireAvant, c);
        actionButtons.add(boutonTireAvant);
        boutonTireAvant.addActionListener(e -> {
            int id = Controleur.getCurrentPlayer();
            Bandit player = modele.getPlayers().get(id);
            Tirer t = new Tirer(modele.getPlayers().get(id), Direction.AVANT);
            modele.addAction(t, id);
            System.out.println("Player" + player.getPlayerID() + " tire avant");
            /** check if all players have completed their actions for the current round */
            boolean filled = true;
            for (int i = 0; i < modele.getActions().size(); i++) {
                if (modele.getActions().get(i).size() != NB_ACTIONS) {
                    filled = false;
                    break;
                }
            }
            if (filled) {
                boutonAction.setEnabled(true);
                disableOtherButtonsExcept(boutonAction);

            } //enable go!
            //move to the next player's turn after currentplayer's actions are done
            if (modele.getActions().get(id).size() >= NB_ACTIONS) {
                int nextPId = id + 1;
                if (nextPId < NB_JOUEURS) {
                    Controleur.setCurrentPlayer(nextPId);
                }
            }
            currentPlayerLabel.setText("Joueur " + Controleur.getCurrentPlayer() + " choisi ses actions");
            if (filled) currentPlayerLabel.setText("TIME TO PRESS GO!");
            playerButtonLabel.setText("Player" + player.getPlayerID() + " : button to shoot forward");
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,KeyEvent.SHIFT_DOWN_MASK), "ShiftLEFT.pressed");
        am.put("ShiftLEFT.pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonTireAvant.doClick();
            }
        });
        /** Button to shoot backward */
        JButton boutonTireArriere = new JButton("Tire >");
        c.gridx = 9;
        c.gridy = 1;
        this.add(boutonTireArriere, c);
        actionButtons.add(boutonTireArriere);

        boutonTireArriere.addActionListener(e -> {
            int id = Controleur.getCurrentPlayer();
            Bandit player = modele.getPlayers().get(id);
            Tirer t = new Tirer(modele.getPlayers().get(id), Direction.ARRIERE);
            modele.addAction(t, id);
            System.out.println("Player" + player.getPlayerID() + " tire arriere");
            /** check if all players have completed their actions for the current round */
            boolean filled = true;
            for (int i = 0; i < modele.getActions().size(); i++) {
                if (modele.getActions().get(i).size() != NB_ACTIONS) {
                    filled = false;
                    break;
                }
            }
            if (filled) {
                boutonAction.setEnabled(true);
                disableOtherButtonsExcept(boutonAction);

            } //enable go!
            //move to the next player's turn after currentplayer's actions are done
            if (modele.getActions().get(id).size() >= NB_ACTIONS) {
                int nextPId = id + 1;
                if (nextPId < NB_JOUEURS) {
                    Controleur.setCurrentPlayer(nextPId);
                }
            }
            currentPlayerLabel.setText("Joueur " + Controleur.getCurrentPlayer() + " choisi ses actions");
            if (filled) currentPlayerLabel.setText("TIME TO PRESS GO!");
            playerButtonLabel.setText("Player" + player.getPlayerID() + " : button to shoot backward");
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,KeyEvent.SHIFT_DOWN_MASK), "ShiftRIGHT.pressed");
        am.put("ShiftRIGHT.pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonTireArriere.doClick();
            }
        });
        /** Button to rob */
        JButton boutonBraque = new JButton("Braque $");
        c.gridx = 0;
        c.gridy = 1;
        this.add(boutonBraque, c);
        actionButtons.add(boutonBraque);
        boutonBraque.addActionListener(e -> {
            int id = Controleur.getCurrentPlayer();
            Bandit player = modele.getPlayers().get(id);
            Braquer b = new Braquer(player);
            modele.addAction(b, id);
            System.out.println("Player" + player.getPlayerID() + " braque");
            /** check if all players have completed their actions for the current round */
            boolean filled = true;
            for (int i = 0; i < modele.getActions().size(); i++) {
                if (modele.getActions().get(i).size() != NB_ACTIONS) {
                    filled = false;
                    break;
                }
            }
            if (filled) {
                boutonAction.setEnabled(true);
                disableOtherButtonsExcept(boutonAction);

            } //enable go!
            //move to the next player's turn after currentplayer's actions are done
            if (modele.getActions().get(id).size() >= NB_ACTIONS) {
                int nextPId = id + 1;
                if (nextPId < NB_JOUEURS) {
                    Controleur.setCurrentPlayer(nextPId);
                }
            }
            currentPlayerLabel.setText("Joueur " + Controleur.getCurrentPlayer() + " choisi ses actions");
            if (filled) currentPlayerLabel.setText("TIME TO PRESS GO!");
            playerButtonLabel.setText("Player" + player.getPlayerID() + " : button to ROB$$$");
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "B.pressed");
        am.put("B.pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonBraque.doClick();
            }
        });
        /** Button to restart the game. */
        restart = new JButton("RESTART");
        c.gridx = 0;
        c.gridy = 0;
        this.add(restart, c);
        restart.setEnabled(false);
        restart.addActionListener(e -> {
            this.setVisible(false);
            /** clearing the previous game's players */
            modele.getPlayers().clear();
            wagons.clear();
            /** creating a new game */
            CModele modelenew = new CModele();
            CVue vue = new CVue(modelenew);
            vue.firstPage();
            vue.pickACharac(0);
            Controleur.roundCount=1;
            /** resetting the maxScore to 0 */
            Controleur.scoreMax=0;
            restart.setEnabled(false);
        });


    }
    /**
     * Method to facilitate disabling all other buttons except for the specified button
     * @param b The button exception.
     */
    private void disableOtherButtonsExcept(JButton b) {
        for (JButton button : actionButtons) {
            if (button != b) {
                button.setEnabled(false);
            }
        }
    }


}
